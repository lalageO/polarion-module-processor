package com.example.polarionprocessor.ai.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 单个候选 Work Item 的 AI 生成结果。
 */
public class AiGenerateResult {

    private Boolean success;
    private AiPromptType promptType;
    private String model;
    private String title;
    private Map<String, Object> fields = new LinkedHashMap<String, Object>();
    private Map<String, Object> parsedFields = new LinkedHashMap<String, Object>();
    private String prompt;
    private String rawResponse;
    private AiUsage usage;
    private String errorMessage;

    public static AiGenerateResult failure(AiPromptType promptType, String errorMessage) {
        AiGenerateResult result = new AiGenerateResult();
        result.setSuccess(Boolean.FALSE);
        result.setPromptType(promptType);
        result.setErrorMessage(errorMessage);
        return result;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public AiPromptType getPromptType() {
        return promptType;
    }

    public void setPromptType(AiPromptType promptType) {
        this.promptType = promptType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields == null ? new LinkedHashMap<String, Object>() : fields;
    }

    public Map<String, Object> getParsedFields() {
        return parsedFields;
    }

    public void setParsedFields(Map<String, Object> parsedFields) {
        this.parsedFields = parsedFields == null ? new LinkedHashMap<String, Object>() : parsedFields;
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

    public AiUsage getUsage() {
        return usage;
    }

    public void setUsage(AiUsage usage) {
        this.usage = usage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
