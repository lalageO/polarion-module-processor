package com.example.polarionprocessor.model.polarion;

/**
 * 正式 Polarion module.xml 导入接口的响应对象。
 */
public class PolarionModuleImportResponse {

    /** 请求是否成功完成到可返回状态。 */
    private Boolean success;
    private String jobId;
    private String projectId;
    private String moduleFolder;
    private String moduleName;
    private Boolean dryRun;
    private String status;
    private Integer paragraphCount;
    private Integer itemCount;
    private Integer candidateCount;
    private Integer headingCount;
    private Integer requirementCount;
    private Integer ignoredCount;
    private Integer createdCount;
    private Integer headingCreatedCount;
    private Integer requirementCreatedCount;
    private Integer replacedCount;
    private Integer skippedCount;
    private Integer createBlockedCount;
    private Integer failedCount;
    private String outputDir;
    private String originalXmlFile;
    private String processedXmlFile;
    private String resultJsonFile;
    private String previewCsvFile;
    private String aiDebugFile;
    private String progressLogFile;
    private String svnCommitStatus;
    private String svnRevision;
    private String svnErrorMessage;
    private String message;

    public static PolarionModuleImportResponse failure(String message) {
        PolarionModuleImportResponse response = new PolarionModuleImportResponse();
        response.setSuccess(false);
        response.setStatus("FAILED");
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

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getModuleFolder() {
        return moduleFolder;
    }

    public void setModuleFolder(String moduleFolder) {
        this.moduleFolder = moduleFolder;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getParagraphCount() {
        return paragraphCount;
    }

    public void setParagraphCount(Integer paragraphCount) {
        this.paragraphCount = paragraphCount;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public Integer getCandidateCount() {
        return candidateCount;
    }

    public void setCandidateCount(Integer candidateCount) {
        this.candidateCount = candidateCount;
    }

    public Integer getHeadingCount() {
        return headingCount;
    }

    public void setHeadingCount(Integer headingCount) {
        this.headingCount = headingCount;
    }

    public Integer getRequirementCount() {
        return requirementCount;
    }

    public void setRequirementCount(Integer requirementCount) {
        this.requirementCount = requirementCount;
    }

    public Integer getIgnoredCount() {
        return ignoredCount;
    }

    public void setIgnoredCount(Integer ignoredCount) {
        this.ignoredCount = ignoredCount;
    }

    public Integer getCreatedCount() {
        return createdCount;
    }

    public void setCreatedCount(Integer createdCount) {
        this.createdCount = createdCount;
    }

    public Integer getHeadingCreatedCount() {
        return headingCreatedCount;
    }

    public void setHeadingCreatedCount(Integer headingCreatedCount) {
        this.headingCreatedCount = headingCreatedCount;
    }

    public Integer getRequirementCreatedCount() {
        return requirementCreatedCount;
    }

    public void setRequirementCreatedCount(Integer requirementCreatedCount) {
        this.requirementCreatedCount = requirementCreatedCount;
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

    public Integer getCreateBlockedCount() {
        return createBlockedCount;
    }

    public void setCreateBlockedCount(Integer createBlockedCount) {
        this.createBlockedCount = createBlockedCount;
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

    public String getAiDebugFile() {
        return aiDebugFile;
    }

    public void setAiDebugFile(String aiDebugFile) {
        this.aiDebugFile = aiDebugFile;
    }

    public String getProgressLogFile() {
        return progressLogFile;
    }

    public void setProgressLogFile(String progressLogFile) {
        this.progressLogFile = progressLogFile;
    }

    public String getSvnCommitStatus() {
        return svnCommitStatus;
    }

    public void setSvnCommitStatus(String svnCommitStatus) {
        this.svnCommitStatus = svnCommitStatus;
    }

    public String getSvnRevision() {
        return svnRevision;
    }

    public void setSvnRevision(String svnRevision) {
        this.svnRevision = svnRevision;
    }

    public String getSvnErrorMessage() {
        return svnErrorMessage;
    }

    public void setSvnErrorMessage(String svnErrorMessage) {
        this.svnErrorMessage = svnErrorMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
