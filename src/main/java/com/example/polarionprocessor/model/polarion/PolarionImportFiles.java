package com.example.polarionprocessor.model.polarion;

/**
 * 正式导入任务输出文件清单。
 */
public class PolarionImportFiles {

    private String originalXml;
    private String processedXml;
    private String csv;

    public String getOriginalXml() {
        return originalXml;
    }

    public void setOriginalXml(String originalXml) {
        this.originalXml = originalXml;
    }

    public String getProcessedXml() {
        return processedXml;
    }

    public void setProcessedXml(String processedXml) {
        this.processedXml = processedXml;
    }

    public String getCsv() {
        return csv;
    }

    public void setCsv(String csv) {
        this.csv = csv;
    }
}
