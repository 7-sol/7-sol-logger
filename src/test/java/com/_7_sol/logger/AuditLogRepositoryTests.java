/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import com._7_sol.logger.util.AuditLog;
import com._7_sol.logger.util.AuditLogRepository;
import com._7_sol.logger.util.LogEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for {@link AuditLogRepository} using Testcontainers.
 */
class AuditLogRepositoryTests extends BaseIntegrationTest {

  @Autowired
  private AuditLogRepository repository;

  /**
   * Tests saving and retrieving an audit log.
   */
  @Test
  void shouldSaveAndRetrieveAuditLog() {
    AuditLog logEntry = new AuditLog(
        null,
        "CREATE_ORDER",
        "op-id",
        "rel-id",
        "test-action",
        "SUCCESS",
        "user-1",
        "dbId-1",
        "pod-1",
        "192.168.1.100",
        Instant.now(),
        List.of(new LogEntry(Instant.now(), "INFO", "Order created"))
    );

    AuditLog saved = repository.save(logEntry);
    assertThat(saved.id()).isNotNull();

    List<AuditLog> all = repository.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.get(0).traceId()).isEqualTo("corr-1");
  }
}
