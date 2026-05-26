package com.example.polarionprocessor.model;

import org.springframework.web.multipart.MultipartFile;

public class ModuleProcessRequest {

    private MultipartFile file;
    private String moduleName;
    private Boolean dryRun;
    private String replaceMode;
    private String mockIdPrefix;
    private Boolean includeSubItems;
    private Boolean requireKeyword;
    private Integer minOutlineDepth;
    private String titleMode;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Boolean getDryRun() {
        return dryRun;
    }

    public void setDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
    }

    public String getReplaceMode() {
        return replaceMode;
    }

    public void setReplaceMode(String replaceMode) {
        this.replaceMode = replaceMode;
    }

    public String getMockIdPrefix() {
        return mockIdPrefix;
    }

    public void setMockIdPrefix(String mockIdPrefix) {
        this.mockIdPrefix = mockIdPrefix;
    }

    public Boolean getIncludeSubItems() {
        return includeSubItems;
    }

    public void setIncludeSubItems(Boolean includeSubItems) {
        this.includeSubItems = includeSubItems;
    }

    public Boolean getRequireKeyword() {
        return requireKeyword;
    }

    public void setRequireKeyword(Boolean requireKeyword) {
        this.requireKeyword = requireKeyword;
    }

    public Integer getMinOutlineDepth() {
        return minOutlineDepth;
    }

    public void setMinOutlineDepth(Integer minOutlineDepth) {
        this.minOutlineDepth = minOutlineDepth;
    }

    public String getTitleMode() {
        return titleMode;
    }

    public void setTitleMode(String titleMode) {
        this.titleMode = titleMode;
    }
}
