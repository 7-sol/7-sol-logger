/* Copyright (c) 2026 7-Sol. All rights reserved. */
package com._7_sol.logger.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * Custom Jackson deserializer for converting JSON string representations into MongoDB ObjectId instances.
 *
 * <p>This deserializer handles the conversion of hexadecimal string values from JSON into
 * BSON ObjectId objects. It safely handles null and empty string values by returning null
 * instead of throwing exceptions.</p>
 */
public class ObjectIdDeserializer extends JsonDeserializer<ObjectId> {

    /**
     * Deserializes a JSON string value into a MongoDB ObjectId.
     *
     * @param p The JSON parser providing access to the current token value.
     * @param ctxt The deserialization context (unused but required by Jackson API).
     * @return A new ObjectId instance created from the hex string, or null if the input is null or empty.
     * @throws IOException If an error occurs during JSON parsing.
     */
    @Override
    public ObjectId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // Extract the string value from the current JSON token (expected to be a hex string)
        String hex = p.getValueAsString();

        // Return null for null or empty strings; otherwise create a new ObjectId from the hex string
        return (hex == null || hex.isEmpty()) ? null : new ObjectId(hex);
    }
}
