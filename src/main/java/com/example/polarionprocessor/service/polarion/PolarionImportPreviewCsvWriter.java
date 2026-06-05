package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.model.polarion.PolarionImportItemResult;
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
 * 正式流程 CSV 预览写出器。
 */
@Service
public class PolarionImportPreviewCsvWriter {

    private final ModuleProcessorProperties properties;

    public PolarionImportPreviewCsvWriter(ModuleProcessorProperties properties) {
        this.properties = properties;
    }

    /**
     * 按第二版固定字段写出 import_preview.csv。
     */
    public void write(Path file, List<PolarionImportItemResult> items) throws IOException {
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
                            "itemRole",
                            "workItemType",
                            "outlineNo",
                            "outlineDepth",
                            "startParagraphId",
                            "endParagraphId",
                            "candidate",
                            "skipReason",
                            "titleText",
                            "hasChildOutline",
                            "decisionReason",
                            "parentOutlineNo",
                            "parentWkId",
                            "title",
                            "workItemId",
                            "aiStatus",
                            "status",
                            "errorMessage",
                            "description")
                    .build();
            CSVPrinter printer = new CSVPrinter(writer, format);
            try {
                for (PolarionImportItemResult item : items) {
                    printer.printRecord(
                            item.getSeq(),
                            item.getItemRole(),
                            item.getWorkItemType(),
                            item.getOutlineNo(),
                            item.getOutlineDepth(),
                            item.getStartParagraphId(),
                            item.getEndParagraphId(),
                            Boolean.TRUE.equals(item.getCandidate()) ? "YES" : "NO",
                            item.getSkipReason(),
                            item.getTitleText(),
                            Boolean.TRUE.equals(item.getHasChildOutline()) ? "YES" : "NO",
                            item.getDecisionReason(),
                            item.getParentOutlineNo(),
                            item.getParentWkId(),
                            item.getTitle(),
                            item.getWorkItemId(),
                            item.getAiStatus(),
                            item.getStatus(),
                            item.getErrorMessage(),
                            item.getDescription());
                }
            } finally {
                printer.close();
            }
        } finally {
            writer.close();
        }
    }
}
