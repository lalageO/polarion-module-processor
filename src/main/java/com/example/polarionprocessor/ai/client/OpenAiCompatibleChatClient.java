package com.example.polarionprocessor.ai.client;

import com.example.polarionprocessor.ai.config.AiProperties;
import com.example.polarionprocessor.ai.model.AiChatResponse;
import com.example.polarionprocessor.ai.model.AiUsage;
import com.example.polarionprocessor.util.TextUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI-compatible /v1/chat/completions 客户端。
 */
@Service
public class OpenAiCompatibleChatClient implements AiChatClient {

    private final AiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleChatClient(AiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiChatResponse chat(String prompt) {
        RuntimeException lastException = null;
        int maxAttempts = Math.max(1, valueOrDefault(properties.getMaxRetries(), 1));
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return doChat(prompt);
            } catch (RuntimeException e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    sleep(valueOrDefault(properties.getRetryIntervalMs(), 1000));
                }
            }
        }
        throw lastException == null
                ? new IllegalStateException("AI chat failed")
                : lastException;
    }

    private AiChatResponse doChat(String prompt) {
        String url = chatUrl();
        try {
            String rawResponse = objectMapper.writeValueAsString(restTemplate().postForObject(
                    url,
                    new HttpEntity<Map<String, Object>>(requestBody(prompt), headers()),
                    Object.class));
            JsonNode root = objectMapper.readTree(rawResponse);
            AiChatResponse response = new AiChatResponse();
            response.setModel(text(root.path("model")));
            response.setRawResponse(rawResponse);
            response.setContent(extractContent(root));
            response.setUsage(extractUsage(root.path("usage")));
            return response;
        } catch (HttpStatusCodeException e) {
            throw new IllegalStateException("AI HTTP call failed: url=" + url
                    + ", status=" + e.getRawStatusCode()
                    + ", body=" + abbreviate(e.getResponseBodyAsString(), 300), e);
        } catch (RestClientException e) {
            throw new IllegalStateException("AI HTTP call failed: url=" + url + ", " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalStateException("AI response parse failed: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> requestBody(String prompt) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("model", properties.getModel());
        body.put("temperature", properties.getTemperature() == null ? 0.2D : properties.getTemperature());
        List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
        Map<String, String> userMessage = new LinkedHashMap<String, String>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        body.put("messages", messages);
        if (Boolean.TRUE.equals(properties.getResponseFormatJson())) {
            Map<String, String> responseFormat = new LinkedHashMap<String, String>();
            responseFormat.put("type", "json_object");
            body.put("response_format", responseFormat);
        }
        return body;
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (TextUtils.hasText(properties.getApiKey())) {
            headers.setBearerAuth(properties.getApiKey().trim());
        }
        return headers;
    }

    private RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(valueOrDefault(properties.getConnectTimeoutMs(), 5000));
        requestFactory.setReadTimeout(valueOrDefault(properties.getReadTimeoutMs(), 60000));
        return new RestTemplate(requestFactory);
    }

    private String chatUrl() {
        String baseUrl = TextUtils.hasText(properties.getBaseUrl())
                ? properties.getBaseUrl().trim()
                : "http://llm.freetech.com/v1";
        String path = TextUtils.hasText(properties.getChatCompletionsPath())
                ? properties.getChatCompletionsPath().trim()
                : "/chat/completions";
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + path;
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }

    private String extractContent(JsonNode root) {
        JsonNode choices = root.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode message = choices.get(0).path("message");
            String content = text(message.path("content"));
            if (TextUtils.hasText(content)) {
                return content;
            }
            return text(choices.get(0).path("text"));
        }
        return null;
    }

    private AiUsage extractUsage(JsonNode usageNode) {
        if (usageNode == null || usageNode.isMissingNode() || usageNode.isNull()) {
            return null;
        }
        AiUsage usage = new AiUsage();
        usage.setPromptTokens(intValue(usageNode.path("prompt_tokens")));
        usage.setCompletionTokens(intValue(usageNode.path("completion_tokens")));
        usage.setTotalTokens(intValue(usageNode.path("total_tokens")));
        return usage;
    }

    private Integer intValue(JsonNode node) {
        return node == null || !node.isNumber() ? null : node.asInt();
    }

    private String text(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    private int valueOrDefault(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private void sleep(int millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI retry interrupted", e);
        }
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
