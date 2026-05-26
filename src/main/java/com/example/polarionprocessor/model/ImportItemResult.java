package com.example.polarionprocessor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * One importable work item candidate after paragraph grouping and candidate selection.
 */
public class ImportItemResult {

    /** Position in the generated item list. */
    private Integer seq;

    /** Anchor paragraph id; this is the first paragraph in the grouped block. */
    private String paragraphId;

    /** First paragraph id included in this item block. */
    private String startParagraphId;

    /** Last paragraph id included in this item block. */
    private String endParagraphId;

    /** All paragraph ids included in this item block, used for preview and troubleshooting. */
    private List<String> paragraphIds = new ArrayList<String>();

    /** Number of original <p> nodes included in this item block. */
    private Integer paragraphCount;

    /** Numeric clause number of the anchor paragraph, for example 2.2. */
    private String outlineNo;

    /** Stable local key composed from module name, anchor paragraph id, and text hash. */
    private String paragraphKey;

    /** Full grouped description text used by title generation and later Work Item creation. */
    private String sourceText;

    /** Alias of sourceText kept for the later Polarion Work Item description field. */
    private String description;

    /** SHA-256 of sourceText. */
    private String sourceTextHash;

    /** Exact original HTML range covering all paragraphs in this item block. */
    private String sourceOuterHtml;

    /** SHA-256 of sourceOuterHtml. */
    private String sourceOuterHtmlHash;

    /** Start offset of sourceOuterHtml inside the original CDATA HTML string. */
    private Integer sourceStartIndex;

    /** End offset of sourceOuterHtml inside the original CDATA HTML string. */
    private Integer sourceEndIndex;

    /** Whether this grouped item should be replaced or imported. */
    private Boolean candidate;

    /** Machine-readable reason when candidate is false. */
    private String skipReason;

    /** Title suggested by the configured title generator. */
    private String generatedTitle;

    /** Title to use when creating the Work Item; currently equal to generatedTitle. */
    private String finalTitle;

    /** Mock or real Polarion Work Item id, for example FDP-000001. */
    private String workItemId;

    /** HTML fragment that replaces sourceOuterHtml in mock mode. */
    private String replacementHtml;

    /** Current item processing status. */
    private String status;

    /** Machine-readable error code for this item. */
    private String errorCode;

    /** Human-readable error message for this item. */
    private String errorMessage;

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getParagraphId() {
        return paragraphId;
    }

    public void setParagraphId(String paragraphId) {
        this.paragraphId = paragraphId;
    }

    public String getStartParagraphId() {
        return startParagraphId;
    }

    public void setStartParagraphId(String startParagraphId) {
        this.startParagraphId = startParagraphId;
    }

    public String getEndParagraphId() {
        return endParagraphId;
    }

    public void setEndParagraphId(String endParagraphId) {
        this.endParagraphId = endParagraphId;
    }

    public List<String> getParagraphIds() {
        return paragraphIds;
    }

    public void setParagraphIds(List<String> paragraphIds) {
        this.paragraphIds = paragraphIds;
    }

    public Integer getParagraphCount() {
        return paragraphCount;
    }

    public void setParagraphCount(Integer paragraphCount) {
        this.paragraphCount = paragraphCount;
    }

    public String getOutlineNo() {
        return outlineNo;
    }

    public void setOutlineNo(String outlineNo) {
        this.outlineNo = outlineNo;
    }

    public String getParagraphKey() {
        return paragraphKey;
    }

    public void setParagraphKey(String paragraphKey) {
        this.paragraphKey = paragraphKey;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceTextHash() {
        return sourceTextHash;
    }

    public void setSourceTextHash(String sourceTextHash) {
        this.sourceTextHash = sourceTextHash;
    }

    public String getSourceOuterHtml() {
        return sourceOuterHtml;
    }

    public void setSourceOuterHtml(String sourceOuterHtml) {
        this.sourceOuterHtml = sourceOuterHtml;
    }

    public String getSourceOuterHtmlHash() {
        return sourceOuterHtmlHash;
    }

    public void setSourceOuterHtmlHash(String sourceOuterHtmlHash) {
        this.sourceOuterHtmlHash = sourceOuterHtmlHash;
    }

    public Integer getSourceStartIndex() {
        return sourceStartIndex;
    }

    public void setSourceStartIndex(Integer sourceStartIndex) {
        this.sourceStartIndex = sourceStartIndex;
    }

    public Integer getSourceEndIndex() {
        return sourceEndIndex;
    }

    public void setSourceEndIndex(Integer sourceEndIndex) {
        this.sourceEndIndex = sourceEndIndex;
    }

    public Boolean getCandidate() {
        return candidate;
    }

    public void setCandidate(Boolean candidate) {
        this.candidate = candidate;
    }

    public String getSkipReason() {
        return skipReason;
    }

    public void setSkipReason(String skipReason) {
        this.skipReason = skipReason;
    }

    public String getGeneratedTitle() {
        return generatedTitle;
    }

    public void setGeneratedTitle(String generatedTitle) {
        this.generatedTitle = generatedTitle;
    }

    public String getFinalTitle() {
        return finalTitle;
    }

    public void setFinalTitle(String finalTitle) {
        this.finalTitle = finalTitle;
    }

    public String getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(String workItemId) {
        this.workItemId = workItemId;
    }

    public String getReplacementHtml() {
        return replacementHtml;
    }

    public void setReplacementHtml(String replacementHtml) {
        this.replacementHtml = replacementHtml;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
