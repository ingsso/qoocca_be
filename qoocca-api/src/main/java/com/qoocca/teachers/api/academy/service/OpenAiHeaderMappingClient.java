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

    private Map<String, String> callOpenAI(List<String> headers) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", properties.getModel());
        request.put("temperature", 0);
        request.put("max_tokens", 120);

        String prompt = "Map headers to fields (studentName, studentPhone, className). " +
                "Return JSON object with keys studentName, studentPhone, className and string values.\n" +
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

    private Map<String, String> parseMapping(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) return Map.of();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.at("/choices/0/message/content");
            if (content.isMissingNode() || content.asText().isBlank()) return Map.of();

            JsonNode payload = objectMapper.readTree(content.asText());

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
