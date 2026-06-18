/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger.util;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;


@Document(collection = "audit_logs")
@SuppressFBWarnings("EI_EXPOSE_REP")
@Data
public class AuditLog {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId id;
    private String traceId;
    private String status;
    private String userId;
    private String dbOid;
    private String podUid;
    private String requestorIp;
    private Instant timestamp;
    private List<LogAction> logs = List.of();
}
