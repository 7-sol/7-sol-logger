/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import static org.assertj.core.api.Assertions.assertThat;

import com._7_sol.logger.util.AuditLog;
import com._7_sol.logger.util.AuditLogDto;
import com._7_sol.logger.util.AuditLogRepository;
import com._7_sol.logger.util.LogAction;
import com._7_sol.logger.util.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Nats;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * End-to-end integration tests for NATS log ingestion.
 */
class NatsIngestionIntegrationTests extends BaseIntegrationTest {

  @DynamicPropertySource
  static void setLocalProperties(DynamicPropertyRegistry registry) {
    registry.add("nats.subject", () -> "test.audit.logs");
  }

  @Autowired
  private AuditLogRepository repository;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Verifies that messages published to NATS are persisted to MongoDB.
   *
   * @throws Exception if message publishing fails.
   */
  @Test
  void shouldIngestFromNatsToMongo() throws Exception {
    ObjectId testId = new ObjectId();
    AuditLogDto dto = new AuditLogDto(
            testId,
            "UPDATE_PROFILE",
            "nats-corr-1",
            "span-1",
            "SUCCESS",
            "user-2",
            "dbId-2",
            "pod-2",
            "192.168.1.200",
            Instant.now(),
            List.of(new LogEntry(Instant.now(), "INFO", "span-1", "Profile updated"))
    );

    // Connect, publish and wait
    try (Connection conn = Nats.connect(natsUrl())) {
      conn.publish("test.audit.logs", objectMapper.writeValueAsBytes(dto));
      conn.flush(Duration.ofSeconds(2));
    }

    // Poll repository until message is persisted or timeout reached
    boolean persisted = false;
    for (int i = 0; i < 20; i++) {
      Optional<AuditLog> found = repository.findAll().stream()
              .filter(al -> "nats-corr-1".equals(al.getTraceId()))
              .findFirst();
      if (found.isPresent()) {
        assertThat(found.get().getTraceId()).isEqualTo("nats-corr-1");
        persisted = true;
        break;
      }
      Thread.sleep(500);
    }

    assertThat(persisted).as("Log should be persisted in MongoDB").isTrue();
  }
}
