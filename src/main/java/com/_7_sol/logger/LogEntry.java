/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import java.time.Instant;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

/**
 * Represents an individual log statement captured within an operation.
 *
 * @param timestamp Time when this specific log was recorded.
 * @param level Severity level (e.g., INFO, ERROR).
 * @param message The log message content.
 */
@RegisterReflectionForBinding
public record LogEntry(
    Instant timestamp,
    String level,
    String message
) {}
