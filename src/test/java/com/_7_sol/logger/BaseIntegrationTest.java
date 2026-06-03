/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests providing shared Testcontainers.
 */
@SpringBootTest
@Testcontainers
public abstract class BaseIntegrationTest {

  @Container
  static final MongoDBContainer mongo = new MongoDBContainer("mongo:8.0");

  @Container
  static final GenericContainer<?> nats = new GenericContainer<>("nats:latest")
      .withExposedPorts(4222)
      .waitingFor(Wait.forLogMessage(".*Server is ready.*", 1));

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
    registry.add("nats.url", BaseIntegrationTest::natsUrl);
  }

  protected static String natsUrl() {
    return "nats://" + nats.getHost() + ":" + nats.getMappedPort(4222);
  }
}
