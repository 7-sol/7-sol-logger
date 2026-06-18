/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/**
 * Service that subscribes to NATS and persists audit logs in batches.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class NatsSubscriber {

  private static final int BATCH_SIZE = 500;
  private static final int BATCH_TIMEOUT_MS = 2000;
  private static final int QUEUE_CAPACITY = 10000;

  private final MongoTemplate mongoTemplate;
  private final ObjectMapper objectMapper;

  @Value("${nats.url}")
  private String natsUrl;

  @Value("${nats.subject:audit.logs}")
  @Getter
  private String subject;

  private Connection natsConnection;

  /**
   * Returns the active NATS connection.
   *
   * @return The NATS connection.
   */
  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Connection getConnection() {
    return natsConnection;
  }


  private Dispatcher dispatcher;
  private Thread processorThread;
  private final BlockingQueue<AuditLogDto> logQueue =
      new LinkedBlockingQueue<>(QUEUE_CAPACITY);
  private volatile boolean running = true;

  /**
   * Initializes the NATS connection and starts the ingestion pipeline.
   *
   * @throws IOException If a connection error occurs.
   * @throws InterruptedException If the connection process is interrupted.
   */
  @PostConstruct
  public void start() throws IOException, InterruptedException {
    Options options = new Options.Builder().server(natsUrl).build();
    natsConnection = Nats.connect(options);

    dispatcher = natsConnection.createDispatcher(msg -> {
      try {
        AuditLogDto logEntry = objectMapper.readValue(
            msg.getData(), AuditLogDto.class);
        if (!logQueue.offer(logEntry)) {
          log.warn("Log queue is full, dropping log entry: {}", logEntry.traceId());
        }
      } catch (Exception e) {
        log.error("Failed to deserialize audit log", e);
      }
    });

    dispatcher.subscribe(subject);

    // Start a virtual thread for batch processing
    processorThread = Thread.ofVirtual()
        .name("nats-batch-processor")
        .start(this::processLogs);
  }

  /**
   * Continuous loop to process logs from the queue in batches.
   */
  private void processLogs() {
    List<AuditLogDto> batch = new ArrayList<>(BATCH_SIZE);
    while (running || !logQueue.isEmpty()) {
      try {
        long startTime = System.currentTimeMillis();
        while (batch.size() < BATCH_SIZE &&
            (System.currentTimeMillis() - startTime) < BATCH_TIMEOUT_MS) {
          
          long remainingTime = BATCH_TIMEOUT_MS - 
              (System.currentTimeMillis() - startTime);
          AuditLogDto entry = logQueue.poll(
              Math.max(0, remainingTime), TimeUnit.MILLISECONDS);
          
          if (entry != null) {
            batch.add(entry);
          } else {
            break; // Timeout reached or queue empty
          }
        }

        if (!batch.isEmpty()) {
          persistBatch(batch);
          batch.clear();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("Batch processor interrupted", e);
        break;
      } catch (Exception e) {
        log.error("Error in batch processor loop", e);
      }
    }
    log.info("Batch processor thread finished.");
  }

  /**
   * Persists a batch of audit logs to MongoDB.
   *
   * @param batch The list of audit logs to persist.
   */
  private void persistBatch(List<AuditLogDto> batch) {
      var consolidatedBatch = consolidate(batch);
      try {
          mongoTemplate.insertAll(consolidatedBatch);
          log.debug("Successfully persisted batch of {} logs", consolidatedBatch.size());
      } catch (Exception e) {
          log.error("Failed to persist batch of {} logs", consolidatedBatch.size(), e);
          // In a real scenario, we might want to retry or move to a DLQ
      }
  }

  private List<AuditLog> consolidate(List<AuditLogDto> batch) {
      var traceIdMap = new HashMap<String, List<AuditLog>>();

      for (AuditLogDto dto : batch) {
          if(dto.traceId() == null){
              var uuid = UUID.randomUUID().toString();
              traceIdMap.put(uuid, List.of(toLog(uuid, dto)));
          }
          else{
              traceIdMap.computeIfAbsent(dto.traceId(), k -> new ArrayList<>()).add(toLog(dto.traceId(), dto));
          }
      }
  }

  private AuditLog toLog(String traceId, AuditLogDto dto) {
      var actionMap = new HashMap<String, List<LogAction>>();

      var log = new AuditLog();
      log.setTimestamp(dto.timestamp());
      log.setTraceId(traceId);
      log.setDbOid(dto.dbOid());
      log.setData(dto.data());
      return log;
  }

  /**
   * Gracefully shuts down the NATS connection.
   *
   * @throws InterruptedException If the close process is interrupted.
   */
  @PreDestroy
  public void stop() throws InterruptedException {
    log.info("Stopping NatsSubscriber...");
    running = false;
    
    if (dispatcher != null && natsConnection != null) {
      natsConnection.closeDispatcher(dispatcher);
    }
    
    if (processorThread != null) {
      processorThread.join(Duration.ofSeconds(5));
    }
    
    if (natsConnection != null) {
      natsConnection.close();
    }
    log.info("NatsSubscriber stopped.");
  }
}
