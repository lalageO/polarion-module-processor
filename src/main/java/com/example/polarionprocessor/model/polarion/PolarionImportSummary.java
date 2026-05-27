package com.example.polarionprocessor.model.polarion;

/**
 * 正式导入任务的数量汇总。
 */
public class PolarionImportSummary {

    private Integer paragraphCount;
    private Integer itemCount;
    private Integer candidateCount;
    private Integer createdCount;
    private Integer replacedCount;
    private Integer skippedCount;
    private Integer failedCount;

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

    public Integer getCreatedCount() {
        return createdCount;
    }

    public void setCreatedCount(Integer createdCount) {
        this.createdCount = createdCount;
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
}
