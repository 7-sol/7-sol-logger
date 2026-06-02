/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import static org.assertj.core.api.Assertions.assertThat;

import com._7_sol.logger.util.AuditLog;
import com._7_sol.logger.util.AuditLogRepository;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * End-to-end integration tests for NATS log ingestion.
 */
@SpringBootTest
@Testcontainers
class NatsIngestionIntegrationTests {

  @Container
  static MongoDBContainer mongo = new MongoDBContainer("mongo:8.0");

  @Container
  static GenericContainer<?> nats = new GenericContainer<>("nats:latest")
      .withExposedPorts(4222);

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
    registry.add("nats.url", NatsIngestionIntegrationTests::natsUrl);
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
    AuditLog logEntry = new AuditLog(
        testId,
        "op-id-1",
        "UPDATE_PROFILE",
        "SUCCESS",
        "nats-corr-1",
        "user-2",
        "pod-2",
        Instant.now(),
        List.of(new LogEntry(Instant.now(), "INFO", "Profile updated"))
    );
    
    // Connect, publish and wait
    try (Connection conn = Nats.connect(natsUrl())) {
      conn.publish("test.audit.logs", objectMapper.writeValueAsBytes(logEntry));
      conn.flush(Duration.ofSeconds(2));
    }

    // Poll repository until message is persisted or timeout reached
    boolean persisted = false;
    for (int i = 0; i < 20; i++) {
      Optional<AuditLog> found = repository.findById(testId);
      if (found.isPresent()) {
        assertThat(found.get().correlationId()).isEqualTo("nats-corr-1");
        persisted = true;
        break;
      }
      Thread.sleep(500);
    }
    
    assertThat(persisted).as("Log should be persisted in MongoDB").isTrue();
  }

  static String natsUrl(){
    return "nats://" + nats.getHost() + ":" + nats.getMappedPort(4222);
  }
}
