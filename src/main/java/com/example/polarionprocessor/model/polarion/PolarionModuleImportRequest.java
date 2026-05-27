package com.example.polarionprocessor.model.polarion;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 正式 Polarion module.xml 导入接口的请求参数。
 */
public class PolarionModuleImportRequest {

    /** Polarion 项目 id，例如 FDP_Demo。 */
    private String projectId;

    /** 模块所在目录，例如 10 Stakeholder Requirement。 */
    private String moduleFolder;

    /** 模块名，例如 R171e2。 */
    private String moduleName;

    /** 要创建的 Work Item 类型。 */
    private String workItemType;

    /** true 时只下载、解析、识别和输出预览，不创建 Work Item，也不改写 XML。 */
    private Boolean dryRun;

    /** 是否要求条款文本命中 shall、may、should 等关键词。 */
    private Boolean requireKeyword;

    /** 传给 Work Item 创建 API 的默认扩展字段。 */
    private Map<String, Object> defaultFields = new LinkedHashMap<String, Object>();

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

    public Boolean getRequireKeyword() {
        return requireKeyword;
    }

    public void setRequireKeyword(Boolean requireKeyword) {
        this.requireKeyword = requireKeyword;
    }

    public Map<String, Object> getDefaultFields() {
        return defaultFields;
    }

    public void setDefaultFields(Map<String, Object> defaultFields) {
        this.defaultFields = defaultFields;
    }
}
