package com.example.polarionprocessor.model;

/**
 * API response returned after processing one module.xml upload.
 */
public class ModuleProcessResponse {

    /** Whether the whole request completed without a top-level failure. */
    private Boolean success;

    /** Output job id. */
    private String jobId;

    /** Resolved module name. */
    private String moduleName;

    /** Whether the run was dry-run. */
    private Boolean dryRun;

    /** Effective replacement mode. */
    private String replaceMode;

    /** Total scanned <p> count before grouping. */
    private Integer totalParagraphCount;

    /** Total grouped item count after numeric-boundary grouping. */
    private Integer totalItemCount;

    /** Number of candidate items. */
    private Integer candidateCount;

    /** Number of successfully replaced items. */
    private Integer replacedCount;

    /** Number of skipped items. */
    private Integer skippedCount;

    /** Number of failed items. */
    private Integer failedCount;

    /** Relative output directory for this job. */
    private String outputDir;

    /** Original XML backup file name. */
    private String originalXmlFile;

    /** Processed XML file name. */
    private String processedXmlFile;

    /** JSON ledger file name. */
    private String resultJsonFile;

    /** CSV preview file name. */
    private String previewCsvFile;

    /** Human-readable request result message. */
    private String message;

    /**
     * Creates a minimal failure response when processing cannot even start.
     */
    public static ModuleProcessResponse failure(String message) {
        ModuleProcessResponse response = new ModuleProcessResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Boolean getDryRun() {
        return dryRun;
    }

    public void setDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
    }

    public String getReplaceMode() {
        return replaceMode;
    }

    public void setReplaceMode(String replaceMode) {
        this.replaceMode = replaceMode;
    }

    public Integer getTotalParagraphCount() {
        return totalParagraphCount;
    }

    public void setTotalParagraphCount(Integer totalParagraphCount) {
        this.totalParagraphCount = totalParagraphCount;
    }

    public Integer getTotalItemCount() {
        return totalItemCount;
    }

    public void setTotalItemCount(Integer totalItemCount) {
        this.totalItemCount = totalItemCount;
    }

    public Integer getCandidateCount() {
        return candidateCount;
    }

    public void setCandidateCount(Integer candidateCount) {
        this.candidateCount = candidateCount;
    }

    public Integer getReplacedCount() {
        return replacedCount;
    }

    public void setReplacedCount(Integer replacedCount) {
        this.replacedCount = replacedCount;
    }

    public Integer getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(Integer skippedCount) {
        this.skippedCount = skippedCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getOriginalXmlFile() {
        return originalXmlFile;
    }

    public void setOriginalXmlFile(String originalXmlFile) {
        this.originalXmlFile = originalXmlFile;
    }

    public String getProcessedXmlFile() {
        return processedXmlFile;
    }

    public void setProcessedXmlFile(String processedXmlFile) {
        this.processedXmlFile = processedXmlFile;
    }

    public String getResultJsonFile() {
        return resultJsonFile;
    }

    public void setResultJsonFile(String resultJsonFile) {
        this.resultJsonFile = resultJsonFile;
    }

    public String getPreviewCsvFile() {
        return previewCsvFile;
    }

    public void setPreviewCsvFile(String previewCsvFile) {
        this.previewCsvFile = previewCsvFile;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
