/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import java.time.Instant;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents an audit log document persisted in MongoDB.
 *
 * @param id The unique identifier of the log entry.
 * @param correlationId The correlation ID for tracing.
 * @param userId The ID of the user who initiated the action.
 * @param podUid The UID of the pod where the action occurred.
 * @param createdAt The timestamp when the log was created.
 * @param payload The nested action details.
 */
@Document(collection = "audit_logs")
@RegisterReflectionForBinding
public record AuditLog(
    @Id String id,
    String correlationId,
    String userId,
    String podUid,
    @Indexed Instant createdAt,
    AuditLogPayload payload
) {}
