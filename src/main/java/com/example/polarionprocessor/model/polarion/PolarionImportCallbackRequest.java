package com.example.polarionprocessor.model.polarion;

/**
 * 公司内部导入完成回调请求体。
 */
public class PolarionImportCallbackRequest {

    private String jobId;
    private String statusCode;
    private String result;

    public PolarionImportCallbackRequest() {
    }

    public PolarionImportCallbackRequest(String jobId, String statusCode, String result) {
        this.jobId = jobId;
        this.statusCode = statusCode;
        this.result = result;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
