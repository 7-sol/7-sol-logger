/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service that verifies the application and NATS connection on startup.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class StartupVerificationService {

  private final NatsSubscriber natsSubscriber;
  private final ObjectMapper objectMapper;
  private final BuildProperties buildProperties;
  /**
   * Sends a test log via NATS after the application has started.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void verifyStartup() {
    try {
      AuditLogDto startupLog = new AuditLogDto(
          null,
          StartupVerificationService.class.getSimpleName() + ".verifyStartup",
          UUID.randomUUID().toString(),
          Long.toHexString(ThreadLocalRandom.current().nextLong()),
          null,
          "SUCCESS",
          UUID.randomUUID().toString(),
          null,
          null,
          System.getenv("HOSTNAME"),
          Instant.now(),
          List.of(new LogEntry(
              Instant.now(),
              "INFO",
              "Log receiver up. NATS up. App:%s Ver:%s"
                      .formatted(buildProperties.getName(), buildProperties.getVersion())))
      );

      byte[] payload = objectMapper.writeValueAsBytes(startupLog);
      natsSubscriber.getConnection().publish(
          natsSubscriber.getSubject(),
          payload
      );
      
      log.info(">>> Startup verification log published successfully to subject: {}",
          natsSubscriber.getSubject());
      
    } catch (Exception e) {
      log.error(">>> Failed to publish startup verification log to NATS", e);
    }
  }
}
