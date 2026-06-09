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
import com.example.polarionprocessor.enums.PolarionItemRole;
import com.example.polarionprocessor.model.debug.ModuleProcessRequest;
import com.example.polarionprocessor.model.shared.ImportItemResult;
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
import com.example.polarionprocessor.service.shared.ParagraphScanner;
import com.example.polarionprocessor.service.shared.TitleGenerator;
import com.example.polarionprocessor.util.FileUtils;
import com.example.polarionprocessor.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarionModuleImportService.class);

    private static final String ORIGINAL_XML_FILE = "original_module.xml";
    private static final String PROCESSED_XML_FILE = "module.xml";
    private static final String RESULT_JSON_FILE = "import_result.json";
    private static final String PREVIEW_CSV_FILE = "import_preview.csv";
    private static final String PROGRESS_LOG_FILE = "progress.log";
    private static final String AI_STATUS_CALLING = "CALLING";
    private static final String AI_STATUS_SUCCESS = "SUCCESS";
    private static final String AI_STATUS_FAILED = "FAILED";
    private static final String AI_STATUS_SKIPPED = "SKIPPED";
    private static final DateTimeFormatter JOB_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private final ModuleProcessorProperties moduleProperties;
    private final PolarionProperties polarionProperties;
    private final PolarionModuleUrlParser moduleUrlParser;
    private final ModuleXmlDownloader moduleXmlDownloader;
    private final ModuleXmlExtractor moduleXmlExtractor;
    private final ParagraphScanner paragraphScanner;
    private final PolarionDocumentItemBuilder documentItemBuilder;
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
    private final PolarionProgressLogWriter progressLogWriter;
    private final PolarionImportCallbackNotifier callbackNotifier;

    public PolarionModuleImportService(ModuleProcessorProperties moduleProperties,
                                       PolarionProperties polarionProperties,
                                       PolarionModuleUrlParser moduleUrlParser,
                                       ModuleXmlDownloader moduleXmlDownloader,
                                       ModuleXmlExtractor moduleXmlExtractor,
                                       ParagraphScanner paragraphScanner,
                                       PolarionDocumentItemBuilder documentItemBuilder,
                                       TitleGenerator titleGenerator,
                                       PolarionWorkItemCreator workItemCreator,
                                       WorkItemCreateApiRequestBuilder apiRequestBuilder,
                                       ModuleWorkItemMacroRenderer macroRenderer,
                                       ModuleXmlRewriter moduleXmlRewriter,
                                       SvnModuleCommitter svnModuleCommitter,
                                       PolarionImportResultWriter resultWriter,
                                       PolarionImportPreviewCsvWriter csvWriter,
                                       WorkItemAiGenerationService aiGenerationService,
                                       AiDebugWriter aiDebugWriter,
                                       PolarionProgressLogWriter progressLogWriter,
                                       PolarionImportCallbackNotifier callbackNotifier) {
        this.moduleProperties = moduleProperties;
        this.polarionProperties = polarionProperties;
        this.moduleUrlParser = moduleUrlParser;
        this.moduleXmlDownloader = moduleXmlDownloader;
        this.moduleXmlExtractor = moduleXmlExtractor;
        this.paragraphScanner = paragraphScanner;
        this.documentItemBuilder = documentItemBuilder;
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
        this.progressLogWriter = progressLogWriter;
        this.callbackNotifier = callbackNotifier;
    }

    /**
     * 执行正式导入流程。
     */
    public PolarionModuleImportResponse importModule(PolarionModuleImportRequest request) {
        ResolvedRequest resolved = resolveRequest(request);
        String jobId = firstText(resolved.jobId, buildJobId(resolved.moduleName));
        resolved.jobId = jobId;
        Path jobDir = Paths.get(moduleProperties.getOutputDir()).resolve(jobId);
        Path originalFile = jobDir.resolve(ORIGINAL_XML_FILE);
        Path processedFile = jobDir.resolve(PROCESSED_XML_FILE);
        Path resultJsonFile = jobDir.resolve(RESULT_JSON_FILE);
        Path previewCsvFile = jobDir.resolve(PREVIEW_CSV_FILE);
        Path aiDebugFile = jobDir.resolve(aiDebugWriter.fileName());
        Path progressLogFile = jobDir.resolve(progressLogWriter.fileName());

        PolarionImportJobResult jobResult = buildEmptyJobResult(jobId, resolved);
        try {
            FileUtils.ensureDirectory(jobDir);
            appendProgress(progressLogFile, "任务已启动：项目 " + resolved.projectId
                    + "，文档 " + resolved.moduleName + "。");
            appendProgress(progressLogFile, "正在从 Polarion SVN 下载 module.xml。");
            String xmlContent = moduleXmlDownloader.download(resolved.baseUrl, resolved.projectId, resolved.moduleFolder, resolved.moduleName);
            FileUtils.writeUtf8(originalFile, xmlContent);
            appendProgress(progressLogFile, "module.xml 下载完成，已保存原始文件。");

            appendProgress(progressLogFile, "正在解析文档内容并识别 heading/stakeholderrequirement。");
            ModuleXmlContent moduleXmlContent = moduleXmlExtractor.extract(xmlContent);
            List<ParagraphInfo> paragraphs = paragraphScanner.scan(moduleXmlContent.getHtmlContent());
            List<PolarionImportItemResult> items = buildItems(resolved, moduleXmlContent.getHtmlContent(), paragraphs);

            jobResult.setItems(items);
            jobResult.setStatus(JobStatus.ITEMS_READY.name());
            updateSummary(jobResult, paragraphs.size());
            appendProgress(progressLogFile, "识别完成：共扫描段落 " + paragraphs.size()
                    + " 个，heading " + jobResult.getSummary().getHeadingCount()
                    + " 个，stakeholderrequirement " + jobResult.getSummary().getRequirementCount()
                    + " 个，忽略 " + jobResult.getSummary().getSkippedCount() + " 个。");
            resultWriter.writeAtomic(resultJsonFile, jobResult);
            csvWriter.write(previewCsvFile, items);

            generateAiFieldsIfNeeded(resolved, jobResult, resultJsonFile, previewCsvFile, aiDebugFile, progressLogFile);

            if (resolved.dryRun) {
                appendProgress(progressLogFile, "试运行完成：未创建 Work Item，也未提交 SVN。");
                return notifyAndReturn(buildResponse(true, "Dry-run completed", jobDir, jobResult));
            }

            createWorkItems(resolved, jobResult, resultJsonFile, previewCsvFile, progressLogFile);
            appendProgress(progressLogFile, "正在把创建成功的 Work Item 回写到 module.xml。");
            rewriteXml(moduleXmlContent, jobResult, processedFile);
            appendProgress(progressLogFile, "module.xml 回写完成，正在提交 SVN。");
            SvnCommitResult svnCommitResult = commitToSvn(resolved, jobId, jobDir, processedFile);
            jobResult.setSvnCommitResult(svnCommitResult);
            if (!Boolean.TRUE.equals(svnCommitResult.getSuccess())) {
                appendProgress(progressLogFile, "SVN 提交失败：" + svnCommitResult.getErrorMessage());
                jobResult.setStatus(JobStatus.FAILED.name());
                updateSummary(jobResult, paragraphs.size());
                resultWriter.writeAtomic(resultJsonFile, jobResult);
                csvWriter.write(previewCsvFile, jobResult.getItems());
                return notifyAndReturn(buildResponse(false, "SVN commit failed", jobDir, jobResult));
            }
            appendProgress(progressLogFile, "SVN 提交完成。");

            jobResult.setStatus(hasErrors(jobResult.getItems())
                    ? JobStatus.COMPLETED_WITH_ERRORS.name()
                    : JobStatus.COMPLETED.name());
            updateSummary(jobResult, paragraphs.size());
            resultWriter.writeAtomic(resultJsonFile, jobResult);
            csvWriter.write(previewCsvFile, jobResult.getItems());
            appendProgress(progressLogFile, "任务完成：heading 创建 " + jobResult.getSummary().getHeadingCreatedCount()
                    + " 个，stakeholderrequirement 创建 " + jobResult.getSummary().getRequirementCreatedCount()
                    + " 个，失败 " + jobResult.getSummary().getFailedCount() + " 个。");
            return notifyAndReturn(buildResponse(true, "Polarion module import completed", jobDir, jobResult));
        } catch (ModuleProcessException e) {
            jobResult.setStatus(JobStatus.FAILED.name());
            appendProgress(progressLogFile, "任务失败：" + e.getErrorCode() + ": " + e.getMessage());
            return notifyAndReturn(buildFailureResponse(e.getErrorCode() + ": " + e.getMessage(), jobDir, jobResult));
        } catch (IOException e) {
            jobResult.setStatus(JobStatus.FAILED.name());
            appendProgress(progressLogFile, "任务失败：文件写入异常，" + e.getMessage());
            return notifyAndReturn(buildFailureResponse("FILE_WRITE_FAILED: " + e.getMessage(), jobDir, jobResult));
        } catch (RuntimeException e) {
            jobResult.setStatus(JobStatus.FAILED.name());
            appendProgress(progressLogFile, "任务失败：" + e.getClass().getSimpleName() + ": " + e.getMessage());
            return notifyAndReturn(buildFailureResponse("FAILED: " + e.getClass().getSimpleName() + ": " + e.getMessage(), jobDir, jobResult));
        }
    }

    private List<PolarionImportItemResult> buildItems(ResolvedRequest resolved,
                                                      String htmlContent,
                                                      List<ParagraphInfo> paragraphs) {
        List<PolarionImportItemResult> items = documentItemBuilder.build(resolved.moduleName, htmlContent, paragraphs);
        ModuleProcessRequest titleRequest = new ModuleProcessRequest();
        for (PolarionImportItemResult item : items) {
            if (isRequirement(item)) {
                String ruleTitle = titleGenerator.generate(buildLegacyTitleItem(item), titleRequest);
                item.setRuleTitle(ruleTitle);
                item.setTitle(effectiveTitle(resolved, item));
            }
            item.setWorkItemCreateFields(buildCreateFields(resolved, item));
            item.setCustomFields(buildApiRequest(resolved, item).getCustomFields());
        }
        return items;
    }

    private void generateAiFieldsIfNeeded(ResolvedRequest resolved,
                                          PolarionImportJobResult jobResult,
                                          Path resultJsonFile,
                                          Path previewCsvFile,
                                          Path aiDebugFile,
                                          Path progressLogFile) throws IOException {
        if (!aiGenerationService.shouldRun(resolved.dryRun)) {
            LOGGER.info("AI generation skipped by configuration: dryRun={}", resolved.dryRun);
            appendProgress(progressLogFile, "AI 生成已按配置跳过。");
            return;
        }
        jobResult.getFiles().setAiDebug(aiDebugWriter.fileName());
        jobResult.setStatus(JobStatus.AI_GENERATING.name());
        resultWriter.writeAtomic(resultJsonFile, jobResult);
        csvWriter.write(previewCsvFile, jobResult.getItems());

        int requirementCount = countRole(jobResult.getItems(), PolarionItemRole.REQUIREMENT);
        LOGGER.info("AI generation started: jobId={}, projectId={}, requirementCount={}, dryRun={}",
                jobResult.getJobId(),
                resolved.projectId,
                requirementCount,
                resolved.dryRun);
        appendProgress(progressLogFile, "开始 AI 生成字段：stakeholderrequirement 共 " + requirementCount + " 个；heading 不需要 AI。");
        if (requirementCount == 0) {
            markNonRequirementsAiSkipped(jobResult);
            jobResult.setStatus(JobStatus.AI_SKIPPED.name());
            aiDebugWriter.append(aiDebugFile, buildNoCandidateAiDebugRecord(jobResult));
            resultWriter.writeAtomic(resultJsonFile, jobResult);
            csvWriter.write(previewCsvFile, jobResult.getItems());
            LOGGER.info("AI generation skipped: no requirement items, jobId={}", jobResult.getJobId());
            appendProgress(progressLogFile, "没有 stakeholderrequirement，AI 生成阶段跳过。");
            return;
        }

        int requirementIndex = 0;
        for (PolarionImportItemResult item : jobResult.getItems()) {
            if (!isRequirement(item)) {
                item.setAiStatus(AI_STATUS_SKIPPED);
                continue;
            }
            requirementIndex++;
            item.setAiStatus(AI_STATUS_CALLING);
            item.setAiErrorMessage(null);
            item.setAiDebugRef(buildAiDebugRef(item));
            appendProgress(progressLogFile, "正在进行 AI 生成字段：" + formatItemProgress(requirementIndex, requirementCount, item) + "。");
            LOGGER.info("Generating AI fields: jobId={}, seq={}, outlineNo={}, itemKey={}",
                    jobResult.getJobId(),
                    item.getSeq(),
                    item.getOutlineNo(),
                    item.getItemKey());
            resultWriter.writeAtomic(resultJsonFile, jobResult);
            csvWriter.write(previewCsvFile, jobResult.getItems());

            AiGenerateRequest aiRequest = buildAiRequest(resolved, jobResult, item);
            AiGenerateResult aiResult = aiGenerationService.generate(aiRequest);
            applyAiResult(resolved, item, aiResult);
            aiDebugWriter.append(aiDebugFile, buildAiDebugRecord(jobResult, item, aiResult));
            item.setCustomFields(buildApiRequest(resolved, item).getCustomFields());
            appendProgress(progressLogFile, formatAiProgressResult(requirementIndex, requirementCount, item));

            updateSummary(jobResult, jobResult.getSummary().getParagraphCount());
            resultWriter.writeAtomic(resultJsonFile, jobResult);
            csvWriter.write(previewCsvFile, jobResult.getItems());
            LOGGER.info("AI fields generated: jobId={}, seq={}, aiStatus={}, title={}",
                    jobResult.getJobId(),
                    item.getSeq(),
                    item.getAiStatus(),
                    TextUtils.truncateAtWordBoundary(item.getTitle(), 80));
        }

        jobResult.setStatus(JobStatus.AI_COMPLETED.name());
        resultWriter.writeAtomic(resultJsonFile, jobResult);
        csvWriter.write(previewCsvFile, jobResult.getItems());
        LOGGER.info("AI generation finished: jobId={}, requirementCount={}", jobResult.getJobId(), requirementCount);
        appendProgress(progressLogFile, "AI 字段生成阶段完成。");
    }

    private void markNonRequirementsAiSkipped(PolarionImportJobResult jobResult) {
        for (PolarionImportItemResult item : jobResult.getItems()) {
            if (!isRequirement(item)) {
                item.setAiStatus(AI_STATUS_SKIPPED);
            }
        }
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

    private AiDebugRecord buildNoCandidateAiDebugRecord(PolarionImportJobResult jobResult) {
        AiDebugRecord record = new AiDebugRecord();
        record.setRef("ai-no-candidate");
        record.setJobId(jobResult.getJobId());
        record.setProjectId(jobResult.getProjectId());
        record.setModuleName(jobResult.getModuleName());
        record.setSuccess(Boolean.TRUE);
        record.setErrorMessage("No requirement items; AI was not called");
        return record;
    }

    private String buildAiDebugRef(PolarionImportItemResult item) {
        Integer seq = item == null ? null : item.getSeq();
        if (seq == null) {
            return "ai-unknown";
        }
        return String.format("ai-%06d", seq);
    }

    private ImportItemResult buildLegacyTitleItem(PolarionImportItemResult item) {
        ImportItemResult legacyItem = new ImportItemResult();
        legacyItem.setSeq(item.getSeq());
        legacyItem.setParagraphId(item.getStartParagraphId());
        legacyItem.setStartParagraphId(item.getStartParagraphId());
        legacyItem.setEndParagraphId(item.getEndParagraphId());
        legacyItem.setOutlineNo(item.getOutlineNo());
        legacyItem.setSourceText(item.getDescription());
        legacyItem.setDescription(item.getDescription());
        legacyItem.setCandidate(item.getCandidate());
        legacyItem.setSkipReason(item.getSkipReason());
        legacyItem.setSourceStartIndex(item.getSourceStartIndex());
        legacyItem.setSourceEndIndex(item.getSourceEndIndex());
        return legacyItem;
    }

    private Map<String, Object> buildCreateFields(ResolvedRequest resolved, PolarionImportItemResult item) {
        Map<String, Object> fields = new LinkedHashMap<String, Object>();
        fields.put("type", item == null ? resolved.workItemType : firstText(item.getWorkItemType(), resolved.workItemType));
        fields.put("polarionId", resolved.projectId);
        fields.put("authorId", resolved.authorId);
        putIfNotNull(fields, "authorName", resolved.authorName);
        putIfNotNull(fields, "status", resolved.status);
        putIfNotNull(fields, "wkId", resolved.wkId);
        putIfNotNull(fields, "assigneeIds", resolved.assigneeIds);
        putIfNotNull(fields, "dueDate", resolved.dueDate);
        putIfNotNull(fields, "startDate", resolved.startDate);
        putIfNotNull(fields, "parentWkId", item == null ? resolved.parentWkId : firstText(item.getParentWkId(), topLevelParentWkId(resolved, item)));
        putIfNotNull(fields, "moduleURI", resolved.moduleURI);
        putIfNotNull(fields, "isNewPdp", resolved.isNewPdp);
        putIfNotNull(fields, "onlyCreate", resolved.onlyCreate);
        putIfNotNull(fields, "commentContent", resolved.commentContent);
        putIfNotNull(fields, "removedLink", resolved.removedLink);
        putIfNotNull(fields, "initialEstimate", resolved.initialEstimate);
        putIfNotNull(fields, "timeSpent", resolved.timeSpent);
        if (!isHeading(item) && resolved.defaultFields != null) {
            fields.putAll(resolved.defaultFields);
        }
        return fields;
    }

    private void createWorkItems(ResolvedRequest resolved,
                                 PolarionImportJobResult jobResult,
                                 Path resultJsonFile,
                                 Path previewCsvFile,
                                 Path progressLogFile) throws IOException {
        jobResult.setStatus(JobStatus.CREATING_WORK_ITEMS.name());
        resultWriter.writeAtomic(resultJsonFile, jobResult);
        createHeadingWorkItems(resolved, jobResult, resultJsonFile, previewCsvFile, progressLogFile);
        createRequirementWorkItems(resolved, jobResult, resultJsonFile, previewCsvFile, progressLogFile);
        appendProgress(progressLogFile, "Work Item 创建阶段完成。");
    }

    private void createHeadingWorkItems(ResolvedRequest resolved,
                                        PolarionImportJobResult jobResult,
                                        Path resultJsonFile,
                                        Path previewCsvFile,
                                        Path progressLogFile) throws IOException {
        int headingCount = countRole(jobResult.getItems(), PolarionItemRole.HEADING);
        int headingIndex = 0;
        appendProgress(progressLogFile, "开始创建 heading：共 " + headingCount + " 个。");
        for (PolarionImportItemResult item : jobResult.getItems()) {
            if (!isHeading(item)) {
                continue;
            }
            headingIndex++;
            if (!resolveParentWkIdForCreate(jobResult, item)) {
                markCreateBlocked(item, "PARENT_HEADING_CREATE_FAILED", "Parent heading is not available: " + item.getParentOutlineNo());
                appendProgress(progressLogFile, "heading 创建阻断：" + formatItemProgress(headingIndex, headingCount, item)
                        + "，原因=" + item.getErrorMessage() + "。");
            } else {
                createOneAndRecord(resolved, jobResult, item, headingIndex, headingCount, "heading", progressLogFile);
            }
            updateSummary(jobResult, jobResult.getSummary().getParagraphCount());
            resultWriter.writeAtomic(resultJsonFile, jobResult);
            csvWriter.write(previewCsvFile, jobResult.getItems());
        }
        updateSummary(jobResult, jobResult.getSummary().getParagraphCount());
        appendProgress(progressLogFile, "heading 创建阶段完成：成功 "
                + jobResult.getSummary().getHeadingCreatedCount()
                + " 个，失败 " + countFailedRole(jobResult.getItems(), PolarionItemRole.HEADING) + " 个。");
    }

    private void createRequirementWorkItems(ResolvedRequest resolved,
                                            PolarionImportJobResult jobResult,
                                            Path resultJsonFile,
                                            Path previewCsvFile,
                                            Path progressLogFile) throws IOException {
        int requirementCount = countRole(jobResult.getItems(), PolarionItemRole.REQUIREMENT);
        int requirementIndex = 0;
        appendProgress(progressLogFile, "开始创建 stakeholderrequirement：共 " + requirementCount + " 个。");
        for (PolarionImportItemResult item : jobResult.getItems()) {
            if (!isRequirement(item)) {
                continue;
            }
            requirementIndex++;
            if (!resolveParentWkIdForCreate(jobResult, item)) {
                markCreateBlocked(item, "PARENT_HEADING_CREATE_FAILED", "Parent heading is not available: " + item.getParentOutlineNo());
                appendProgress(progressLogFile, "stakeholderrequirement 创建阻断：" + formatItemProgress(requirementIndex, requirementCount, item)
                        + "，原因=" + item.getErrorMessage() + "。");
            } else {
                if (!TextUtils.hasText(item.getParentWkId()) && !TextUtils.hasText(item.getParentOutlineNo())) {
                    item.setParentWkId(topLevelParentWkId(resolved, item));
                }
                createOneAndRecord(resolved, jobResult, item, requirementIndex, requirementCount, "stakeholderrequirement", progressLogFile);
            }
            updateSummary(jobResult, jobResult.getSummary().getParagraphCount());
            resultWriter.writeAtomic(resultJsonFile, jobResult);
            csvWriter.write(previewCsvFile, jobResult.getItems());
        }
        updateSummary(jobResult, jobResult.getSummary().getParagraphCount());
        appendProgress(progressLogFile, "stakeholderrequirement 创建阶段完成：成功 "
                + jobResult.getSummary().getRequirementCreatedCount()
                + " 个，失败 " + countFailedRole(jobResult.getItems(), PolarionItemRole.REQUIREMENT) + " 个。");
    }

    private void createOneAndRecord(ResolvedRequest resolved,
                                    PolarionImportJobResult jobResult,
                                    PolarionImportItemResult item,
                                    int index,
                                    int total,
                                    String roleName,
                                    Path progressLogFile) {
        item.setWorkItemCreateFields(buildCreateFields(resolved, item));
        item.setCustomFields(buildApiRequest(resolved, item).getCustomFields());
        item.setStatus(ItemStatus.CREATING.name());
        appendProgress(progressLogFile, "正在创建 " + roleName + "：" + formatItemProgress(index, total, item) + "。");
        WorkItemCreateResult createResult = createOneSafely(resolved, item);
        if (createResult != null
                && Boolean.TRUE.equals(createResult.getSuccess())
                && TextUtils.hasText(createResult.getWorkItemId())) {
            item.setWorkItemId(createResult.getWorkItemId());
            item.setStatus(ItemStatus.CREATED.name());
            item.setErrorMessage(null);
            appendProgress(progressLogFile, roleName + " 创建成功：" + formatItemProgress(index, total, item)
                    + "，workItemId=" + createResult.getWorkItemId() + "。");
        } else {
            item.setStatus(ItemStatus.CREATE_FAILED.name());
            item.setErrorMessage(formatCreateError(createResult));
            appendProgress(progressLogFile, roleName + " 创建失败：" + formatItemProgress(index, total, item)
                    + "，原因=" + item.getErrorMessage() + "。");
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
        request.setType(item == null ? resolved.workItemType : firstText(item.getWorkItemType(), resolved.workItemType));
        request.setTitle(effectiveTitle(resolved, item));
        request.setDescription(item == null || isHeading(item) ? null : item.getDescription());
        request.setAuthorName(resolved.authorName);
        request.setAuthorId(resolved.authorId);
        request.setStatus(resolved.status);
        request.setWkId(resolved.wkId);
        request.setAssigneeIds(resolved.assigneeIds);
        request.setDueDate(resolved.dueDate);
        request.setStartDate(resolved.startDate);
        request.setParentWkId(item == null ? resolved.parentWkId : firstText(item.getParentWkId(), topLevelParentWkId(resolved, item)));
        request.setModuleURI(resolved.moduleURI);
        request.setIsNewPdp(resolved.isNewPdp);
        request.setOnlyCreate(resolved.onlyCreate);
        request.setCommentContent(resolved.commentContent);
        request.setRemovedLink(resolved.removedLink);
        request.setInitialEstimate(resolved.initialEstimate);
        request.setTimeSpent(resolved.timeSpent);
        request.setFields(buildRequestFields(resolved, item));
        request.setCustomFields(isHeading(item) ? new ArrayList<PolarionCustomFieldRequest>() : resolved.customFields);
        return request;
    }

    private Map<String, Object> buildRequestFields(ResolvedRequest resolved, PolarionImportItemResult item) {
        Map<String, Object> fields = new LinkedHashMap<String, Object>();
        if (isHeading(item)) {
            return fields;
        }
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
                item.setReplacementHtml(macroRenderer.render(item));
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
        files.setProgressLog(PROGRESS_LOG_FILE);
        result.setFiles(files);
        return result;
    }

    private void updateSummary(PolarionImportJobResult result, Integer paragraphCount) {
        PolarionImportSummary summary = new PolarionImportSummary();
        summary.setParagraphCount(paragraphCount == null ? 0 : paragraphCount);
        summary.setItemCount(result.getItems().size());
        summary.setCandidateCount(countCandidates(result.getItems()));
        summary.setHeadingCount(countRole(result.getItems(), PolarionItemRole.HEADING));
        summary.setRequirementCount(countRole(result.getItems(), PolarionItemRole.REQUIREMENT));
        summary.setIgnoredCount(countRole(result.getItems(), PolarionItemRole.IGNORED));
        summary.setCreatedCount(countCreated(result.getItems()));
        summary.setHeadingCreatedCount(countCreatedRole(result.getItems(), PolarionItemRole.HEADING));
        summary.setRequirementCreatedCount(countCreatedRole(result.getItems(), PolarionItemRole.REQUIREMENT));
        summary.setReplacedCount(countStatus(result.getItems(), ItemStatus.REPLACED));
        summary.setSkippedCount(countStatus(result.getItems(), ItemStatus.SKIPPED));
        summary.setCreateBlockedCount(countStatus(result.getItems(), ItemStatus.CREATE_BLOCKED));
        summary.setFailedCount(countStatus(result.getItems(), ItemStatus.CREATE_FAILED)
                + countStatus(result.getItems(), ItemStatus.CREATE_BLOCKED)
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
        response.setHeadingCount(jobResult.getSummary().getHeadingCount());
        response.setRequirementCount(jobResult.getSummary().getRequirementCount());
        response.setIgnoredCount(jobResult.getSummary().getIgnoredCount());
        response.setCreatedCount(jobResult.getSummary().getCreatedCount());
        response.setHeadingCreatedCount(jobResult.getSummary().getHeadingCreatedCount());
        response.setRequirementCreatedCount(jobResult.getSummary().getRequirementCreatedCount());
        response.setReplacedCount(jobResult.getSummary().getReplacedCount());
        response.setSkippedCount(jobResult.getSummary().getSkippedCount());
        response.setCreateBlockedCount(jobResult.getSummary().getCreateBlockedCount());
        response.setFailedCount(jobResult.getSummary().getFailedCount());
        response.setOutputDir(jobDir.toString().replace('\\', '/'));
        response.setOriginalXmlFile(ORIGINAL_XML_FILE);
        response.setProcessedXmlFile(jobResult.getFiles().getProcessedXml());
        response.setResultJsonFile(RESULT_JSON_FILE);
        response.setPreviewCsvFile(PREVIEW_CSV_FILE);
        response.setAiDebugFile(jobResult.getFiles().getAiDebug());
        response.setProgressLogFile(jobResult.getFiles().getProgressLog());
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

    private PolarionModuleImportResponse notifyAndReturn(PolarionModuleImportResponse response) {
        callbackNotifier.notifyFinished(response);
        return response;
    }

    private boolean hasErrors(List<PolarionImportItemResult> items) {
        for (PolarionImportItemResult item : items) {
            if (ItemStatus.CREATE_FAILED.name().equals(item.getStatus())
                    || ItemStatus.CREATE_BLOCKED.name().equals(item.getStatus())
                    || ItemStatus.REPLACE_FAILED.name().equals(item.getStatus())
                    || ItemStatus.FAILED.name().equals(item.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private boolean isHeading(PolarionImportItemResult item) {
        return item != null && PolarionItemRole.HEADING.name().equals(item.getItemRole());
    }

    private boolean isRequirement(PolarionImportItemResult item) {
        return item != null && PolarionItemRole.REQUIREMENT.name().equals(item.getItemRole());
    }

    private boolean resolveParentWkIdForCreate(PolarionImportJobResult jobResult, PolarionImportItemResult item) {
        if (item == null || !TextUtils.hasText(item.getParentOutlineNo())) {
            return true;
        }
        PolarionImportItemResult parent = findNearestParentHeadingByOutline(
                jobResult == null ? null : jobResult.getItems(),
                item,
                item.getParentOutlineNo());
        if (parent == null || !TextUtils.hasText(parent.getWorkItemId())) {
            return false;
        }
        item.setParentWkId(parent.getWorkItemId());
        return true;
    }

    private PolarionImportItemResult findNearestParentHeadingByOutline(List<PolarionImportItemResult> items,
                                                                       PolarionImportItemResult child,
                                                                       String outlineNo) {
        if (items == null || child == null || !TextUtils.hasText(outlineNo)) {
            return null;
        }
        String normalized = normalizeOutline(outlineNo);
        int childIndex = findItemIndex(items, child);
        if (childIndex < 0) {
            return null;
        }
        for (int i = childIndex - 1; i >= 0; i--) {
            PolarionImportItemResult candidate = items.get(i);
            if (isHeading(candidate)
                    && normalized.equals(normalizeOutline(candidate.getOutlineNo()))) {
                return candidate;
            }
        }
        return null;
    }

    private int findItemIndex(List<PolarionImportItemResult> items, PolarionImportItemResult target) {
        if (items == null || target == null) {
            return -1;
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == target) {
                return i;
            }
        }
        return -1;
    }

    private void markCreateBlocked(PolarionImportItemResult item, String errorCode, String errorMessage) {
        item.setStatus(ItemStatus.CREATE_BLOCKED.name());
        item.setErrorMessage(errorCode + ": " + errorMessage);
    }

    private String topLevelParentWkId(ResolvedRequest resolved, PolarionImportItemResult item) {
        if (resolved == null || item == null || isHeading(item) || TextUtils.hasText(item.getParentOutlineNo())) {
            return null;
        }
        return resolved.parentWkId;
    }

    private String normalizeOutline(String outlineNo) {
        String normalized = outlineNo == null ? "" : outlineNo.trim();
        while (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String effectiveTitle(ResolvedRequest resolved, PolarionImportItemResult item) {
        if (isHeading(item)) {
            return firstText(
                    item == null ? null : item.getRuleTitle(),
                    item == null ? null : item.getTitle());
        }
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

    private int countRole(List<PolarionImportItemResult> items, PolarionItemRole role) {
        int count = 0;
        for (PolarionImportItemResult item : items) {
            if (item != null && role.name().equals(item.getItemRole())) {
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

    private int countCreatedRole(List<PolarionImportItemResult> items, PolarionItemRole role) {
        int count = 0;
        for (PolarionImportItemResult item : items) {
            if (item != null && role.name().equals(item.getItemRole()) && TextUtils.hasText(item.getWorkItemId())) {
                count++;
            }
        }
        return count;
    }

    private int countFailedRole(List<PolarionImportItemResult> items, PolarionItemRole role) {
        int count = 0;
        for (PolarionImportItemResult item : items) {
            if (item != null && role.name().equals(item.getItemRole())
                    && (ItemStatus.CREATE_FAILED.name().equals(item.getStatus())
                    || ItemStatus.CREATE_BLOCKED.name().equals(item.getStatus())
                    || ItemStatus.REPLACE_FAILED.name().equals(item.getStatus())
                    || ItemStatus.FAILED.name().equals(item.getStatus()))) {
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
        resolved.jobId = TextUtils.hasText(safeRequest.getJobId())
                ? sanitizeJobId(safeRequest.getJobId())
                : null;
        resolved.baseUrl = firstText(location == null ? safeRequest.getBaseUrl() : location.getBaseUrl(), polarionProperties.getBaseUrl());
        PolarionProperties.WorkItemApi api = polarionProperties.getWorkItemApi();
        resolved.projectId = firstText(
                location == null ? safeRequest.getProjectId() : location.getProjectId(),
                api == null ? null : api.getDefaultPolarionId(),
                polarionProperties.getDefaultProjectId());
        resolved.moduleFolder = firstText(location == null ? safeRequest.getModuleFolder() : location.getModuleFolder(), polarionProperties.getDefaultModuleFolder());
        resolved.moduleName = firstText(location == null ? safeRequest.getModuleName() : location.getModuleName(), moduleProperties.getDefaultModuleName());
        resolved.moduleURI = firstText(
                safeRequest.getModuleURI(),
                location == null ? null : location.getModuleURI(),
                moduleUrlParser.buildModuleURI(resolved.projectId, resolved.moduleFolder, resolved.moduleName));
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

    private void appendProgress(Path progressLogFile, String message) {
        try {
            progressLogWriter.append(progressLogFile, message);
        } catch (IOException e) {
            LOGGER.warn("Progress log write failed: file={}, message={}", progressLogFile, e.getMessage());
        }
    }

    private String formatItemProgress(int index, int total, PolarionImportItemResult item) {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append(index).append("/").append(total).append("]");
        if (TextUtils.hasText(item.getItemRole())) {
            builder.append(" ").append(item.getItemRole());
        }
        if (TextUtils.hasText(item.getOutlineNo())) {
            builder.append(" 条款 ").append(item.getOutlineNo());
        }
        if (TextUtils.hasText(item.getParentOutlineNo())) {
            builder.append("，parentOutlineNo=").append(item.getParentOutlineNo());
        }
        if (TextUtils.hasText(item.getParentWkId())) {
            builder.append("，parentWkId=").append(item.getParentWkId());
        }
        if (TextUtils.hasText(item.getItemKey())) {
            builder.append("，itemKey=").append(item.getItemKey());
        }
        String title = firstText(item.getTitle(), item.getRuleTitle(), item.getDescription());
        if (TextUtils.hasText(title)) {
            builder.append("，标题=").append(TextUtils.truncateAtWordBoundary(title, 50));
        }
        return builder.toString();
    }

    private String formatAiProgressResult(int index, int total, PolarionImportItemResult item) {
        String base = formatItemProgress(index, total, item);
        if (AI_STATUS_SUCCESS.equals(item.getAiStatus())) {
            return "AI 生成完成：" + base + "。";
        }
        return "AI 生成失败：" + base + "，原因=" + item.getAiErrorMessage() + "。";
    }

    private String buildJobId(String moduleName) {
        return TextUtils.sanitizePathPart(moduleName) + "_" + JOB_TIME_FORMAT.format(LocalDateTime.now());
    }

    private String sanitizeJobId(String jobId) {
        String sanitized = TextUtils.sanitizePathPart(jobId);
        if (".".equals(sanitized) || "..".equals(sanitized)) {
            return "job_" + JOB_TIME_FORMAT.format(LocalDateTime.now());
        }
        return sanitized;
    }

    /**
     * 解析后的请求参数，避免在主流程中重复处理默认值。
     */
    private static class ResolvedRequest {
        private String jobId;
        private String baseUrl;
        private String projectId;
        private String moduleFolder;
        private String moduleName;
        private String moduleURI;
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
