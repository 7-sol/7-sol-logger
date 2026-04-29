/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Nats;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

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
//    String natsUrl = "nats://" + nats.getHost() + ":"
//        + nats.getMappedPort(4222);
    
    AuditLog logEntry = new AuditLog(
        "msg-id-1",
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

    // Give the application some time to process the message and buffer
    Thread.sleep(5000);

    // Verify
    StepVerifier.create(repository.findById("msg-id-1"))
        .expectSubscription()
        .expectNextMatches(log -> log.correlationId().equals("nats-corr-1"))
        .verifyComplete();
  }

  static String natsUrl(){
    return "nats://" + nats.getHost() + ":" + nats.getMappedPort(4222);
  }
}
