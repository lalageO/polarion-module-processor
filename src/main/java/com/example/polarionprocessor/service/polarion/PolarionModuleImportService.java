package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.config.PolarionProperties;
import com.example.polarionprocessor.enums.ItemStatus;
import com.example.polarionprocessor.enums.JobStatus;
import com.example.polarionprocessor.model.shared.ImportItemResult;
import com.example.polarionprocessor.model.debug.ModuleProcessRequest;
import com.example.polarionprocessor.model.shared.ModuleXmlContent;
import com.example.polarionprocessor.model.shared.ParagraphInfo;
import com.example.polarionprocessor.model.polarion.PolarionImportFiles;
import com.example.polarionprocessor.model.polarion.PolarionImportItemResult;
import com.example.polarionprocessor.model.polarion.PolarionImportJobResult;
import com.example.polarionprocessor.model.polarion.PolarionImportSummary;
import com.example.polarionprocessor.model.polarion.PolarionModuleLocation;
import com.example.polarionprocessor.model.polarion.PolarionModuleImportRequest;
import com.example.polarionprocessor.model.polarion.PolarionModuleImportResponse;
import com.example.polarionprocessor.model.polarion.WorkItemCreateRequest;
import com.example.polarionprocessor.model.polarion.WorkItemCreateResult;
import com.example.polarionprocessor.service.shared.ModuleProcessException;
import com.example.polarionprocessor.service.shared.ModuleXmlExtractor;
import com.example.polarionprocessor.service.shared.ModuleXmlRewriter;
import com.example.polarionprocessor.service.shared.NumberedItemGrouper;
import com.example.polarionprocessor.service.shared.ParagraphCandidateSelector;
import com.example.polarionprocessor.service.shared.ParagraphScanner;
import com.example.polarionprocessor.service.shared.TitleGenerator;
import com.example.polarionprocessor.util.FileUtils;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 正式 Polarion module.xml 导入流程编排服务。
 */
@Service
public class PolarionModuleImportService {

    private static final String ORIGINAL_XML_FILE = "original_module.xml";
    private static final String PROCESSED_XML_FILE = "processed_module.xml";
    private static final String RESULT_JSON_FILE = "import_result.json";
    private static final String PREVIEW_CSV_FILE = "import_preview.csv";
    private static final DateTimeFormatter JOB_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private final ModuleProcessorProperties moduleProperties;
    private final PolarionProperties polarionProperties;
    private final PolarionModuleUrlParser moduleUrlParser;
    private final ModuleXmlDownloader moduleXmlDownloader;
    private final ModuleXmlExtractor moduleXmlExtractor;
    private final ParagraphScanner paragraphScanner;
    private final NumberedItemGrouper numberedItemGrouper;
    private final ParagraphCandidateSelector candidateSelector;
    private final TitleGenerator titleGenerator;
    private final PolarionWorkItemCreator workItemCreator;
    private final ModuleWorkItemMacroRenderer macroRenderer;
    private final ModuleXmlRewriter moduleXmlRewriter;
    private final PolarionImportResultWriter resultWriter;
    private final PolarionImportPreviewCsvWriter csvWriter;

    public PolarionModuleImportService(ModuleProcessorProperties moduleProperties,
                                       PolarionProperties polarionProperties,
                                       PolarionModuleUrlParser moduleUrlParser,
                                       ModuleXmlDownloader moduleXmlDownloader,
                                       ModuleXmlExtractor moduleXmlExtractor,
                                       ParagraphScanner paragraphScanner,
                                       NumberedItemGrouper numberedItemGrouper,
                                       ParagraphCandidateSelector candidateSelector,
                                       TitleGenerator titleGenerator,
                                       PolarionWorkItemCreator workItemCreator,
                                       ModuleWorkItemMacroRenderer macroRenderer,
                                       ModuleXmlRewriter moduleXmlRewriter,
                                       PolarionImportResultWriter resultWriter,
                                       PolarionImportPreviewCsvWriter csvWriter) {
        this.moduleProperties = moduleProperties;
        this.polarionProperties = polarionProperties;
        this.moduleUrlParser = moduleUrlParser;
        this.moduleXmlDownloader = moduleXmlDownloader;
        this.moduleXmlExtractor = moduleXmlExtractor;
        this.paragraphScanner = paragraphScanner;
        this.numberedItemGrouper = numberedItemGrouper;
        this.candidateSelector = candidateSelector;
        this.titleGenerator = titleGenerator;
        this.workItemCreator = workItemCreator;
        this.macroRenderer = macroRenderer;
        this.moduleXmlRewriter = moduleXmlRewriter;
        this.resultWriter = resultWriter;
        this.csvWriter = csvWriter;
    }

    /**
     * 执行正式导入流程。
     */
    public PolarionModuleImportResponse importModule(PolarionModuleImportRequest request) {
        ResolvedRequest resolved = resolveRequest(request);
        String jobId = buildJobId(resolved.moduleName);
        Path jobDir = Paths.get(moduleProperties.getOutputDir()).resolve(jobId);
        Path originalFile = jobDir.resolve(ORIGINAL_XML_FILE);
        Path processedFile = jobDir.resolve(PROCESSED_XML_FILE);
        Path resultJsonFile = jobDir.resolve(RESULT_JSON_FILE);
        Path previewCsvFile = jobDir.resolve(PREVIEW_CSV_FILE);

        PolarionImportJobResult jobResult = buildEmptyJobResult(jobId, resolved);
        try {
            FileUtils.ensureDirectory(jobDir);
            String xmlContent = moduleXmlDownloader.download(resolved.baseUrl, resolved.projectId, resolved.moduleFolder, resolved.moduleName);
            FileUtils.writeUtf8(originalFile, xmlContent);

            ModuleXmlContent moduleXmlContent = moduleXmlExtractor.extract(xmlContent);
            List<ParagraphInfo> paragraphs = paragraphScanner.scan(moduleXmlContent.getHtmlContent());
            List<PolarionImportItemResult> items = buildItems(resolved, moduleXmlContent.getHtmlContent(), paragraphs);

            jobResult.setItems(items);
            jobResult.setStatus(JobStatus.ITEMS_READY.name());
            updateSummary(jobResult, paragraphs.size());
            resultWriter.writeAtomic(resultJsonFile, jobResult);
            csvWriter.write(previewCsvFile, items);

            if (resolved.dryRun) {
                return buildResponse(true, "Dry-run completed", jobDir, jobResult);
            }

            createWorkItems(resolved, jobResult, resultJsonFile, previewCsvFile);
            rewriteXml(moduleXmlContent, jobResult, processedFile);

            jobResult.setStatus(hasErrors(jobResult.getItems())
                    ? JobStatus.COMPLETED_WITH_ERRORS.name()
                    : JobStatus.COMPLETED.name());
            updateSummary(jobResult, paragraphs.size());
            resultWriter.writeAtomic(resultJsonFile, jobResult);
            csvWriter.write(previewCsvFile, jobResult.getItems());
            return buildResponse(true, "Polarion module import completed", jobDir, jobResult);
        } catch (ModuleProcessException e) {
            jobResult.setStatus(JobStatus.FAILED.name());
            return buildFailureResponse(e.getErrorCode() + ": " + e.getMessage(), jobDir, jobResult);
        } catch (IOException e) {
            jobResult.setStatus(JobStatus.FAILED.name());
            return buildFailureResponse("FILE_WRITE_FAILED: " + e.getMessage(), jobDir, jobResult);
        } catch (RuntimeException e) {
            jobResult.setStatus(JobStatus.FAILED.name());
            return buildFailureResponse("FAILED: " + e.getClass().getSimpleName() + ": " + e.getMessage(), jobDir, jobResult);
        }
    }

    private List<PolarionImportItemResult> buildItems(ResolvedRequest resolved,
                                                      String htmlContent,
                                                      List<ParagraphInfo> paragraphs) {
        List<ImportItemResult> legacyItems = numberedItemGrouper.group(resolved.moduleName, htmlContent, paragraphs);
        List<PolarionImportItemResult> items = new ArrayList<PolarionImportItemResult>();
        ModuleProcessRequest titleRequest = new ModuleProcessRequest();
        int minOutlineDepth = moduleProperties.getDefaultMinOutlineDepth() == null
                ? 2
                : moduleProperties.getDefaultMinOutlineDepth();
        int levelTwoMinTextLength = moduleProperties.getLevelTwoMinTextLength() == null
                ? 80
                : moduleProperties.getLevelTwoMinTextLength();

        for (ImportItemResult legacyItem : legacyItems) {
            candidateSelector.apply(legacyItem, minOutlineDepth, resolved.requireKeyword, levelTwoMinTextLength);
            PolarionImportItemResult item = mapItem(resolved, legacyItem);
            if (Boolean.TRUE.equals(legacyItem.getCandidate())) {
                item.setTitle(titleGenerator.generate(legacyItem, titleRequest));
                item.setStatus(ItemStatus.READY.name());
            } else {
                item.setStatus(ItemStatus.SKIPPED.name());
            }
            items.add(item);
        }
        return items;
    }

    private PolarionImportItemResult mapItem(ResolvedRequest resolved, ImportItemResult legacyItem) {
        PolarionImportItemResult item = new PolarionImportItemResult();
        item.setSeq(legacyItem.getSeq());
        item.setItemKey(legacyItem.getParagraphId());
        item.setStartParagraphId(legacyItem.getStartParagraphId());
        item.setEndParagraphId(legacyItem.getEndParagraphId());
        item.setOutlineNo(legacyItem.getOutlineNo());
        item.setDescription(legacyItem.getDescription());
        item.setCandidate(legacyItem.getCandidate());
        item.setSkipReason(legacyItem.getSkipReason());
        item.setSourceStartIndex(legacyItem.getSourceStartIndex());
        item.setSourceEndIndex(legacyItem.getSourceEndIndex());
        item.setWorkItemCreateFields(buildCreateFields(resolved));
        return item;
    }

    private Map<String, Object> buildCreateFields(ResolvedRequest resolved) {
        Map<String, Object> fields = new LinkedHashMap<String, Object>();
        fields.put("type", resolved.workItemType);
        fields.put("project", resolved.projectId);
        if (resolved.defaultFields != null) {
            fields.putAll(resolved.defaultFields);
        }
        return fields;
    }

    private void createWorkItems(ResolvedRequest resolved,
                                 PolarionImportJobResult jobResult,
                                 Path resultJsonFile,
                                 Path previewCsvFile) throws IOException {
        jobResult.setStatus(JobStatus.CREATING_WORK_ITEMS.name());
        resultWriter.writeAtomic(resultJsonFile, jobResult);
        for (PolarionImportItemResult item : jobResult.getItems()) {
            if (!Boolean.TRUE.equals(item.getCandidate())) {
                continue;
            }
            item.setStatus(ItemStatus.CREATING.name());
            WorkItemCreateResult createResult = workItemCreator.createOne(buildCreateRequest(resolved, item));
            if (Boolean.TRUE.equals(createResult.getSuccess()) && TextUtils.hasText(createResult.getWorkItemId())) {
                item.setWorkItemId(createResult.getWorkItemId());
                item.setStatus(ItemStatus.CREATED.name());
                item.setErrorMessage(null);
            } else {
                item.setStatus(ItemStatus.CREATE_FAILED.name());
                item.setErrorMessage(formatCreateError(createResult));
            }
            updateSummary(jobResult, jobResult.getSummary().getParagraphCount());
            resultWriter.writeAtomic(resultJsonFile, jobResult);
            csvWriter.write(previewCsvFile, jobResult.getItems());
        }
    }

    private WorkItemCreateRequest buildCreateRequest(ResolvedRequest resolved, PolarionImportItemResult item) {
        WorkItemCreateRequest request = new WorkItemCreateRequest();
        request.setProjectId(resolved.projectId);
        request.setType(resolved.workItemType);
        request.setTitle(item.getTitle());
        request.setDescription(item.getDescription());
        request.setFields(item.getWorkItemCreateFields());
        return request;
    }

    private String formatCreateError(WorkItemCreateResult createResult) {
        if (createResult == null) {
            return "POLARION_API_EMPTY_RESULT";
        }
        if (TextUtils.hasText(createResult.getErrorCode())) {
            return createResult.getErrorCode() + ": " + createResult.getErrorMessage();
        }
        return createResult.getErrorMessage();
    }

    private void rewriteXml(ModuleXmlContent moduleXmlContent,
                            PolarionImportJobResult jobResult,
                            Path processedFile) throws IOException {
        jobResult.setStatus(JobStatus.REWRITING_XML.name());
        for (PolarionImportItemResult item : jobResult.getItems()) {
            if (ItemStatus.CREATED.name().equals(item.getStatus()) && TextUtils.hasText(item.getWorkItemId())) {
                item.setReplacementHtml(macroRenderer.render(item.getWorkItemId()));
            }
        }
        String processedXmlContent = moduleXmlRewriter.rewritePolarion(moduleXmlContent, jobResult.getItems());
        FileUtils.writeUtf8(processedFile, processedXmlContent);
        jobResult.getFiles().setProcessedXml(PROCESSED_XML_FILE);
    }

    private PolarionImportJobResult buildEmptyJobResult(String jobId, ResolvedRequest resolved) {
        PolarionImportJobResult result = new PolarionImportJobResult();
        result.setJobId(jobId);
        result.setProjectId(resolved.projectId);
        result.setModuleFolder(resolved.moduleFolder);
        result.setModuleName(resolved.moduleName);
        result.setWorkItemType(resolved.workItemType);
        result.setDryRun(resolved.dryRun);
        result.setStatus(JobStatus.ITEMS_READY.name());

        PolarionImportFiles files = new PolarionImportFiles();
        files.setOriginalXml(ORIGINAL_XML_FILE);
        files.setCsv(PREVIEW_CSV_FILE);
        result.setFiles(files);
        return result;
    }

    private void updateSummary(PolarionImportJobResult result, Integer paragraphCount) {
        PolarionImportSummary summary = new PolarionImportSummary();
        summary.setParagraphCount(paragraphCount == null ? 0 : paragraphCount);
        summary.setItemCount(result.getItems().size());
        summary.setCandidateCount(countCandidates(result.getItems()));
        summary.setCreatedCount(countCreated(result.getItems()));
        summary.setReplacedCount(countStatus(result.getItems(), ItemStatus.REPLACED));
        summary.setSkippedCount(countStatus(result.getItems(), ItemStatus.SKIPPED));
        summary.setFailedCount(countStatus(result.getItems(), ItemStatus.CREATE_FAILED)
                + countStatus(result.getItems(), ItemStatus.REPLACE_FAILED)
                + countStatus(result.getItems(), ItemStatus.FAILED));
        result.setSummary(summary);
    }

    private PolarionModuleImportResponse buildResponse(boolean success,
                                                       String message,
                                                       Path jobDir,
                                                       PolarionImportJobResult jobResult) {
        PolarionModuleImportResponse response = new PolarionModuleImportResponse();
        response.setSuccess(success);
        response.setJobId(jobResult.getJobId());
        response.setProjectId(jobResult.getProjectId());
        response.setModuleFolder(jobResult.getModuleFolder());
        response.setModuleName(jobResult.getModuleName());
        response.setDryRun(jobResult.getDryRun());
        response.setStatus(jobResult.getStatus());
        response.setParagraphCount(jobResult.getSummary().getParagraphCount());
        response.setItemCount(jobResult.getSummary().getItemCount());
        response.setCandidateCount(jobResult.getSummary().getCandidateCount());
        response.setCreatedCount(jobResult.getSummary().getCreatedCount());
        response.setReplacedCount(jobResult.getSummary().getReplacedCount());
        response.setSkippedCount(jobResult.getSummary().getSkippedCount());
        response.setFailedCount(jobResult.getSummary().getFailedCount());
        response.setOutputDir(jobDir.toString().replace('\\', '/'));
        response.setOriginalXmlFile(ORIGINAL_XML_FILE);
        response.setProcessedXmlFile(jobResult.getFiles().getProcessedXml());
        response.setResultJsonFile(RESULT_JSON_FILE);
        response.setPreviewCsvFile(PREVIEW_CSV_FILE);
        response.setMessage(message);
        return response;
    }

    private PolarionModuleImportResponse buildFailureResponse(String message,
                                                              Path jobDir,
                                                              PolarionImportJobResult jobResult) {
        PolarionModuleImportResponse response = buildResponse(false, message, jobDir, jobResult);
        response.setStatus(JobStatus.FAILED.name());
        return response;
    }

    private boolean hasErrors(List<PolarionImportItemResult> items) {
        for (PolarionImportItemResult item : items) {
            if (ItemStatus.CREATE_FAILED.name().equals(item.getStatus())
                    || ItemStatus.REPLACE_FAILED.name().equals(item.getStatus())
                    || ItemStatus.FAILED.name().equals(item.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private int countCandidates(List<PolarionImportItemResult> items) {
        int count = 0;
        for (PolarionImportItemResult item : items) {
            if (Boolean.TRUE.equals(item.getCandidate())) {
                count++;
            }
        }
        return count;
    }

    private int countCreated(List<PolarionImportItemResult> items) {
        int count = 0;
        for (PolarionImportItemResult item : items) {
            if (TextUtils.hasText(item.getWorkItemId())) {
                count++;
            }
        }
        return count;
    }

    private int countStatus(List<PolarionImportItemResult> items, ItemStatus status) {
        int count = 0;
        for (PolarionImportItemResult item : items) {
            if (status.name().equals(item.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private ResolvedRequest resolveRequest(PolarionModuleImportRequest request) {
        PolarionModuleImportRequest safeRequest = request == null ? new PolarionModuleImportRequest() : request;
        PolarionModuleLocation location = TextUtils.hasText(safeRequest.getModuleUrl())
                ? moduleUrlParser.parse(safeRequest.getModuleUrl())
                : null;
        ResolvedRequest resolved = new ResolvedRequest();
        resolved.baseUrl = firstText(location == null ? safeRequest.getBaseUrl() : location.getBaseUrl(), polarionProperties.getBaseUrl());
        resolved.projectId = firstText(location == null ? safeRequest.getProjectId() : location.getProjectId(), polarionProperties.getDefaultProjectId());
        resolved.moduleFolder = firstText(location == null ? safeRequest.getModuleFolder() : location.getModuleFolder(), polarionProperties.getDefaultModuleFolder());
        resolved.moduleName = firstText(location == null ? safeRequest.getModuleName() : location.getModuleName(), moduleProperties.getDefaultModuleName());
        resolved.workItemType = firstText(safeRequest.getWorkItemType(), polarionProperties.getDefaultWorkItemType());
        resolved.dryRun = safeRequest.getDryRun() == null ? false : safeRequest.getDryRun();
        resolved.requireKeyword = safeRequest.getRequireKeyword() == null
                ? Boolean.TRUE.equals(moduleProperties.getDefaultRequireKeyword())
                : safeRequest.getRequireKeyword();
        resolved.defaultFields = safeRequest.getDefaultFields() == null
                ? new LinkedHashMap<String, Object>()
                : safeRequest.getDefaultFields();
        return resolved;
    }

    private String firstText(String value, String fallback) {
        return TextUtils.hasText(value) ? value.trim() : fallback;
    }

    private String buildJobId(String moduleName) {
        return TextUtils.sanitizePathPart(moduleName) + "_" + JOB_TIME_FORMAT.format(LocalDateTime.now());
    }

    /**
     * 解析后的请求参数，避免在主流程中重复处理默认值。
     */
    private static class ResolvedRequest {
        private String baseUrl;
        private String projectId;
        private String moduleFolder;
        private String moduleName;
        private String workItemType;
        private boolean dryRun;
        private boolean requireKeyword;
        private Map<String, Object> defaultFields;
    }
}
