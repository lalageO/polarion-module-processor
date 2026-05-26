package com.example.polarionprocessor.model;

public class ModuleProcessResponse {

    private Boolean success;
    private String jobId;
    private String moduleName;
    private Boolean dryRun;
    private String replaceMode;
    private Integer totalParagraphCount;
    private Integer candidateCount;
    private Integer replacedCount;
    private Integer skippedCount;
    private Integer failedCount;
    private String outputDir;
    private String originalXmlFile;
    private String processedXmlFile;
    private String resultJsonFile;
    private String previewCsvFile;
    private String message;

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
