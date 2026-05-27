package com.example.polarionprocessor.model.polarion;

import java.util.ArrayList;
import java.util.List;

/**
 * 正式流程写入 import_result.json 的收缩版结果模型。
 */
public class PolarionImportJobResult {

    private String jobId;
    private String projectId;
    private String moduleFolder;
    private String moduleName;
    private String workItemType;
    private Boolean dryRun;
    private String status;
    private PolarionImportFiles files = new PolarionImportFiles();
    private PolarionImportSummary summary = new PolarionImportSummary();
    private List<PolarionImportItemResult> items = new ArrayList<PolarionImportItemResult>();

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

    public String getModuleFolder() {
        return moduleFolder;
    }

    public void setModuleFolder(String moduleFolder) {
        this.moduleFolder = moduleFolder;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getWorkItemType() {
        return workItemType;
    }

    public void setWorkItemType(String workItemType) {
        this.workItemType = workItemType;
    }

    public Boolean getDryRun() {
        return dryRun;
    }

    public void setDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PolarionImportFiles getFiles() {
        return files;
    }

    public void setFiles(PolarionImportFiles files) {
        this.files = files;
    }

    public PolarionImportSummary getSummary() {
        return summary;
    }

    public void setSummary(PolarionImportSummary summary) {
        this.summary = summary;
    }

    public List<PolarionImportItemResult> getItems() {
        return items;
    }

    public void setItems(List<PolarionImportItemResult> items) {
        this.items = items;
    }
}
