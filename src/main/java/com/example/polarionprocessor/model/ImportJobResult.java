package com.example.polarionprocessor.model;

import java.util.ArrayList;
import java.util.List;

public class ImportJobResult {

    private String jobId;
    private String moduleName;
    private String mode;
    private Boolean dryRun;
    private String createdAt;
    private String updatedAt;
    private String sourceXmlHash;
    private Integer totalParagraphCount;
    private Integer totalItemCount;
    private Integer candidateCount;
    private Integer replacedCount;
    private Integer skippedCount;
    private Integer failedCount;
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
