/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import com._7_sol.logger.util.AuditLog;
import com._7_sol.logger.util.AuditLogRepository;
import com._7_sol.logger.util.LogAction;
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
    AuditLog auditLog = new AuditLog();
    auditLog.setTraceId("corr-1");
    auditLog.setStatus("SUCCESS");
    auditLog.setUserId("user-1");
    auditLog.setDbOid("dbId-1");
    auditLog.setPodUid("pod-1");
    auditLog.setRequestorIp("192.168.1.100");
    auditLog.setTimestamp(Instant.now());
    
    LogAction action = new LogAction(
            "CREATE_ORDER",
            List.of(new LogEntry(Instant.now(), "INFO", "span-1", "Order created"))
    );
    auditLog.setLogs(List.of(action));

    AuditLog saved = repository.save(auditLog);
    assertThat(saved.getId()).isNotNull();

    List<AuditLog> all = repository.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.getFirst().getTraceId()).isEqualTo("corr-1");
    assertThat(all.getFirst().getLogs()).hasSize(1);
    assertThat(all.getFirst().getLogs().getFirst().action()).isEqualTo("CREATE_ORDER");
  }
}
