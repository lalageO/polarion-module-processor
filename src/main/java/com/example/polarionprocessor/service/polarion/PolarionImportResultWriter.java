package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.model.polarion.PolarionImportJobResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 正式流程 JSON 结果写出器，使用临时文件实现原子落盘。
 */
@Service
public class PolarionImportResultWriter {

    private final ObjectMapper objectMapper;

    public PolarionImportResultWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * 写入 import_result.json；先写 .tmp，再移动覆盖正式文件。
     */
    public void writeAtomic(Path file, PolarionImportJobResult result) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        Path tempFile = file.resolveSibling(file.getFileName().toString() + ".tmp");
        objectMapper.writeValue(tempFile.toFile(), result);
        try {
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
