package com.example.polarionprocessor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Full result ledger written to output/{jobId}/import_result.json.
 */
public class ImportJobResult {

    /** Unique output job id, also used as the output directory name. */
    private String jobId;

    /** Logical module name supplied by request or extracted from module.xml title. */
    private String moduleName;

    /** Replace mode used for this run. */
    private String mode;

    /** Whether this run avoided logical replacement. */
    private Boolean dryRun;

    /** Local creation time formatted for human-readable output. */
    private String createdAt;

    /** Local update time formatted for human-readable output. */
    private String updatedAt;

    /** SHA-256 of the uploaded module.xml. */
    private String sourceXmlHash;

    /** Total scanned <p> count before grouping. */
    private Integer totalParagraphCount;

    /** Total grouped item count after numeric-boundary grouping. */
    private Integer totalItemCount;

    /** Number of grouped items selected as candidates. */
    private Integer candidateCount;

    /** Number of grouped items successfully replaced in processed_module.xml. */
    private Integer replacedCount;

    /** Number of grouped items skipped by candidate selection. */
    private Integer skippedCount;

    /** Number of grouped items that failed during processing or replacement. */
    private Integer failedCount;

    /** Per-item details for preview, debugging, and possible later import. */
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
