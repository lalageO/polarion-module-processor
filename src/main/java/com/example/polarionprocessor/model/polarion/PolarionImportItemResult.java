package com.example.polarionprocessor.model.polarion;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 正式导入流程输出到 JSON/CSV 的单个 item 结果。
 */
public class PolarionImportItemResult {

    private Integer seq;
    private String itemKey;
    private String startParagraphId;
    private String endParagraphId;
    private String itemRole;
    private String workItemType;
    private String outlineNo;
    private Integer outlineDepth;
    private String titleText;
    private String ruleTitle;
    private String aiTitle;
    private String title;
    private String description;
    private Boolean candidate;
    private String skipReason;
    private Boolean hasChildOutline;
    private String decisionReason;
    private String parentOutlineNo;
    private String parentWkId;
    private String aiStatus;
    private String aiPromptType;
    private String aiDebugRef;
    private String aiErrorMessage;
    private Map<String, Object> aiFields = new LinkedHashMap<String, Object>();
    private Map<String, Object> workItemCreateFields = new LinkedHashMap<String, Object>();
    private List<PolarionCustomFieldRequest> customFields = new ArrayList<PolarionCustomFieldRequest>();
    private String workItemId;
    private String status;
    private String errorMessage;

    /** 内部替换用 HTML，正式 JSON 不输出。 */
    @JsonIgnore
    private String replacementHtml;

    /** 内部替换用起始偏移，正式 JSON 不输出。 */
    @JsonIgnore
    private Integer sourceStartIndex;

    /** 内部替换用结束偏移，正式 JSON 不输出。 */
    @JsonIgnore
    private Integer sourceEndIndex;

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
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

    public String getItemRole() {
        return itemRole;
    }

    public void setItemRole(String itemRole) {
        this.itemRole = itemRole;
    }

    public String getWorkItemType() {
        return workItemType;
    }

    public void setWorkItemType(String workItemType) {
        this.workItemType = workItemType;
    }

    public String getOutlineNo() {
        return outlineNo;
    }

    public void setOutlineNo(String outlineNo) {
        this.outlineNo = outlineNo;
    }

    public Integer getOutlineDepth() {
        return outlineDepth;
    }

    public void setOutlineDepth(Integer outlineDepth) {
        this.outlineDepth = outlineDepth;
    }

    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRuleTitle() {
        return ruleTitle;
    }

    public void setRuleTitle(String ruleTitle) {
        this.ruleTitle = ruleTitle;
    }

    public String getAiTitle() {
        return aiTitle;
    }

    public void setAiTitle(String aiTitle) {
        this.aiTitle = aiTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Boolean getHasChildOutline() {
        return hasChildOutline;
    }

    public void setHasChildOutline(Boolean hasChildOutline) {
        this.hasChildOutline = hasChildOutline;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public String getParentOutlineNo() {
        return parentOutlineNo;
    }

    public void setParentOutlineNo(String parentOutlineNo) {
        this.parentOutlineNo = parentOutlineNo;
    }

    public String getParentWkId() {
        return parentWkId;
    }

    public void setParentWkId(String parentWkId) {
        this.parentWkId = parentWkId;
    }

    public String getAiStatus() {
        return aiStatus;
    }

    public void setAiStatus(String aiStatus) {
        this.aiStatus = aiStatus;
    }

    public String getAiPromptType() {
        return aiPromptType;
    }

    public void setAiPromptType(String aiPromptType) {
        this.aiPromptType = aiPromptType;
    }

    public String getAiDebugRef() {
        return aiDebugRef;
    }

    public void setAiDebugRef(String aiDebugRef) {
        this.aiDebugRef = aiDebugRef;
    }

    public String getAiErrorMessage() {
        return aiErrorMessage;
    }

    public void setAiErrorMessage(String aiErrorMessage) {
        this.aiErrorMessage = aiErrorMessage;
    }

    public Map<String, Object> getAiFields() {
        return aiFields;
    }

    public void setAiFields(Map<String, Object> aiFields) {
        this.aiFields = aiFields == null ? new LinkedHashMap<String, Object>() : aiFields;
    }

    public Map<String, Object> getWorkItemCreateFields() {
        return workItemCreateFields;
    }

    public void setWorkItemCreateFields(Map<String, Object> workItemCreateFields) {
        this.workItemCreateFields = workItemCreateFields == null
                ? new LinkedHashMap<String, Object>()
                : workItemCreateFields;
    }

    public List<PolarionCustomFieldRequest> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(List<PolarionCustomFieldRequest> customFields) {
        this.customFields = customFields == null
                ? new ArrayList<PolarionCustomFieldRequest>()
                : customFields;
    }

    public String getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(String workItemId) {
        this.workItemId = workItemId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getReplacementHtml() {
        return replacementHtml;
    }

    public void setReplacementHtml(String replacementHtml) {
        this.replacementHtml = replacementHtml;
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
}
