/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

/**
 * Integration tests for {@link AuditLogRepository} using Testcontainers.
 */
@DataMongoTest
@Testcontainers
class AuditLogRepositoryTests {

  @Container
  static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
  }

  @Autowired
  private AuditLogRepository repository;

  /**
   * Tests saving and retrieving an audit log.
   */
  @Test
  void shouldSaveAndRetrieveAuditLog() {
    AuditLog logEntry = new AuditLog(
        null,
        "op-1",
        "CREATE_ORDER",
        "SUCCESS",
        "corr-1",
        "user-1",
        "pod-1",
        Instant.now(),
        List.of(new LogEntry(Instant.now(), "INFO", "Order created"))
    );

    StepVerifier.create(repository.save(logEntry))
        .expectNextMatches(saved -> saved.id() != null)
        .verifyComplete();

    StepVerifier.create(repository.findAll())
        .expectNextMatches(found -> found.correlationId().equals("corr-1"))
        .verifyComplete();
  }
}
