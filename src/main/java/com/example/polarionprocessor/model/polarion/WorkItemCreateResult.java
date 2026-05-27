package com.example.polarionprocessor.model.polarion;

/**
 * Polarion API 创建 Work Item 的返回结果。
 */
public class WorkItemCreateResult {

    private Boolean success;
    private String workItemId;
    private String errorCode;
    private String errorMessage;

    public static WorkItemCreateResult success(String workItemId) {
        WorkItemCreateResult result = new WorkItemCreateResult();
        result.setSuccess(true);
        result.setWorkItemId(workItemId);
        return result;
    }

    public static WorkItemCreateResult failure(String errorCode, String errorMessage) {
        WorkItemCreateResult result = new WorkItemCreateResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        return result;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(String workItemId) {
        this.workItemId = workItemId;
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
