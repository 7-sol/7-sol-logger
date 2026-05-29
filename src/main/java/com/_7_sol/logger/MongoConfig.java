/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/**
 * Configuration for MongoDB, including TTL index setup.
 */
@Configuration
@RequiredArgsConstructor
public class MongoConfig {

  private final MongoTemplate mongoTemplate;

  @Value("${app.logging.retention.days:90}")
  private int retentionDays;

  /**
   * Initializes the TTL index for the audit_logs collection.
   */
  @PostConstruct
  public void initIndexes() {
    mongoTemplate.indexOps("audit_logs")
        .ensureIndex(new Index()
            .on("timestamp", org.springframework.data.domain.Sort.Direction.ASC)
            .expire(Duration.ofDays(retentionDays)));
  }
}
