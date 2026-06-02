/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger.util;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for persisting and querying {@link AuditLog} documents.
 */
@Repository
public interface AuditLogRepository
    extends MongoRepository<AuditLog, ObjectId> {}
