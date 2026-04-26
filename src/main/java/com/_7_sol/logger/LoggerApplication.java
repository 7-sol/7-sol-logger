/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Log Ingestion Server application.
 *
 * <p>This application subscribes to NATS, consumes JSON audit logs,
 * and persists them to MongoDB in batches.</p>
 */
@SpringBootApplication
public class LoggerApplication {

  /**
   * Main method to launch the Spring Boot application.
   *
   * @param args Command line arguments.
   */
  public static void main(final String[] args) {
    SpringApplication.run(LoggerApplication.class, args);
  }
}
