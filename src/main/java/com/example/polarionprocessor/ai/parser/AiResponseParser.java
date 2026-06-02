package com.example.polarionprocessor.ai.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 从模型文本中提取第一个 JSON 对象并解析为字段 Map。
 */
@Service
public class AiResponseParser {

    private final ObjectMapper objectMapper;

    public AiResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> parseObject(String text) {
        String json = extractFirstJsonObject(text);
        if (json == null) {
            throw new IllegalArgumentException("AI response does not contain a JSON object");
        }
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("AI response JSON parse failed: " + e.getMessage(), e);
        }
    }

    private String extractFirstJsonObject(String text) {
        if (text == null) {
            return null;
        }
        int start = text.indexOf('{');
        if (start < 0) {
            return null;
        }
        boolean inString = false;
        boolean escaping = false;
        int depth = 0;
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (escaping) {
                escaping = false;
                continue;
            }
            if (ch == '\\' && inString) {
                escaping = true;
                continue;
            }
            if (ch == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (ch == '{') {
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0) {
                    return text.substring(start, i + 1);
                }
            }
        }
        return null;
    }
}
