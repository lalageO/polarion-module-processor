package com.example.polarionprocessor.service.debug;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.model.shared.ImportItemResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 写出人工可读的预览表，用于检查候选项和替换结果。
 */
@Service
public class ImportPreviewCsvWriter {

    private final ModuleProcessorProperties properties;

    public ImportPreviewCsvWriter(ModuleProcessorProperties properties) {
        this.properties = properties;
    }

    /**
     * 使用固定表头顺序写出 import_preview.csv。
     */
    public void write(Path file, List<ImportItemResult> items) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
        try {
            if (Boolean.TRUE.equals(properties.getCsvWithBom())) {
                // Windows 上的 Excel 带 BOM 时更容易正确识别 UTF-8。
                writer.write('\uFEFF');
            }
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader(
                            "seq",
                            "paragraphId",
                            "paragraphCount",
                            "paragraphIds",
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
                            item.getParagraphCount(),
                            item.getParagraphIds(),
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
