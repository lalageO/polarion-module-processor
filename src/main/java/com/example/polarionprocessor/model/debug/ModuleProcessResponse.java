package com.example.polarionprocessor.model.debug;

/**
 * 单次 module.xml 上传处理完成后的 API 响应。
 */
public class ModuleProcessResponse {

    /** 整个请求是否没有发生顶层失败。 */
    private Boolean success;

    /** 输出任务 id。 */
    private String jobId;

    /** 解析后的模块名。 */
    private String moduleName;

    /** 本次运行是否为 dry-run。 */
    private Boolean dryRun;

    /** 实际生效的替换模式。 */
    private String replaceMode;

    /** 分组前扫描到的 <p> 总数。 */
    private Integer totalParagraphCount;

    /** 基于数字边界分组后的 item 总数。 */
    private Integer totalItemCount;

    /** 候选 item 数量。 */
    private Integer candidateCount;

    /** 成功替换的 item 数量。 */
    private Integer replacedCount;

    /** 跳过的 item 数量。 */
    private Integer skippedCount;

    /** 失败的 item 数量。 */
    private Integer failedCount;

    /** 当前任务的相对输出目录。 */
    private String outputDir;

    /** 原始 XML 备份文件名。 */
    private String originalXmlFile;

    /** 处理后 XML 文件名。 */
    private String processedXmlFile;

    /** JSON 账本文件名。 */
    private String resultJsonFile;

    /** CSV 预览文件名。 */
    private String previewCsvFile;

    /** 人工可读的请求结果信息。 */
    private String message;

    /**
     * 在处理流程尚未开始时创建最小失败响应。
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
