/*
 * Copyright (c) 2026 7-Sol. All rights reserved.
 *
 * This software is the proprietary information of 7-Sol.
 * Use is subject to license terms.
 */
package com._7_sol.logger.config;

import com._7_sol.logger.util.AuditLog;
import com._7_sol.logger.util.AuditLogDto;
import com._7_sol.logger.util.LogAction;
import com._7_sol.logger.util.LogEntry;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting and normalizing AuditLogDto to AuditLog.
 * Manually implemented to avoid MapStruct generation issues in this environment.
 */
@Component
public class AuditLogMapper {

    /**
     * Normalizes a list of AuditLogDto objects into a consolidated list of AuditLog entities.
     *
     * <p>Normalization rules:
     * <ul>
     *   <li>DTOs with the same traceId are consolidated into a single AuditLog.</li>
     *   <li>DTOs with null or empty traceId are assigned a unique UUID and treated as separate AuditLogs.</li>
     *   <li>Within an AuditLog, LogEntry objects with the same action are grouped into a single LogAction.</li>
     * </ul>
     *
     * @param dtos The list of DTOs to normalize.
     * @return A list of normalized AuditLog entities.
     */
    public List<AuditLog> toNormalizedList(List<AuditLogDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }

        // 1. Pre-process: Assign unique UUIDs to DTOs with missing traceId
        List<AuditLogDto> processedDtos = dtos.stream()
                .map(dto -> {
                    if (dto.traceId() == null || dto.traceId().isBlank()) {
                        return new AuditLogDto(
                                dto.id(),
                                dto.action(),
                                UUID.randomUUID().toString(),
                                dto.spanId(),
                                dto.status(),
                                dto.userId(),
                                dto.dbOid(),
                                dto.podUid(),
                                dto.requestorIp(),
                                dto.timestamp(),
                                dto.logs()
                        );
                    }
                    return dto;
                })
                .toList();

        // 2. Group by traceId and map to AuditLog
        return processedDtos.stream()
                .collect(Collectors.groupingBy(AuditLogDto::traceId))
                .entrySet().stream()
                .map(entry -> mapGroupToAuditLog(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * Maps a group of DTOs with the same traceId to a single AuditLog.
     */
    private AuditLog mapGroupToAuditLog(String traceId, List<AuditLogDto> group) {
        AuditLogDto first = group.getFirst();
        AuditLog auditLog = new AuditLog();
        auditLog.setTraceId(traceId);
        auditLog.setUserId(first.userId());
        auditLog.setStatus(first.status());
        auditLog.setDbOid(first.dbOid());
        auditLog.setPodUid(first.podUid());
        auditLog.setRequestorIp(first.requestorIp());
        auditLog.setTimestamp(first.timestamp());

        // Group logs by action and sort them
        List<LogAction> logActions = group.stream()
                .collect(Collectors.groupingBy(AuditLogDto::action))
                .entrySet().stream()
                .map(actionEntry -> new LogAction(
                        actionEntry.getKey(),
                        actionEntry.getValue().stream()
                                .flatMap(dto -> dto.logs().stream())
                                .sorted(Comparator.comparing(LogEntry::timestamp))
                                .toList()
                ))
                .sorted(Comparator.comparing(la -> la.logEntry().isEmpty()
                        ? java.time.Instant.MIN
                        : la.logEntry().getFirst().timestamp()))
                .toList();

        auditLog.setLogs(logActions);
        return auditLog;
    }
}
