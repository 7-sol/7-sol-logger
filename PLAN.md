> **Role:** Senior Reactive Java Architect and DevOps Engineer.
>
> **Context:** You are building the "Receiver" (Log Ingestion Server) for a
> Consolidated Audit Logging framework. This server subscribes to NATS, consumes
> JSON audit logs, persists them to MongoDB, and is deployed as a GraalVM Native
> Image to GKE Autopilot via a 3-tier CI/CD pipeline.
>
> **Task:** Generate the complete Spring Boot WebFlux codebase and CI/CD config.
>
> **Strict Documentation & Formatting Rules:**
> 1.  **Line Length:** EVERY line of code and documentation MUST be strictly
      >     less than 81 characters.
> 2.  **Copyright:** Every Java file MUST start with this placeholder:
      >     `/* Copyright (c) 2026 7-Sol. All rights reserved. */`
> 3.  **Javadoc:** EVERY class, interface, field, and method MUST have
      >     comprehensive Javadoc explaining its purpose, parameters, and returns.
> 4.  **Package:** All Java code MUST reside under `com._7_sol.logger`.
>
> **Strict Code Quality & Anti-Smell Rules:**
> 1.  **Immutability & DI:** Use Java `record`. Use `@RequiredArgsConstructor`.
> 2.  **No Magic Values:** Extract all hardcoded values to `private static final`.
> 3.  **Reactive Purity:** Never use `.block()`.
>
> **Technology Stack:**
> * **Java:** 25, Spring Boot WebFlux (Latest Stable).
> * **Database:** MongoDB 8.2 (Reactive).
> * **Messaging:** NATS (`jnats`).
> * **Testing:** JUnit 5, StepVerifier, Mockito, Testcontainers (Target: 95%).
>
> **Specific Implementation Requirements:**
>
> 1.  **Application Config & TTL:**
      >     * Configure K8s-injected env vars for `${MONGO_URI}`, `${MONGO_USER}`, etc.
>     * Define `app.logging.retention-days` (default 90).
>     * Create a startup `@Configuration` to programmatically apply a TTL index
        >         (`expireAfterSeconds`) to the `createdAt` field in MongoDB.
> 2.  **Data Model & NATS Ingestion:**
      >     * Create `@Document(collection = "audit_logs")` mapping the JSON envelope
              >         (`correlationId`, `userId`, `podUid`) and nested payload.
>     * Ensure the Jackson `ObjectMapper` registers the `JavaTimeModule`.
>     * Deserialize messages and save to Mongo. Catch all errors to protect
        >         the listener thread.
> 3.  **High-Throughput Batching:**
      >     * Use `Flux.bufferTimeout()` (e.g., 500 items or 2s) for incoming messages.
>     * Use `ReactiveMongoTemplate.insertAll()` for bulk inserts.
> 4.  **Observability & Graceful Shutdown:**
      >     * Enable Actuator `/health` and `/prometheus`. Enable graceful shutdown.
>     * Ensure `@PreDestroy` gracefully drains the NATS dispatcher.
> 5.  **GraalVM Native Image Hints:**
      >     * Use `@RegisterReflectionForBinding` on all data models/records to ensure
              >         Jackson can deserialize the incoming NATS JSON in the native binary.
> 6.  **Integration Testing:**
      >     * Use Testcontainers (Mongo 8.2, NATS) to verify end-to-end insertion.
>
> **GraalVM & CI/CD Pipeline Requirements:**
>
> **GraalVM & CI/CD Pipeline Requirements:**
>
> 7.  **Helm Chart (`charts/log-receiver`):**
      >     * Include `isLocalDev` flag.
>     * **Dynamic Routing (ConfigMaps/Secrets):** Do NOT hardcode Mongo OR NATS
        >         URIs in `values-*.yaml`. The Deployment MUST map `MONGO_URI`,
        >         `NATS_URI`, and `NATS_SUBJECT` using `valueFrom: configMapKeyRef`.
        >         Use `secretKeyRef` for all passwords/tokens.
>     * **Environment Sizing (`values-*.yaml`):** Expose `replicaCount` and
        >         resource requests/limits. Dev should default to 1 replica (low RAM),
        >         Prod should default to 3+ replicas (higher RAM for batching).
>     * **Probes:** Include `startupProbe`, `livenessProbe`, `readinessProbe`
        >         pointing to the Actuator endpoints on the management port (8081).
>
> 8.  **Observability & Actuator Security:**
      >     * Enable `/health`, `/prometheus`, and `/loggers` endpoints.
>     * **Security:** Configure `management.server.port` to a different port
        >         (e.g., 8081) than the main application port (e.g., 8080) to ensure
        >         dynamic log levels cannot be exposed to the public internet.
> 9.  **Build & Quality Enforcement Tools (`pom.xml`):**
      >     * **JaCoCo Plugin:** Configure `jacoco-maven-plugin` with a `check` goal
              >         that strictly enforces a minimum line and branch coverage of `0.95`.
              >         The build MUST fail if coverage is not met.
>     * **Checkstyle Plugin:** Configure `maven-checkstyle-plugin` to fail the
        >         build if any line length exceeds 81 characters, or if standard Java
        >         naming conventions are violated.
>     * **SpotBugs Plugin:** Add `spotbugs-maven-plugin` to the build phase to
        >         statically analyze the code and fail on high-priority code smells.
>
> **Output Needed:**
> Provide `pom.xml`, Java source files, tests, `skaffold.yaml`, `cloudbuild.yaml`,
> `clouddeploy.yaml`, and the Helm chart templates/values files.