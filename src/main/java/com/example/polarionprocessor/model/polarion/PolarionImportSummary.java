package com.example.polarionprocessor.model.polarion;

/**
 * 正式导入任务的数量汇总。
 */
public class PolarionImportSummary {

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
}
