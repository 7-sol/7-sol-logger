/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger.util;

import java.time.Instant;

/**
 * Represents an individual log statement captured within an operation.
 *
 * @param timestamp Time when this specific log was recorded.
 * @param level Severity level (e.g., INFO, ERROR).
 * @param message The log message content.
 */
public record LogEntry(
    Instant timestamp,
    String level,
    String message
) {}
