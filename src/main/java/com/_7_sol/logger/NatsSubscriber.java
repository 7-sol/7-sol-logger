/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Service that subscribes to NATS and persists audit logs in batches.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class NatsSubscriber {

  private static final int BATCH_SIZE = 500;
  private static final int BATCH_TIMEOUT_SECONDS = 2;

  private final ReactiveMongoTemplate mongoTemplate;
  private final ObjectMapper objectMapper;

  @Value("${nats.url}")
  private String natsUrl;

  @Value("${nats.subject:audit.logs}")
  private String subject;

  private Connection natsConnection;
  private Dispatcher dispatcher;
  private final Sinks.Many<AuditLog> sink = Sinks.many()
      .multicast()
      .onBackpressureBuffer();


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
        AuditLog logEntry = objectMapper.readValue(
            msg.getData(), AuditLog.class);
        sink.tryEmitNext(logEntry);
      } catch (Exception e) {
        log.error("Failed to deserialize audit log", e);
      }
    });

    dispatcher.subscribe(subject);

    sink.asFlux()
        .bufferTimeout(BATCH_SIZE, Duration.ofSeconds(BATCH_TIMEOUT_SECONDS))
        .flatMap(this::persistBatch)
        .subscribe();
  }

  /**
   * Persists a batch of audit logs to MongoDB.
   *
   * @param batch The list of audit logs to persist.
   * @return A Flux of the persisted audit logs.
   */
  private Flux<AuditLog> persistBatch(List<AuditLog> batch) {
    if (batch.isEmpty()) {
      return Flux.empty();
    }
    return mongoTemplate.insertAll(batch)
        .doOnError(e -> log.error("Failed to persist batch", e));
  }

  /**
   * Gracefully shuts down the NATS connection.
   *
   * @throws InterruptedException If the close process is interrupted.
   */
  @PreDestroy
  public void stop() throws InterruptedException {
    if (dispatcher != null && natsConnection != null) {
      natsConnection.closeDispatcher(dispatcher);
      natsConnection.close();
    }
  }
}
