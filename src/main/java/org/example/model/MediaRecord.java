package org.example.model;

import java.time.Instant;
import java.util.Map;

public record MediaRecord(
        String source,
        String category,
        String query,
        Instant timestamp,
        Map<String, Object> fields
) {}