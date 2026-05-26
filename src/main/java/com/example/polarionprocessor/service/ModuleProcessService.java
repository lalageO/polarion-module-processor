package com.example.polarionprocessor.service;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.enums.ItemStatus;
import com.example.polarionprocessor.enums.ReplaceMode;
import com.example.polarionprocessor.model.ImportItemResult;
import com.example.polarionprocessor.model.ImportJobResult;
import com.example.polarionprocessor.model.ModuleProcessRequest;
import com.example.polarionprocessor.model.ModuleProcessResponse;
import com.example.polarionprocessor.model.ModuleXmlContent;
import com.example.polarionprocessor.model.ParagraphInfo;
import com.example.polarionprocessor.util.FileUtils;
import com.example.polarionprocessor.util.HashUtils;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ModuleProcessService {

    private static final String ORIGINAL_XML_FILE = "original_module.xml";
    private static final String PROCESSED_XML_FILE = "processed_module.xml";
    private static final String RESULT_JSON_FILE = "import_result.json";
    private static final String PREVIEW_CSV_FILE = "import_preview.csv";
    private static final DateTimeFormatter JOB_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    private static final DateTimeFormatter RESULT_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ModuleProcessorProperties properties;
    private final ModuleXmlExtractor moduleXmlExtractor;
    private final ParagraphScanner paragraphScanner;
    private final NumberedItemGrouper numberedItemGrouper;
    private final ParagraphCandidateSelector candidateSelector;
    private final TitleGenerator titleGenerator;
    private final WorkItemIdProvider workItemIdProvider;
    private final ModuleXmlRewriter moduleXmlRewriter;
    private final ImportResultWriter importResultWriter;
    private final ImportPreviewCsvWriter importPreviewCsvWriter;

    public ModuleProcessService(ModuleProcessorProperties properties,
                                ModuleXmlExtractor moduleXmlExtractor,
                                ParagraphScanner paragraphScanner,
                                NumberedItemGrouper numberedItemGrouper,
                                ParagraphCandidateSelector candidateSelector,
                                TitleGenerator titleGenerator,
                                WorkItemIdProvider workItemIdProvider,
                                ModuleXmlRewriter moduleXmlRewriter,
                                ImportResultWriter importResultWriter,
                                ImportPreviewCsvWriter importPreviewCsvWriter) {
        this.properties = properties;
        this.moduleXmlExtractor = moduleXmlExtractor;
        this.paragraphScanner = paragraphScanner;
        this.numberedItemGrouper = numberedItemGrouper;
        this.candidateSelector = candidateSelector;
        this.titleGenerator = titleGenerator;
        this.workItemIdProvider = workItemIdProvider;
        this.moduleXmlRewriter = moduleXmlRewriter;
        this.importResultWriter = importResultWriter;
        this.importPreviewCsvWriter = importPreviewCsvWriter;
    }

    public ModuleProcessResponse process(ModuleProcessRequest request) {
        MultipartFile file = request.getFile();
        if (file == null || file.isEmpty()) {
            return ModuleProcessResponse.failure("module.xml upload failed");
        }

        String xmlContent;
        try {
            xmlContent = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return ModuleProcessResponse.failure("module.xml upload failed");
        }

        String moduleName = resolveModuleName(request, xmlContent);
        boolean dryRun = request.getDryRun() == null ? true : request.getDryRun();
        ReplaceMode replaceMode;
        try {
            replaceMode = ReplaceMode.from(request.getReplaceMode(), properties.getDefaultReplaceMode());
        } catch (IllegalArgumentException e) {
            return ModuleProcessResponse.failure("Unsupported replaceMode: " + request.getReplaceMode());
        }
        if (ReplaceMode.MAPPING.equals(replaceMode)) {
            return ModuleProcessResponse.failure("MAPPING replaceMode is reserved but not implemented in version 1");
        }

        String jobId = buildJobId(moduleName);
        Path outputRoot = Paths.get(properties.getOutputDir());
        Path jobDir = outputRoot.resolve(jobId);
        Path originalFile = jobDir.resolve(ORIGINAL_XML_FILE);
        Path processedFile = jobDir.resolve(PROCESSED_XML_FILE);
        Path resultJsonFile = jobDir.resolve(RESULT_JSON_FILE);
        Path previewCsvFile = jobDir.resolve(PREVIEW_CSV_FILE);

        String createdAt = RESULT_TIME_FORMAT.format(LocalDateTime.now());
        try {
            FileUtils.ensureDirectory(jobDir);
            FileUtils.writeUtf8(originalFile, xmlContent);

            ModuleXmlContent moduleXmlContent = moduleXmlExtractor.extract(xmlContent);
            List<ParagraphInfo> paragraphs = paragraphScanner.scan(moduleXmlContent.getHtmlContent());
            List<ImportItemResult> items = buildItems(request, moduleName, moduleXmlContent.getHtmlContent(), paragraphs);

            String processedXmlContent = xmlContent;
            if (!dryRun && ReplaceMode.MOCK.equals(replaceMode)) {
                assignMockWorkItemIds(request, items);
                processedXmlContent = moduleXmlRewriter.rewrite(moduleXmlContent, items);
            }
            FileUtils.writeUtf8(processedFile, processedXmlContent);

            ImportJobResult jobResult = buildJobResult(
                    jobId,
                    moduleName,
                    replaceMode,
                    dryRun,
                    createdAt,
                    xmlContent,
                    paragraphs.size(),
                    items);
            importResultWriter.write(resultJsonFile, jobResult);
            importPreviewCsvWriter.write(previewCsvFile, items);

            return buildResponse(
                    true,
                    "Process completed",
                    jobId,
                    moduleName,
                    replaceMode,
                    dryRun,
                    jobDir,
                    jobResult);
        } catch (ModuleProcessException e) {
            return buildFailureResponse(e.getErrorCode() + ": " + e.getMessage(), jobId, moduleName, replaceMode, dryRun, jobDir);
        } catch (IOException e) {
            return buildFailureResponse("FILE_WRITE_FAILED: " + e.getMessage(), jobId, moduleName, replaceMode, dryRun, jobDir);
        } catch (RuntimeException e) {
            return buildFailureResponse("FAILED: " + formatRuntimeError(e), jobId, moduleName, replaceMode, dryRun, jobDir);
        }
    }

    private String formatRuntimeError(RuntimeException e) {
        if (TextUtils.hasText(e.getMessage())) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        return e.getClass().getSimpleName();
    }

    private List<ImportItemResult> buildItems(ModuleProcessRequest request,
                                              String moduleName,
                                              String htmlContent,
                                              List<ParagraphInfo> paragraphs) {
        int minOutlineDepth = request.getMinOutlineDepth() == null
                ? properties.getDefaultMinOutlineDepth()
                : request.getMinOutlineDepth();
        boolean requireKeyword = request.getRequireKeyword() == null
                ? Boolean.TRUE.equals(properties.getDefaultRequireKeyword())
                : request.getRequireKeyword();
        int levelTwoMinTextLength = properties.getLevelTwoMinTextLength() == null
                ? 80
                : properties.getLevelTwoMinTextLength();

        List<ImportItemResult> items = numberedItemGrouper.group(moduleName, htmlContent, paragraphs);
        for (ImportItemResult item : items) {
            candidateSelector.apply(item, minOutlineDepth, requireKeyword, levelTwoMinTextLength);
            if (Boolean.TRUE.equals(item.getCandidate())) {
                String title = titleGenerator.generate(item, request);
                item.setGeneratedTitle(title);
                item.setFinalTitle(title);
            }
        }
        return items;
    }

    private void assignMockWorkItemIds(ModuleProcessRequest request, List<ImportItemResult> items) {
        int candidateIndex = 1;
        String mockIdPrefix = TextUtils.hasText(request.getMockIdPrefix())
                ? request.getMockIdPrefix()
                : properties.getDefaultMockIdPrefix();
        for (ImportItemResult item : items) {
            if (!Boolean.TRUE.equals(item.getCandidate())) {
                continue;
            }
            item.setWorkItemId(mockIdPrefix);
            String workItemId = workItemIdProvider.provide(item, candidateIndex++);
            item.setWorkItemId(workItemId);
            item.setReplacementHtml("<div id=\"polarion_wiki macro name=module-workitem;params=id="
                    + workItemId
                    + "\"></div>");
        }
    }

    private ImportJobResult buildJobResult(String jobId,
                                           String moduleName,
                                           ReplaceMode replaceMode,
                                           boolean dryRun,
                                           String createdAt,
                                           String xmlContent,
                                           int totalParagraphCount,
                                           List<ImportItemResult> items) {
        ImportJobResult result = new ImportJobResult();
        result.setJobId(jobId);
        result.setModuleName(moduleName);
        result.setMode(replaceMode.name());
        result.setDryRun(dryRun);
        result.setCreatedAt(createdAt);
        result.setUpdatedAt(RESULT_TIME_FORMAT.format(LocalDateTime.now()));
        result.setSourceXmlHash(HashUtils.sha256(xmlContent));
        result.setItems(items);
        result.setTotalParagraphCount(totalParagraphCount);
        result.setTotalItemCount(items.size());
        result.setCandidateCount(countCandidates(items));
        result.setReplacedCount(countStatus(items, ItemStatus.REPLACED));
        result.setFailedCount(countStatus(items, ItemStatus.REPLACE_FAILED) + countStatus(items, ItemStatus.FAILED));
        result.setSkippedCount(items.size() - result.getCandidateCount());
        return result;
    }

    private ModuleProcessResponse buildResponse(boolean success,
                                                String message,
                                                String jobId,
                                                String moduleName,
                                                ReplaceMode replaceMode,
                                                boolean dryRun,
                                                Path jobDir,
                                                ImportJobResult jobResult) {
        ModuleProcessResponse response = new ModuleProcessResponse();
        response.setSuccess(success);
        response.setJobId(jobId);
        response.setModuleName(moduleName);
        response.setDryRun(dryRun);
        response.setReplaceMode(replaceMode.name());
        response.setTotalParagraphCount(jobResult.getTotalParagraphCount());
        response.setTotalItemCount(jobResult.getTotalItemCount());
        response.setCandidateCount(jobResult.getCandidateCount());
        response.setReplacedCount(jobResult.getReplacedCount());
        response.setSkippedCount(jobResult.getSkippedCount());
        response.setFailedCount(jobResult.getFailedCount());
        response.setOutputDir(jobDir.toString().replace('\\', '/'));
        response.setOriginalXmlFile(ORIGINAL_XML_FILE);
        response.setProcessedXmlFile(PROCESSED_XML_FILE);
        response.setResultJsonFile(RESULT_JSON_FILE);
        response.setPreviewCsvFile(PREVIEW_CSV_FILE);
        response.setMessage(message);
        return response;
    }

    private ModuleProcessResponse buildFailureResponse(String message,
                                                       String jobId,
                                                       String moduleName,
                                                       ReplaceMode replaceMode,
                                                       boolean dryRun,
                                                       Path jobDir) {
        ModuleProcessResponse response = ModuleProcessResponse.failure(message);
        response.setJobId(jobId);
        response.setModuleName(moduleName);
        response.setDryRun(dryRun);
        response.setReplaceMode(replaceMode.name());
        response.setOutputDir(jobDir.toString().replace('\\', '/'));
        response.setOriginalXmlFile(ORIGINAL_XML_FILE);
        response.setProcessedXmlFile(PROCESSED_XML_FILE);
        response.setResultJsonFile(RESULT_JSON_FILE);
        response.setPreviewCsvFile(PREVIEW_CSV_FILE);
        return response;
    }

    private String resolveModuleName(ModuleProcessRequest request, String xmlContent) {
        if (TextUtils.hasText(request.getModuleName())) {
            return request.getModuleName().trim();
        }
        String title = moduleXmlExtractor.extractModuleTitle(xmlContent);
        if (TextUtils.hasText(title)) {
            return title;
        }
        return properties.getDefaultModuleName();
    }

    private String buildJobId(String moduleName) {
        return TextUtils.sanitizePathPart(moduleName) + "_" + JOB_TIME_FORMAT.format(LocalDateTime.now());
    }

    private int countCandidates(List<ImportItemResult> items) {
        int count = 0;
        for (ImportItemResult item : items) {
            if (Boolean.TRUE.equals(item.getCandidate())) {
                count++;
            }
        }
        return count;
    }

    private int countStatus(List<ImportItemResult> items, ItemStatus status) {
        int count = 0;
        for (ImportItemResult item : items) {
            if (status.name().equals(item.getStatus())) {
                count++;
            }
        }
        return count;
    }
}
