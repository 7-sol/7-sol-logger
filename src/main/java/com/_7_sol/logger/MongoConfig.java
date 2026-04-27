/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/**
 * Configuration for MongoDB, including TTL index setup.
 */
@Configuration
public class MongoConfig {

  private final ReactiveMongoTemplate mongoTemplate;

  @Value("${APP_LOGGING_RETENTION_DAYS:90}")
  private int retentionDays;

  /**
   * Constructs MongoConfig with the required template.
   *
   * @param mongoTemplate The reactive mongo template.
   */
  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public MongoConfig(final ReactiveMongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  /**
   * Initializes the TTL index for the audit_logs collection.
   */
  @PostConstruct
  public void initIndexes() {
    mongoTemplate.indexOps("audit_logs")
        .createIndex(new Index()
            .on("timestamp", org.springframework.data.domain.Sort.Direction.ASC)
            .expire(Duration.ofDays(retentionDays)))
        .subscribe();
  }
}
