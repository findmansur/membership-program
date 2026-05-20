package com.firstclub.membership.benefit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * Centralised, defensive parsing of benefit configuration JSON. Keeping all
 * JSON access here means no Benefit implementation has to defend itself
 * against null configs, malformed JSON, or unexpected types.
 */
@Component
public class BenefitConfigParser {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public BenefitConfigParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> parse(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Object> map = objectMapper.readValue(json, MAP_TYPE);
            return map == null ? Collections.emptyMap() : map;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid benefit config JSON: " + e.getMessage(), e);
        }
    }

    public static Number requireNumber(Map<String, Object> config, String key) {
        Object v = config.get(key);
        if (!(v instanceof Number n)) {
            throw new IllegalArgumentException("Benefit config missing numeric key: " + key);
        }
        return n;
    }

    public static Number numberOrDefault(Map<String, Object> config, String key, Number defaultValue) {
        Object v = config.get(key);
        return (v instanceof Number n) ? n : defaultValue;
    }

    public static String stringOrDefault(Map<String, Object> config, String key, String defaultValue) {
        Object v = config.get(key);
        return (v instanceof String s) ? s : defaultValue;
    }
}
