/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;

/**
 * Configuration for MongoDB, including TTL index setup.
 */
@Configuration
public class MongoConfig {

  private final ReactiveMongoTemplate mongoTemplate;

  @Value("${app.logging.retention-days:90}")
  private int retentionDays;

  /**
   * Constructs MongoConfig with the required template.
   *
   * @param mongoTemplate The reactive mongo template.
   */
  public MongoConfig(final ReactiveMongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  /**
   * Initializes the TTL index for the audit_logs collection.
   */
  @PostConstruct
  public void initIndexes() {
    ReactiveIndexOperations indexOps = mongoTemplate.indexOps(AuditLog.class);
    Index ttlIndex = new Index()
        .on("createdAt", org.springframework.data.domain.Sort.Direction.ASC)
        .expire(Duration.ofDays(retentionDays));

    indexOps.ensureIndex(ttlIndex).subscribe();
  }
}
