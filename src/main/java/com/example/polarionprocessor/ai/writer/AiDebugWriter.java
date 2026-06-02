package com.example.polarionprocessor.ai.writer;

import com.example.polarionprocessor.ai.config.AiProperties;
import com.example.polarionprocessor.ai.model.AiDebugRecord;
import com.example.polarionprocessor.util.TextUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * AI 调试信息 JSON Lines 写出器。
 */
@Service
public class AiDebugWriter {

    private static final String DEFAULT_FILE_NAME = "ai_debug.jsonl";

    private final AiProperties properties;
    private final ObjectMapper objectMapper;

    public AiDebugWriter(AiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public boolean isEnabled() {
        return properties.getDebug() != null && Boolean.TRUE.equals(properties.getDebug().getEnabled());
    }

    public String fileName() {
        if (properties.getDebug() != null && TextUtils.hasText(properties.getDebug().getFileName())) {
            return properties.getDebug().getFileName().trim();
        }
        return DEFAULT_FILE_NAME;
    }

    public void append(Path file, AiDebugRecord record) throws IOException {
        if (!isEnabled() || file == null || record == null) {
            return;
        }
        normalizeRecord(record);
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        Files.write(
                file,
                (objectMapper.writeValueAsString(record) + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    private void normalizeRecord(AiDebugRecord record) {
        AiProperties.Debug debug = properties.getDebug();
        if (debug == null) {
            return;
        }
        record.setPrompt(abbreviate(record.getPrompt(), valueOrDefault(debug.getPromptMaxLength(), 4000)));
        if (!Boolean.TRUE.equals(debug.getStoreRawResponse())) {
            record.setRawResponse(null);
        } else {
            record.setRawResponse(abbreviate(record.getRawResponse(), valueOrDefault(debug.getRawResponseMaxLength(), 4000)));
        }
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || maxLength <= 0 || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...[truncated]";
    }

    private int valueOrDefault(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
