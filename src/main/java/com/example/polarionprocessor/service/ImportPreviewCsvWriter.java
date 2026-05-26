package com.example.polarionprocessor.service;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.model.ImportItemResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ImportPreviewCsvWriter {

    private final ModuleProcessorProperties properties;

    public ImportPreviewCsvWriter(ModuleProcessorProperties properties) {
        this.properties = properties;
    }

    public void write(Path file, List<ImportItemResult> items) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
        try {
            if (Boolean.TRUE.equals(properties.getCsvWithBom())) {
                writer.write('\uFEFF');
            }
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader(
                            "seq",
                            "paragraphId",
                            "outlineNo",
                            "candidate",
                            "skipReason",
                            "sourceText",
                            "generatedTitle",
                            "finalTitle",
                            "workItemId",
                            "status",
                            "errorMessage")
                    .build();
            CSVPrinter printer = new CSVPrinter(writer, format);
            try {
                for (ImportItemResult item : items) {
                    printer.printRecord(
                            item.getSeq(),
                            item.getParagraphId(),
                            item.getOutlineNo(),
                            Boolean.TRUE.equals(item.getCandidate()) ? "YES" : "NO",
                            item.getSkipReason(),
                            item.getSourceText(),
                            item.getGeneratedTitle(),
                            item.getFinalTitle(),
                            item.getWorkItemId(),
                            item.getStatus(),
                            item.getErrorMessage());
                }
            } finally {
                printer.close();
            }
        } finally {
            writer.close();
        }
    }
}
