package org.example.util;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonUtils {

    public static String text(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? "" : node.asText();
    }

    public static Double number(JsonNode node) {
        return node != null && node.isNumber() ? node.asDouble() : null;
    }

    public static Integer integer(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.asInt();
    }
}