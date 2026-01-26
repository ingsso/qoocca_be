package com.qoocca.teachers.api.academy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoocca.teachers.api.global.config.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiHeaderMappingClient {

    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, String> mapHeaders(List<String> headers) {
        Map<String, String> mapping = new HashMap<>();
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            return mapping;
        }

        String prompt = buildPrompt(headers);
        Map<String, Object> request = buildRequest(prompt);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(properties.getApiKey());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, httpHeaders);

        try {
            String response = restTemplate.postForObject(
                    properties.getBaseUrl() + "/chat/completions",
                    entity,
                    String.class
            );
            return parseMapping(response);
        } catch (RestClientException e) {
            return mapping;
        }
    }

    private Map<String, Object> buildRequest(String prompt) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", properties.getModel());
        request.put("temperature", 0);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content",
                        "You map spreadsheet headers to canonical fields. " +
                                "Return JSON only."),
                Map.of("role", "user", "content", prompt)
        );
        request.put("messages", messages);
        return request;
    }

    private String buildPrompt(List<String> headers) {
        return "Canonical fields: studentName, studentPhone, className.\n" +
                "Input headers: " + headers + "\n" +
                "Return JSON:\n" +
                "{\n" +
                "  \"studentName\": {\"header\": \"...\", \"confidence\": 0.0-1.0},\n" +
                "  \"studentPhone\": {\"header\": \"...\", \"confidence\": 0.0-1.0},\n" +
                "  \"className\": {\"header\": \"...\", \"confidence\": 0.0-1.0}\n" +
                "}";
    }

    private Map<String, String> parseMapping(String responseBody) {
        Map<String, String> mapping = new HashMap<>();
        if (responseBody == null || responseBody.isBlank()) {
            return mapping;
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.at("/choices/0/message/content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                return mapping;
            }
            JsonNode payload = objectMapper.readTree(content.asText());
            mapping.put("studentName", getHeaderName(payload, "studentName"));
            mapping.put("studentPhone", getHeaderName(payload, "studentPhone"));
            mapping.put("className", getHeaderName(payload, "className"));
            return mapping;
        } catch (Exception e) {
            return mapping;
        }
    }

    private String getHeaderName(JsonNode payload, String field) {
        JsonNode node = payload.path(field).path("header");
        if (node.isMissingNode()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }
}
