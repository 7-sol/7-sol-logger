/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger.util;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

/**
 * Represents an audit log document persisted in MongoDB.
 *
 * @param id The unique identifier of the log entry in MongoDB.
 * @param action Semantic name of the action being performed.
 * @param status Current status of the operation (e.g., SUCCESS, FAILURE).
 * @param traceId Identifier used to correlate this request.
 * @param userId Identifier of the user initiating the request.
 * @param podUid Unique identifier for the Kubernetes Pod instance.
 * @param timestamp Timestamp when the audit record was first initialized.
 * @param logs List of log entries captured during the operation.
 */
//@Document(collection = "audit_logs")
@SuppressFBWarnings("EI_EXPOSE_REP")
public record AuditLogDto(
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    ObjectId id,
    String action,
    String traceId,
    String spanId,
    String status,
    String userId,
    String dbOid,
    String podUid,
    String requestorIp,
    Instant timestamp,
    List<LogEntry> logs
) {
  /**
   * Canonical constructor for AuditLog.
   *
   * @param id The unique identifier.
   * @param action The action name.
   * @param status The status.
   * @param traceId The correlation identifier.
   * @param userId The user identifier.
   * @param podUid The pod identifier.
   * @param timestamp The record timestamp.
   * @param logs The list of log entries.
   */
  public AuditLogDto {
    logs = (logs == null) ? List.of() : List.copyOf(logs);
    timestamp = (timestamp == null) ? Instant.now() : timestamp;
    id = (id == null) ? new ObjectId() : id;
  }
}
