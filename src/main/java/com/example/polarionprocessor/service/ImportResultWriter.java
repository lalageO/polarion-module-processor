package com.example.polarionprocessor.service;

import com.example.polarionprocessor.model.ImportJobResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Writes the machine-readable JSON ledger.
 */
@Service
public class ImportResultWriter {

    private final ObjectMapper objectMapper;

    public ImportResultWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Serializes import_result.json using indented JSON for easier inspection.
     */
    public void write(Path file, ImportJobResult result) throws IOException {
        objectMapper.writeValue(file.toFile(), result);
    }
}
