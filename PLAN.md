# Project Conversion Plan: Reactive to Blocking [COMPLETED]

This plan outlines the steps to convert the Log Ingestion Server from
Spring Boot WebFlux (Reactive) to Spring Boot Web (Blocking) using
Java 25 features like Virtual Threads for high throughput.

## Phase 1: Dependency Refactoring [DONE]
1.  **Modify `pom.xml`:** [DONE]
    *   Replace `spring-boot-starter-data-mongodb-reactive` with
        `spring-boot-starter-data-mongodb`.
    *   Replace `spring-boot-starter-webflux` with
        `spring-boot-starter-web`.
    *   Remove `reactor-test`.
2.  **Enable Virtual Threads:** [DONE]
    *   Add `spring.threads.virtual.enabled=true` to `application.yaml`
        to leverage Java 25's efficient concurrency model.

## Phase 2: Core Logic Conversion [DONE]
1.  **Repository Update (`AuditLogRepository.java`):** [DONE]
    *   Change inheritance from `ReactiveMongoRepository` to
        `MongoRepository`.
2.  **Configuration Update (`MongoConfig.java`):** [DONE]
    *   Switch `ReactiveMongoTemplate` to `MongoTemplate`.
    *   Update `initIndexes` to use blocking calls.
3.  **Service Update (`NatsSubscriber.java`):** [DONE]
    *   Remove Project Reactor dependencies (`Sinks`, `Flux`, etc.).
    *   Implement a blocking batching mechanism using a
        `BlockingQueue` with a dedicated Virtual Thread.
    *   Use `MongoTemplate.insertAll()` for batch persistence.

## Phase 3: Test Conversion & Verification [DONE]
1.  **Update Test Suite:** [DONE]
    *   Refactor `AuditLogRepositoryTests` to use standard blocking
        assertions instead of `StepVerifier`.
    *   Refactor `NatsIngestionIntegrationTests` to verify data
        persistence using blocking repository calls.
2.  **Verify Build:** [DONE]
    *   Run `mvn clean compile` to check for type errors.
    *   Run `mvn test` to ensure functional parity.

## Phase 4: Final Cleanup [DONE]
1.  **Remove Unused Imports:** [DONE]
    *   Strip all `reactor.*` and `org.reactivestreams.*` imports.
2.  **Documentation:** [DONE]
    *   Update Javadoc and banner to reflect blocking behavior.