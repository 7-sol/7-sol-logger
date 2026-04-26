/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

/**
 * Represents the nested payload of an audit log entry.
 *
 * @param action The action performed.
 * @param status The status of the action.
 * @param details Additional details about the action.
 */
@RegisterReflectionForBinding
public record AuditLogPayload(
    String action,
    String status,
    String details
) {}
