/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger.config;

import static org.assertj.core.api.Assertions.assertThat;

import com._7_sol.logger.util.AuditLog;
import com._7_sol.logger.util.AuditLogDto;
import com._7_sol.logger.util.LogAction;
import com._7_sol.logger.util.LogEntry;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link AuditLogMapper}.
 */
@SpringBootTest
class AuditLogMapperTests {

    @Autowired
    private AuditLogMapper mapper;

    @Test
    void shouldNormalizeDtosWithSameTraceId() {
        String traceId = "trace-123";
        Instant now = Instant.now();
        
        AuditLogDto dto1 = new AuditLogDto(
                new ObjectId(), "ACTION_A", traceId, "span-1", "SUCCESS", "user-1", "db-1", "pod-1", "127.0.0.1", now,
                List.of(new LogEntry(now, "INFO", "span-1", "Message 1"))
        );
        AuditLogDto dto2 = new AuditLogDto(
                new ObjectId(), "ACTION_A", traceId, "span-2", "SUCCESS", "user-1", "db-1", "pod-1", "127.0.0.1", now,
                List.of(new LogEntry(now.plusMillis(100), "DEBUG", "span-2", "Message 2"))
        );
        AuditLogDto dto3 = new AuditLogDto(
                new ObjectId(), "ACTION_B", traceId, "span-3", "SUCCESS", "user-1", "db-1", "pod-1", "127.0.0.1", now,
                List.of(new LogEntry(now.plusMillis(200), "WARN", "span-3", "Message 3"))
        );

        List<AuditLog> result = mapper.toNormalizedList(List.of(dto1, dto2, dto3));

        assertThat(result).hasSize(1);
        AuditLog auditLog = result.getFirst();
        assertThat(auditLog.getTraceId()).isEqualTo(traceId);
        assertThat(auditLog.getLogs()).hasSize(2); // ACTION_A and ACTION_B

        LogAction actionA = auditLog.getLogs().stream()
                .filter(a -> "ACTION_A".equals(a.action()))
                .findFirst().orElseThrow();
        assertThat(actionA.logEntry()).hasSize(2);
        assertThat(actionA.logEntry().get(0).message()).isEqualTo("Message 1");
        assertThat(actionA.logEntry().get(1).message()).isEqualTo("Message 2");

        LogAction actionB = auditLog.getLogs().stream()
                .filter(a -> "ACTION_B".equals(a.action()))
                .findFirst().orElseThrow();
        assertThat(actionB.logEntry()).hasSize(1);
        assertThat(actionB.logEntry().getFirst().message()).isEqualTo("Message 3");
    }

    @Test
    void shouldAssignUniqueTraceIdWhenMissing() {
        AuditLogDto dto1 = new AuditLogDto(
                new ObjectId(), "ACTION_A", null, "span-1", "SUCCESS", "user-1", "db-1", "pod-1", "127.0.0.1", Instant.now(),
                List.of(new LogEntry(Instant.now(), "INFO", "span-1", "Message 1"))
        );
        AuditLogDto dto2 = new AuditLogDto(
                new ObjectId(), "ACTION_B", "", "span-2", "SUCCESS", "user-1", "db-1", "pod-1", "127.0.0.1", Instant.now(),
                List.of(new LogEntry(Instant.now(), "INFO", "span-2", "Message 2"))
        );

        List<AuditLog> result = mapper.toNormalizedList(List.of(dto1, dto2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTraceId()).isNotBlank();
        assertThat(result.get(1).getTraceId()).isNotBlank();
        assertThat(result.get(0).getTraceId()).isNotEqualTo(result.get(1).getTraceId());
        
        // Verify UUID format
        assertThat(UUID.fromString(result.get(0).getTraceId())).isNotNull();
        assertThat(UUID.fromString(result.get(1).getTraceId())).isNotNull();
    }

    @Test
    void shouldSortLogsChronologically() {
        String traceId = "trace-sort";
        Instant now = Instant.now();

        // DTO with a later action but earlier log entry
        AuditLogDto dtoLaterAction = new AuditLogDto(
                new ObjectId(), "LATER_ACTION", traceId, "span-1", "SUCCESS", "user-1", "db-1", "pod-1", "127.0.0.1", now,
                List.of(new LogEntry(now.minusSeconds(10), "INFO", "span-1", "Earlier Log"))
        );

        // DTO with an earlier action but later log entry
        AuditLogDto dtoEarlierAction = new AuditLogDto(
                new ObjectId(), "EARLIER_ACTION", traceId, "span-2", "SUCCESS", "user-1", "db-1", "pod-1", "127.0.0.1", now,
                List.of(new LogEntry(now, "INFO", "span-2", "Later Log"))
        );

        List<AuditLog> result = mapper.toNormalizedList(List.of(dtoLaterAction, dtoEarlierAction));

        assertThat(result).hasSize(1);
        List<LogAction> logs = result.getFirst().getLogs();
        assertThat(logs).hasSize(2);

        // LATER_ACTION should come first because its log entry is earlier
        assertThat(logs.get(0).action()).isEqualTo("LATER_ACTION");
        assertThat(logs.get(1).action()).isEqualTo("EARLIER_ACTION");
    }

    @Test
    void shouldReturnEmptyListForNullOrEmptyInput() {
        assertThat(mapper.toNormalizedList(null)).isEmpty();
        assertThat(mapper.toNormalizedList(List.of())).isEmpty();
    }
}
