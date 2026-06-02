/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import com._7_sol.logger.config.LoggerHint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.annotation.Primary;

/**
 * Main entry point for the Log Ingestion Server application.
 *
 * <p>This application subscribes to NATS, consumes JSON audit logs,
 * and persists them to MongoDB in batches.</p>
 */
@SpringBootApplication
@ImportRuntimeHints({
    LoggerHint.class
})
public class LoggerApplication {

  /**
   * Main method to launch the Spring Boot application.
   *
   * @param args Command line arguments.
   */
  public static void main(final String[] args) {
    SpringApplication.run(LoggerApplication.class, args);
  }

  /**
   * Provides a primary ObjectMapper bean with Java 8 time support.
   *
   * @return The configured ObjectMapper.
   */
  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper;
  }
}
