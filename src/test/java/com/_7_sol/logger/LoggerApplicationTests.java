/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Basic application context loading tests.
 */
@SpringBootTest
@Testcontainers
class LoggerApplicationTests {

  @Container
  static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

  @Container
  static GenericContainer<?> nats = new GenericContainer<>("nats:latest")
      .withExposedPorts(4222);

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
    registry.add("nats.url", NatsIngestionIntegrationTests::natsUrl);
  }

  /**
   * Verifies that the application context loads successfully.
   */
  @Test
  void contextLoads() {
    // Basic check to ensure the Spring context starts without errors.
  }
}
