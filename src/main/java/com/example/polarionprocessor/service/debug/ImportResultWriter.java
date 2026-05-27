package com.example.polarionprocessor.service.debug;

import com.example.polarionprocessor.model.debug.ImportJobResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 写出程序可读的 JSON 结果账本。
 */
@Service
public class ImportResultWriter {

    private final ObjectMapper objectMapper;

    public ImportResultWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * 使用缩进 JSON 序列化 import_result.json，便于人工查看。
     */
    public void write(Path file, ImportJobResult result) throws IOException {
        objectMapper.writeValue(file.toFile(), result);
    }
}
