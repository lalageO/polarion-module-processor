package com.example.polarionprocessor.ai.model;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ai_debug.jsonl 单行记录。
 */
public class AiDebugRecord {

    private String ref;
    private String jobId;
    private String projectId;
    private String moduleName;
    private Integer itemSeq;
    private String itemKey;
    private String outlineNo;
    private String promptType;
    private String model;
    private Boolean success;
    private String errorMessage;
    private String prompt;
    private String rawResponse;
    private Map<String, Object> parsedFields = new LinkedHashMap<String, Object>();
    private Map<String, Object> acceptedFields = new LinkedHashMap<String, Object>();
    private AiUsage usage;
    private String createdAt = LocalDateTime.now().toString();

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
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

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Integer getItemSeq() {
        return itemSeq;
    }

    public void setItemSeq(Integer itemSeq) {
        this.itemSeq = itemSeq;
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public String getOutlineNo() {
        return outlineNo;
    }

    public void setOutlineNo(String outlineNo) {
        this.outlineNo = outlineNo;
    }

    public String getPromptType() {
        return promptType;
    }

    public void setPromptType(String promptType) {
        this.promptType = promptType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public Map<String, Object> getParsedFields() {
        return parsedFields;
    }

    public void setParsedFields(Map<String, Object> parsedFields) {
        this.parsedFields = parsedFields == null ? new LinkedHashMap<String, Object>() : parsedFields;
    }

    public Map<String, Object> getAcceptedFields() {
        return acceptedFields;
    }

    public void setAcceptedFields(Map<String, Object> acceptedFields) {
        this.acceptedFields = acceptedFields == null ? new LinkedHashMap<String, Object>() : acceptedFields;
    }

    public AiUsage getUsage() {
        return usage;
    }

    public void setUsage(AiUsage usage) {
        this.usage = usage;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
