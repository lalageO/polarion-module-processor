package com.example.polarionprocessor.model;

import java.util.ArrayList;
import java.util.List;

public class ImportItemResult {

    private Integer seq;
    private String paragraphId;
    private String startParagraphId;
    private String endParagraphId;
    private List<String> paragraphIds = new ArrayList<String>();
    private Integer paragraphCount;
    private String outlineNo;
    private String paragraphKey;
    private String sourceText;
    private String description;
    private String sourceTextHash;
    private String sourceOuterHtml;
    private String sourceOuterHtmlHash;
    private Integer sourceStartIndex;
    private Integer sourceEndIndex;
    private Boolean candidate;
    private String skipReason;
    private String generatedTitle;
    private String finalTitle;
    private String workItemId;
    private String replacementHtml;
    private String status;
    private String errorCode;
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
