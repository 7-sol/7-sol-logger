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
> 7.  **Skaffold Orchestration (`skaffold.yaml`):**
      >     * Use the Paketo Jammy Tiny builder for GraalVM Native Images.
>     * Set `BP_NATIVE_IMAGE=true` and `BP_NATIVE_IMAGE_BUILD_ARGUMENTS: "-Ob"`.
>     * **Profile `dev`:** Namespace `dev`, load `values-dev.yaml` (Minikube).
>     * **Profile `beta`:** Namespace `beta`, load `values-beta.yaml`, set
        >         `build.local.push: true` (Artifact Registry).
>     * **Profile `prod`:** Namespace `prod`, load `values-prod.yaml`, use
        >         `googleCloudBuild` with `machineType: E2_HIGHMEM_8`.
> 8.  **Helm Chart (`charts/log-receiver`):**
      >     * Include `isLocalDev` flag and use `secretKeyRef` for sensitive data.
>     * Inject `SPRING_PROFILES_ACTIVE`.
>     * **GKE Autopilot Needs:** Deployment MUST define CPU and Memory requests
        >         and limits.
>     * **Probes:** Deployment MUST include `startupProbe`, `livenessProbe`, and
        >         `readinessProbe` pointing to the Actuator endpoints.
>     * **DB Routing (Values):**
        >         * `values-dev.yaml`: `mongodb-service.dev-mongo.svc.cluster.local`
>         * `values-beta.yaml` & `prod`: `mongodb-service.mongo.svc.cluster.local`
> 9.  **Cloud Deploy & Build:**
      >     * Output `artifacts.json` via Skaffold. Provide `cloudbuild.yaml` and
              >         `clouddeploy.yaml` for GCP promotion.
>
> **Output Needed:**
> Provide `pom.xml`, Java source files, tests, `skaffold.yaml`, `cloudbuild.yaml`,
> `clouddeploy.yaml`, and the Helm chart templates/values files.