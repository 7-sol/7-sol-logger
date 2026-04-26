/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Reactive repository for persisting and querying {@link AuditLog} documents.
 */
@Repository
public interface AuditLogRepository
    extends ReactiveMongoRepository<AuditLog, String> {}
