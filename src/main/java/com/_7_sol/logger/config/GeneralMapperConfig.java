/*
 * Copyright (c) 2026 7-Sol. All rights reserved.
 *
 * This software is the proprietary information of 7-Sol.
 * Use is subject to license terms.
 */
package com._7_sol.logger.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Central MapStruct configuration interface for all mappers in the authentication system.
 * <p>
 * This configuration interface defines common mapping settings that are inherited by all
 * MapStruct mapper interfaces within the application. It establishes consistent behavior
 * for handling unmapped properties and integrates mappers with Spring's dependency injection.
 * </p>
 * <p>
 * Configuration details:
 * <ul>
 *   <li><b>unmappedSourcePolicy:</b> IGNORE - Unmapped source properties will not generate warnings or errors</li>
 *   <li><b>unmappedTargetPolicy:</b> IGNORE - Unmapped target properties will not generate warnings or errors</li>
 *   <li><b>componentModel:</b> spring - Mappers are generated as Spring beans for dependency injection</li>
 * </ul>
 * </p>
 * <p>
 * Usage: Apply this configuration to mapper interfaces using {@code @Mapper(config = CentralMapperConfig.class)}
 * to ensure consistent mapping behavior across the application.
 * </p>
 *
 * @see org.mapstruct.MapperConfig
 * @see org.mapstruct.Mapper
 */
@MapperConfig(
    componentModel = "spring", // Makes it a Spring Bean
    unmappedSourcePolicy = ReportingPolicy.IGNORE, // Ignores extra fields in MenuItem
    unmappedTargetPolicy = ReportingPolicy.IGNORE  // Ignores extra fields in MenuDto
)
public interface GeneralMapperConfig {
}
