package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.ai.model.AiDebugRecord;
import com.example.polarionprocessor.ai.model.AiGenerateRequest;
import com.example.polarionprocessor.ai.model.AiGenerateResult;
import com.example.polarionprocessor.ai.service.WorkItemAiGenerationService;
import com.example.polarionprocessor.ai.writer.AiDebugWriter;
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
import com.example.polarionprocessor.model.polarion.PolarionCustomFieldRequest;
import com.example.polarionprocessor.model.polarion.PolarionEnumOptionRequest;
import com.example.polarionprocessor.model.polarion.PolarionModuleLocation;
import com.example.polarionprocessor.model.polarion.PolarionModuleImportRequest;
import com.example.polarionprocessor.model.polarion.PolarionModuleImportResponse;
import com.example.polarionprocessor.model.polarion.SvnCommitResult;
import com.example.polarionprocessor.model.polarion.WorkItemCreateApiRequest;
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
    private static final String PROCESSED_XML_FILE = "module.xml";
    private static final String RESULT_JSON_FILE = "import_result.json";
    private static final String PREVIEW_CSV_FILE = "import_preview.csv";
    private static final String AI_STATUS_CALLING = "CALLING";
    private static final String AI_STATUS_SUCCESS = "SUCCESS";
    private static final String AI_STATUS_FAILED = "FAILED";
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
    private final WorkItemCreateApiRequestBuilder apiRequestBuilder;
    private final ModuleWorkItemMacroRenderer macroRenderer;
    private final ModuleXmlRewriter moduleXmlRewriter;
    private final SvnModuleCommitter svnModuleCommitter;
    private final PolarionImportResultWriter resultWriter;
    private final PolarionImportPreviewCsvWriter csvWriter;
    private final WorkItemAiGenerationService aiGenerationService;
    private final AiDebugWriter aiDebugWriter;

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
                                       WorkItemCreateApiRequestBuilder apiRequestBuilder,
                                       ModuleWorkItemMacroRenderer macroRenderer,
                                       ModuleXmlRewriter moduleXmlRewriter,
                                       SvnModuleCommitter svnModuleCommitter,
                                       PolarionImportResultWriter resultWriter,
                                       PolarionImportPreviewCsvWriter csvWriter,
                                       WorkItemAiGenerationService aiGenerationService,
                                       AiDebugWriter aiDebugWriter) {
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
        this.apiRequestBuilder = apiRequestBuilder;
        this.macroRenderer = macroRenderer;
        this.moduleXmlRewriter = moduleXmlRewriter;
        this.svnModuleCommitter = svnModuleCommitter;
        this.resultWriter = resultWriter;
        this.csvWriter = csvWriter;
        this.aiGenerationService = aiGenerationService;
        this.aiDebugWriter = aiDebugWriter;
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
        Path aiDebugFile = jobDir.resolve(aiDebugWriter.fileName());

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

            generateAiFieldsIfNeeded(resolved, jobResult, resultJsonFile, previewCsvFile, aiDebugFile);

            if (resolved.dryRun) {
                return buildResponse(true, "Dry-run completed", jobDir, jobResult);
            }

            createWorkItems(resolved, jobResult, resultJsonFile, previewCsvFile);
            rewriteXml(moduleXmlContent, jobResult, processedFile);
            SvnCommitResult svnCommitResult = commitToSvn(resolved, jobId, jobDir, processedFile);
            jobResult.setSvnCommitResult(svnCommitResult);
            if (!Boolean.TRUE.equals(svnCommitResult.getSuccess())) {
                jobResult.setStatus(JobStatus.FAILED.name());
                updateSummary(jobResult, paragraphs.size());
                resultWriter.writeAtomic(resultJsonFile, jobResult);
                csvWriter.write(previewCsvFile, jobResult.getItems());
                return buildResponse(false, "SVN commit failed", jobDir, jobResult);
            }

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
                String ruleTitle = titleGenerator.generate(legacyItem, titleRequest);
                item.setRuleTitle(ruleTitle);
                item.setTitle(effectiveTitle(resolved, item));
                item.setStatus(ItemStatus.READY.name());
            } else {
                item.setStatus(ItemStatus.SKIPPED.name());
            }
            item.setCustomFields(buildApiRequest(resolved, item).getCustomFields());
            items.add(item);
        }
        return items;
    }

    private void generateAiFieldsIfNeeded(ResolvedRequest resolved,
                                          PolarionImportJobResult jobResult,
                                          Path resultJsonFile,
                                          Path previewCsvFile,
                                          Path aiDebugFile) throws IOException {
        if (!aiGenerationService.shouldRun(resolved.dryRun)) {
            return;
        }
        jobResult.getFiles().setAiDebug(aiDebugWriter.fileName());
        jobResult.setStatus(JobStatus.AI_GENERATING.name());
        resultWriter.writeAtomic(resultJsonFile, jobResult);
        csvWriter.write(previewCsvFile, jobResult.getItems());

        for (PolarionImportItemResult item : jobResult.getItems()) {
            if (!Boolean.TRUE.equals(item.getCandidate())) {
                continue;
            }
            item.setAiStatus(AI_STATUS_CALLING);
            item.setAiErrorMessage(null);
            item.setAiDebugRef(buildAiDebugRef(item));
            resultWriter.writeAtomic(resultJsonFile, jobResult);
            csvWriter.write(previewCsvFile, jobResult.getItems());

            AiGenerateRequest aiRequest = buildAiRequest(resolved, jobResult, item);
            AiGenerateResult aiResult = aiGenerationService.generate(aiRequest);
            applyAiResult(resolved, item, aiResult);
            aiDebugWriter.append(aiDebugFile, buildAiDebugRecord(jobResult, item, aiResult));
            item.setCustomFields(buildApiRequest(resolved, item).getCustomFields());

            updateSummary(jobResult, jobResult.getSummary().getParagraphCount());
            resultWriter.writeAtomic(resultJsonFile, jobResult);
            csvWriter.write(previewCsvFile, jobResult.getItems());
        }

        jobResult.setStatus(JobStatus.AI_COMPLETED.name());
        resultWriter.writeAtomic(resultJsonFile, jobResult);
        csvWriter.write(previewCsvFile, jobResult.getItems());
    }

    private AiGenerateRequest buildAiRequest(ResolvedRequest resolved,
                                             PolarionImportJobResult jobResult,
                                             PolarionImportItemResult item) {
        AiGenerateRequest request = new AiGenerateRequest();
        request.setJobId(jobResult.getJobId());
        request.setProjectId(resolved.projectId);
        request.setModuleName(resolved.moduleName);
        request.setItemSeq(item.getSeq());
        request.setItemKey(item.getItemKey());
        request.setOutlineNo(item.getOutlineNo());
        request.setRuleTitle(item.getRuleTitle());
        request.setDescription(item.getDescription());
        request.setEnumOptions(buildProjectEnumOptions(resolved.projectId));
        return request;
    }

    private void applyAiResult(ResolvedRequest resolved,
                               PolarionImportItemResult item,
                               AiGenerateResult aiResult) {
        if (aiResult == null) {
            item.setAiStatus(AI_STATUS_FAILED);
            item.setAiErrorMessage("AI result is empty");
            item.setTitle(effectiveTitle(resolved, item));
            item.setAiFields(new LinkedHashMap<String, Object>());
            return;
        }
        item.setAiPromptType(aiResult.getPromptType() == null ? null : aiResult.getPromptType().name());
        if (Boolean.TRUE.equals(aiResult.getSuccess())) {
            item.setAiStatus(AI_STATUS_SUCCESS);
            item.setAiErrorMessage(null);
            if (TextUtils.hasText(aiResult.getTitle())) {
                item.setAiTitle(aiResult.getTitle());
            }
            item.setAiFields(aiResult.getFields());
        } else {
            item.setAiStatus(AI_STATUS_FAILED);
            item.setAiErrorMessage(aiResult.getErrorMessage());
            item.setAiFields(new LinkedHashMap<String, Object>());
        }
        item.setTitle(effectiveTitle(resolved, item));
    }

    private AiDebugRecord buildAiDebugRecord(PolarionImportJobResult jobResult,
                                             PolarionImportItemResult item,
                                             AiGenerateResult aiResult) {
        AiDebugRecord record = new AiDebugRecord();
        record.setRef(item.getAiDebugRef());
        record.setJobId(jobResult.getJobId());
        record.setProjectId(jobResult.getProjectId());
        record.setModuleName(jobResult.getModuleName());
        record.setItemSeq(item.getSeq());
        record.setItemKey(item.getItemKey());
        record.setOutlineNo(item.getOutlineNo());
        if (aiResult != null) {
            record.setPromptType(aiResult.getPromptType() == null ? null : aiResult.getPromptType().name());
            record.setModel(aiResult.getModel());
            record.setSuccess(aiResult.getSuccess());
            record.setErrorMessage(aiResult.getErrorMessage());
            record.setPrompt(aiResult.getPrompt());
            record.setRawResponse(aiResult.getRawResponse());
            record.setParsedFields(aiResult.getParsedFields());
            Map<String, Object> acceptedFields = new LinkedHashMap<String, Object>();
            putIfNotNull(acceptedFields, "title", aiResult.getTitle());
            if (aiResult.getFields() != null) {
                acceptedFields.putAll(aiResult.getFields());
            }
            record.setAcceptedFields(acceptedFields);
            record.setUsage(aiResult.getUsage());
        } else {
            record.setSuccess(Boolean.FALSE);
            record.setErrorMessage("AI result is empty");
        }
        return record;
    }

    private String buildAiDebugRef(PolarionImportItemResult item) {
        Integer seq = item == null ? null : item.getSeq();
        if (seq == null) {
            return "ai-unknown";
        }
        return String.format("ai-%06d", seq);
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
        fields.put("polarionId", resolved.projectId);
        fields.put("authorId", resolved.authorId);
        putIfNotNull(fields, "authorName", resolved.authorName);
        putIfNotNull(fields, "status", resolved.status);
        putIfNotNull(fields, "wkId", resolved.wkId);
        putIfNotNull(fields, "assigneeIds", resolved.assigneeIds);
        putIfNotNull(fields, "dueDate", resolved.dueDate);
        putIfNotNull(fields, "startDate", resolved.startDate);
        putIfNotNull(fields, "parentWkId", resolved.parentWkId);
        putIfNotNull(fields, "isNewPdp", resolved.isNewPdp);
        putIfNotNull(fields, "onlyCreate", resolved.onlyCreate);
        putIfNotNull(fields, "commentContent", resolved.commentContent);
        putIfNotNull(fields, "removedLink", resolved.removedLink);
        putIfNotNull(fields, "initialEstimate", resolved.initialEstimate);
        putIfNotNull(fields, "timeSpent", resolved.timeSpent);
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
            item.setCustomFields(buildApiRequest(resolved, item).getCustomFields());
            item.setStatus(ItemStatus.CREATING.name());
            WorkItemCreateResult createResult = createOneSafely(resolved, item);
            if (createResult != null
                    && Boolean.TRUE.equals(createResult.getSuccess())
                    && TextUtils.hasText(createResult.getWorkItemId())) {
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

    private WorkItemCreateResult createOneSafely(ResolvedRequest resolved, PolarionImportItemResult item) {
        try {
            return workItemCreator.createOne(buildCreateRequest(resolved, item));
        } catch (RuntimeException e) {
            return WorkItemCreateResult.failure(
                    "POLARION_API_EXCEPTION",
                    e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private WorkItemCreateRequest buildCreateRequest(ResolvedRequest resolved, PolarionImportItemResult item) {
        WorkItemCreateRequest request = new WorkItemCreateRequest();
        request.setProjectId(resolved.projectId);
        request.setType(resolved.workItemType);
        request.setTitle(effectiveTitle(resolved, item));
        request.setDescription(item.getDescription());
        request.setAuthorName(resolved.authorName);
        request.setAuthorId(resolved.authorId);
        request.setStatus(resolved.status);
        request.setWkId(resolved.wkId);
        request.setAssigneeIds(resolved.assigneeIds);
        request.setDueDate(resolved.dueDate);
        request.setStartDate(resolved.startDate);
        request.setParentWkId(resolved.parentWkId);
        request.setIsNewPdp(resolved.isNewPdp);
        request.setOnlyCreate(resolved.onlyCreate);
        request.setCommentContent(resolved.commentContent);
        request.setRemovedLink(resolved.removedLink);
        request.setInitialEstimate(resolved.initialEstimate);
        request.setTimeSpent(resolved.timeSpent);
        request.setFields(buildRequestFields(resolved, item));
        request.setCustomFields(resolved.customFields);
        return request;
    }

    private Map<String, Object> buildRequestFields(ResolvedRequest resolved, PolarionImportItemResult item) {
        Map<String, Object> fields = new LinkedHashMap<String, Object>();
        if (item != null && item.getAiFields() != null) {
            fields.putAll(item.getAiFields());
        }
        if (resolved.defaultFields != null) {
            fields.putAll(resolved.defaultFields);
        }
        return fields;
    }

    private WorkItemCreateApiRequest buildApiRequest(ResolvedRequest resolved, PolarionImportItemResult item) {
        return apiRequestBuilder.build(buildCreateRequest(resolved, item));
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

    private SvnCommitResult commitToSvn(ResolvedRequest resolved,
                                        String jobId,
                                        Path jobDir,
                                        Path processedFile) {
        String commitMessage = polarionProperties.getSvn() == null
                ? null
                : polarionProperties.getSvn().getDefaultCommitMessage();
        return svnModuleCommitter.commit(
                jobId,
                jobDir,
                resolved.baseUrl,
                resolved.projectId,
                resolved.moduleFolder,
                resolved.moduleName,
                processedFile,
                commitMessage);
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
        response.setAiDebugFile(jobResult.getFiles().getAiDebug());
        if (jobResult.getSvnCommitResult() != null) {
            response.setSvnCommitStatus(jobResult.getSvnCommitResult().getStatus());
            response.setSvnRevision(jobResult.getSvnCommitResult().getRevision());
            response.setSvnErrorMessage(jobResult.getSvnCommitResult().getErrorMessage());
        }
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

    private String effectiveTitle(ResolvedRequest resolved, PolarionImportItemResult item) {
        return firstText(
                resolved == null || resolved.defaultFields == null ? null : stringValue(resolved.defaultFields.get("title")),
                item == null ? null : item.getAiTitle(),
                item == null ? null : item.getRuleTitle(),
                item == null ? null : item.getTitle());
    }

    private Map<String, List<PolarionEnumOptionRequest>> buildProjectEnumOptions(String projectId) {
        Map<String, List<PolarionEnumOptionRequest>> enumOptions =
                new LinkedHashMap<String, List<PolarionEnumOptionRequest>>();
        PolarionProperties.WorkItemApi api = polarionProperties.getWorkItemApi();
        List<PolarionCustomFieldRequest> customFields = projectCustomFields(api, projectId);
        if (customFields == null) {
            return enumOptions;
        }
        for (PolarionCustomFieldRequest field : customFields) {
            if (field == null || !TextUtils.hasText(field.getId())
                    || field.getEnumOptions() == null || field.getEnumOptions().isEmpty()) {
                continue;
            }
            enumOptions.put(canonicalCustomFieldId(field.getId()), copyEnumOptions(field.getEnumOptions()));
        }
        return enumOptions;
    }

    private List<PolarionCustomFieldRequest> projectCustomFields(PolarionProperties.WorkItemApi api, String projectId) {
        if (api == null || api.getProjectCustomFields() == null || !TextUtils.hasText(projectId)) {
            return null;
        }
        List<PolarionCustomFieldRequest> fields = api.getProjectCustomFields().get(projectId);
        if (fields != null) {
            return fields;
        }
        for (Map.Entry<String, List<PolarionCustomFieldRequest>> entry : api.getProjectCustomFields().entrySet()) {
            if (entry.getKey() != null && projectId.trim().equalsIgnoreCase(entry.getKey().trim())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private List<PolarionEnumOptionRequest> copyEnumOptions(List<PolarionEnumOptionRequest> source) {
        List<PolarionEnumOptionRequest> copy = new ArrayList<PolarionEnumOptionRequest>();
        if (source == null) {
            return copy;
        }
        for (PolarionEnumOptionRequest option : source) {
            if (option != null) {
                copy.add(new PolarionEnumOptionRequest(option.getId(), option.getName()));
            }
        }
        return copy;
    }

    private String canonicalCustomFieldId(String id) {
        if (!TextUtils.hasText(id)) {
            return id;
        }
        if ("requirementsouce".equalsIgnoreCase(id.trim())) {
            return "requirementsource";
        }
        return id.trim();
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
        PolarionProperties.WorkItemApi api = polarionProperties.getWorkItemApi();
        resolved.projectId = firstText(
                location == null ? safeRequest.getProjectId() : location.getProjectId(),
                api == null ? null : api.getDefaultPolarionId(),
                polarionProperties.getDefaultProjectId());
        resolved.moduleFolder = firstText(location == null ? safeRequest.getModuleFolder() : location.getModuleFolder(), polarionProperties.getDefaultModuleFolder());
        resolved.moduleName = firstText(location == null ? safeRequest.getModuleName() : location.getModuleName(), moduleProperties.getDefaultModuleName());
        resolved.workItemType = firstText(
                safeRequest.getWorkItemType(),
                api == null ? null : api.getDefaultType(),
                polarionProperties.getDefaultWorkItemType());
        resolved.authorName = safeRequest.getAuthorName();
        resolved.authorId = firstText(safeRequest.getAuthorId(), safeRequest.getAuthorName(), api == null ? null : api.getDefaultAuthorId());
        resolved.status = safeRequest.getStatus();
        resolved.wkId = safeRequest.getWkId();
        resolved.assigneeIds = safeRequest.getAssigneeIds();
        resolved.dueDate = safeRequest.getDueDate();
        resolved.startDate = safeRequest.getStartDate();
        resolved.parentWkId = safeRequest.getParentWkId();
        resolved.isNewPdp = safeRequest.getIsNewPdp();
        resolved.onlyCreate = safeRequest.getOnlyCreate();
        resolved.commentContent = safeRequest.getCommentContent();
        resolved.removedLink = safeRequest.getRemovedLink();
        resolved.initialEstimate = safeRequest.getInitialEstimate();
        resolved.timeSpent = safeRequest.getTimeSpent();
        resolved.dryRun = safeRequest.getDryRun() == null ? false : safeRequest.getDryRun();
        resolved.requireKeyword = safeRequest.getRequireKeyword() == null
                ? Boolean.TRUE.equals(moduleProperties.getDefaultRequireKeyword())
                : safeRequest.getRequireKeyword();
        resolved.defaultFields = safeRequest.getDefaultFields() == null
                ? new LinkedHashMap<String, Object>()
                : safeRequest.getDefaultFields();
        resolved.customFields = safeRequest.getCustomFields() == null
                ? new ArrayList<PolarionCustomFieldRequest>()
                : safeRequest.getCustomFields();
        return resolved;
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (TextUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private void putIfNotNull(Map<String, Object> fields, String key, Object value) {
        if (value != null) {
            fields.put(key, value);
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
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
        private String authorName;
        private String authorId;
        private String status;
        private String wkId;
        private List<String> assigneeIds;
        private String dueDate;
        private String startDate;
        private String parentWkId;
        private Boolean isNewPdp;
        private Boolean onlyCreate;
        private String commentContent;
        private Boolean removedLink;
        private String initialEstimate;
        private String timeSpent;
        private boolean dryRun;
        private boolean requireKeyword;
        private Map<String, Object> defaultFields;
        private List<PolarionCustomFieldRequest> customFields;
    }
}
