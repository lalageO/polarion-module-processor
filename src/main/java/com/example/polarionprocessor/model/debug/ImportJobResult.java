package com.example.polarionprocessor.model.debug;

import com.example.polarionprocessor.model.shared.ImportItemResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 写入 output/{jobId}/import_result.json 的完整任务结果账本。
 */
public class ImportJobResult {

    /** 唯一任务 id，同时作为输出目录名。 */
    private String jobId;

    /** 逻辑模块名，来自请求参数或 module.xml 的 title 字段。 */
    private String moduleName;

    /** 本次运行使用的替换模式。 */
    private String mode;

    /** 本次运行是否跳过逻辑替换。 */
    private Boolean dryRun;

    /** 本地创建时间，使用人工可读格式。 */
    private String createdAt;

    /** 本地更新时间，使用人工可读格式。 */
    private String updatedAt;

    /** 上传 module.xml 的 SHA-256。 */
    private String sourceXmlHash;

    /** 分组前扫描到的 <p> 总数。 */
    private Integer totalParagraphCount;

    /** 基于数字边界分组后的 item 总数。 */
    private Integer totalItemCount;

    /** 被选为候选的分组 item 数量。 */
    private Integer candidateCount;

    /** 在 processed_module.xml 中成功替换的分组 item 数量。 */
    private Integer replacedCount;

    /** 被候选筛选跳过的分组 item 数量。 */
    private Integer skippedCount;

    /** 处理或替换过程中失败的分组 item 数量。 */
    private Integer failedCount;

    /** 单个 item 的明细，用于预览、排查和后续可能的导入。 */
    private List<ImportItemResult> items = new ArrayList<ImportItemResult>();

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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Boolean getDryRun() {
        return dryRun;
    }

    public void setDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSourceXmlHash() {
        return sourceXmlHash;
    }

    public void setSourceXmlHash(String sourceXmlHash) {
        this.sourceXmlHash = sourceXmlHash;
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

    public List<ImportItemResult> getItems() {
        return items;
    }

    public void setItems(List<ImportItemResult> items) {
        this.items = items;
    }
}
