package com.qoocca.teachers.api.academy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoocca.teachers.api.global.config.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiHeaderMappingClient {

    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redis;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final Duration HEADER_CACHE_TTL = Duration.ofHours(24);
    private static final Duration COOLDOWN_TTL = Duration.ofSeconds(10);

    private static final String CACHE_PREFIX = "ai:headerMap:v1:";
    private static final String COOLDOWN_PREFIX = "ai:headerMap:cooldown:v1:";
    private static final String EXTENDED_CACHE_PREFIX = "ai:headerMap:extended:v1:";
    private static final String EXTENDED_COOLDOWN_PREFIX = "ai:headerMap:extended:cooldown:v1:";
    private static final String RELATION_CACHE_PREFIX = "ai:parentRel:v1:";

    public Map<String, String> mapHeaders(Long academyId, List<String> headers) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            log.warn("OpenAI API key not configured");
            return Map.of();
        }
        if (headers == null || headers.isEmpty()) return Map.of();

        // 1) 캐시 조회
        String cacheKey = CACHE_PREFIX + sha256(String.join("|", headers));
        Map<String, String> cached = readCache(cacheKey);
        if (cached != null) {
            log.debug("Header mapping cache hit (model={}): {}", properties.getModel(), cached);
            return cached;
        }

        // 2) 쿨다운 (폭주 방지)
        if (academyId != null) {
            String coolKey = COOLDOWN_PREFIX + academyId;
            Boolean ok = redis.opsForValue().setIfAbsent(coolKey, "1", COOLDOWN_TTL);
            if (Boolean.FALSE.equals(ok)) {
                log.warn("AI header mapping skipped by cooldown (academyId={})", academyId);
                return Map.of(); // 여기서 룰 기반 fallback으로 이어도 됨
            }
        }

        // 3) OpenAI 호출
        Map<String, String> mapping;
        try {
            mapping = callOpenAI(headers);
        } catch (Exception e) {
            log.error("OpenAI header mapping failed", e);
            return Map.of();
        }

        // 4) 캐시 저장
        if (!mapping.isEmpty()) writeCache(cacheKey, mapping);

        return mapping;
    }

    public Map<Integer, String> mapExtendedHeaders(Long academyId, List<String> headers) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            log.warn("OpenAI API key not configured");
            return Map.of();
        }
        if (headers == null || headers.isEmpty()) return Map.of();

        String cacheKey = EXTENDED_CACHE_PREFIX + sha256(String.join("|", headers));
        Map<Integer, String> cached = readExtendedCache(cacheKey);
        if (cached != null) {
            log.debug("Extended header mapping cache hit (model={}): {}", properties.getModel(), cached);
            return cached;
        }

        if (academyId != null) {
            String coolKey = EXTENDED_COOLDOWN_PREFIX + academyId;
            Boolean ok = redis.opsForValue().setIfAbsent(coolKey, "1", COOLDOWN_TTL);
            if (Boolean.FALSE.equals(ok)) {
                log.warn("AI extended header mapping skipped by cooldown (academyId={})", academyId);
                return Map.of();
            }
        }

        Map<Integer, String> mapping;
        try {
            mapping = callOpenAIForExtendedHeaders(headers);
        } catch (Exception e) {
            log.error("OpenAI extended header mapping failed", e);
            return Map.of();
        }

        if (!mapping.isEmpty()) writeExtendedCache(cacheKey, mapping);

        return mapping;
    }

    public String classifyParentRelationship(Long academyId, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return null;
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            log.warn("OpenAI API key not configured");
            return null;
        }

        String cacheKey = RELATION_CACHE_PREFIX + sha256(rawValue.trim());
        String cached = readRelationCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        if (academyId != null) {
            String coolKey = RELATION_CACHE_PREFIX + "cooldown:" + academyId;
            Boolean ok = redis.opsForValue().setIfAbsent(coolKey, "1", COOLDOWN_TTL);
            if (Boolean.FALSE.equals(ok)) {
                log.warn("AI parent relationship skipped by cooldown (academyId={})", academyId);
                return null;
            }
        }

        String result;
        try {
            result = callOpenAIForParentRelationship(rawValue);
        } catch (Exception e) {
            log.error("OpenAI parent relationship failed", e);
            return null;
        }

        if (result != null && !result.isBlank()) {
            writeRelationCache(cacheKey, result);
        }
        return result;
    }

    private Map<String, String> readCache(String key) {
        try {
            String json = redis.opsForValue().get(key);
            if (json == null || json.isBlank()) return null;
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to read cache key={}", key, e);
            return null;
        }
    }

    private void writeCache(String key, Map<String, String> mapping) {
        try {
            String json = objectMapper.writeValueAsString(mapping);
            redis.opsForValue().set(key, json, HEADER_CACHE_TTL);
        } catch (Exception e) {
            log.warn("Failed to write cache key={}", key, e);
        }
    }

    private Map<Integer, String> readExtendedCache(String key) {
        try {
            String json = redis.opsForValue().get(key);
            if (json == null || json.isBlank()) return null;
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to read cache key={}", key, e);
            return null;
        }
    }

    private void writeExtendedCache(String key, Map<Integer, String> mapping) {
        try {
            String json = objectMapper.writeValueAsString(mapping);
            redis.opsForValue().set(key, json, HEADER_CACHE_TTL);
        } catch (Exception e) {
            log.warn("Failed to write cache key={}", key, e);
        }
    }

    private String readRelationCache(String key) {
        try {
            String value = redis.opsForValue().get(key);
            if (value == null || value.isBlank()) return null;
            return value;
        } catch (Exception e) {
            log.warn("Failed to read cache key={}", key, e);
            return null;
        }
    }

    private void writeRelationCache(String key, String value) {
        try {
            redis.opsForValue().set(key, value, HEADER_CACHE_TTL);
        } catch (Exception e) {
            log.warn("Failed to write cache key={}", key, e);
        }
    }

    private Map<String, String> callOpenAI(List<String> headers) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", properties.getModel());
        request.put("temperature", 0);
        request.put("max_tokens", 120);

        String prompt = "Map headers to fields (studentName, studentPhone, className). " +
                "Return ONLY a JSON object with keys studentName, studentPhone, className and string values.\n" +
                "Headers: " + headers;

        request.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(properties.getApiKey());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, httpHeaders);

        String url = properties.getBaseUrl() + "/chat/completions";
        String response = restTemplate.postForObject(url, entity, String.class);

        return parseMapping(response);
    }

    private Map<Integer, String> callOpenAIForExtendedHeaders(List<String> headers) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", properties.getModel());
        request.put("temperature", 0);
        request.put("max_tokens", 200);

        String indexedHeaders = buildIndexedHeaders(headers);
        String prompt = "For each header, assign a label from this list only: " +
                "[studentName, studentPhone, className, parentName, parentPhone, parentRelationship, cardNum, isPay, alarm, unknown]. " +
                "Return ONLY a JSON array of objects with fields index (int) and label (string). " +
                "Use the index from the input list below. Do not include extra text.\n" +
                "Headers with index: " + indexedHeaders;

        request.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(properties.getApiKey());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, httpHeaders);

        String url = properties.getBaseUrl() + "/chat/completions";
        String response = restTemplate.postForObject(url, entity, String.class);

        return parseExtendedMapping(response);
    }

    private String buildIndexedHeaders(List<String> headers) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(i).append(": ").append(headers.get(i));
        }
        return sb.toString();
    }

    private String callOpenAIForParentRelationship(String rawValue) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", properties.getModel());
        request.put("temperature", 0);
        request.put("max_tokens", 20);

        String prompt = "Classify the relationship into exactly one of: [\"모\", \"부\", \"조모\", \"조부\"]. " +
                "Return ONLY one of those values, no extra text.\n" +
                "Input: " + rawValue;

        request.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(properties.getApiKey());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, httpHeaders);

        String url = properties.getBaseUrl() + "/chat/completions";
        String response = restTemplate.postForObject(url, entity, String.class);

        return parseSingleLabel(response);
    }

    private Map<String, String> parseMapping(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) return Map.of();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.at("/choices/0/message/content");
            if (content.isMissingNode() || content.asText().isBlank()) return Map.of();

            JsonNode payload = readJsonObject(content.asText());
            if (payload == null) return Map.of();

            Map<String, String> out = new HashMap<>();
            put(out, "studentName", payload.path("studentName").asText(null));
            put(out, "studentPhone", payload.path("studentPhone").asText(null));
            put(out, "className", payload.path("className").asText(null));
            return out;
        } catch (Exception e) {
            log.error("Failed to parse OpenAI response", e);
            return Map.of();
        }
    }

    private Map<Integer, String> parseExtendedMapping(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) return Map.of();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.at("/choices/0/message/content");
            if (content.isMissingNode() || content.asText().isBlank()) return Map.of();

            JsonNode payload = readJsonArray(content.asText());
            if (payload == null || !payload.isArray()) return Map.of();

            Map<Integer, String> out = new HashMap<>();
            for (JsonNode node : payload) {
                if (!node.has("index") || !node.has("label")) continue;
                int idx = node.get("index").asInt(-1);
                String label = node.get("label").asText(null);
                if (idx >= 0 && label != null && !label.isBlank()) {
                    out.put(idx, label);
                }
            }
            return out;
        } catch (Exception e) {
            log.error("Failed to parse OpenAI extended response", e);
            return Map.of();
        }
    }

    private String parseSingleLabel(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) return null;
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.at("/choices/0/message/content");
            if (content.isMissingNode() || content.asText().isBlank()) return null;
            String text = content.asText().trim();
            String cleaned = text.replace("`", "").trim();
            if (cleaned.startsWith("{") || cleaned.startsWith("[")) {
                JsonNode payload = objectMapper.readTree(cleaned);
                if (payload.isTextual()) {
                    return payload.asText().trim();
                }
                if (payload.has("label")) {
                    return payload.get("label").asText().trim();
                }
            }
            return cleaned.replaceAll("^\"|\"$", "");
        } catch (Exception e) {
            log.error("Failed to parse OpenAI relationship response", e);
            return null;
        }
    }

    private JsonNode readJsonObject(String text) {
        String trimmed = text.trim();
        try {
            return objectMapper.readTree(trimmed);
        } catch (Exception ignored) {
            String extracted = extractJsonBlock(trimmed, '{', '}');
            if (extracted == null) return null;
            try {
                return objectMapper.readTree(extracted);
            } catch (Exception e) {
                return null;
            }
        }
    }

    private JsonNode readJsonArray(String text) {
        String trimmed = text.trim();
        try {
            return objectMapper.readTree(trimmed);
        } catch (Exception ignored) {
            String extracted = extractJsonBlock(trimmed, '[', ']');
            if (extracted == null) return null;
            try {
                return objectMapper.readTree(extracted);
            } catch (Exception e) {
                return null;
            }
        }
    }

    private String extractJsonBlock(String text, char open, char close) {
        int start = text.indexOf(open);
        int end = text.lastIndexOf(close);
        if (start < 0 || end <= start) {
            return null;
        }
        return text.substring(start, end + 1);
    }

    private void put(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) map.put(key, value);
    }

    private String sha256(String input) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(Objects.hashCode(input));
        }
    }
}
