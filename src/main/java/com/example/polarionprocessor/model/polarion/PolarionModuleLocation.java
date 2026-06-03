package com.example.polarionprocessor.model.polarion;

/**
 * 从 Polarion wiki URL 中解析出的模块定位信息。
 */
public class PolarionModuleLocation {

    /** Polarion 站点基础地址，例如 http://alm.freetech.com。 */
    private String baseUrl;

    /** Polarion 项目 id，例如 FDP_Demo。 */
    private String projectId;

    /** 模块所在目录，例如 10 Stakeholder Requirement。 */
    private String moduleFolder;

    /** 模块名，例如 R171e。 */
    private String moduleName;

    /** Polarion 文档反向链接 URI，用于 Work Item 的 Open in Document。 */
    private String moduleURI;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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

    public String getModuleURI() {
        return moduleURI;
    }

    public void setModuleURI(String moduleURI) {
        this.moduleURI = moduleURI;
    }
}
