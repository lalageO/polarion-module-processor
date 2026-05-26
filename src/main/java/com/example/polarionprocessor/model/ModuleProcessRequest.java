package com.example.polarionprocessor.model;

import org.springframework.web.multipart.MultipartFile;

/**
 * API-level processing parameters collected from the multipart upload request.
 */
public class ModuleProcessRequest {

    /** Uploaded module.xml file. */
    private MultipartFile file;

    /** Optional logical module name; falls back to XML title and then application default. */
    private String moduleName;

    /** When true, do not perform replacement semantics. */
    private Boolean dryRun;

    /** Requested replacement mode, for example NONE or MOCK. */
    private String replaceMode;

    /** Prefix used by mock Work Item id generation, for example FDP. */
    private String mockIdPrefix;

    /** Reserved switch for future sub-item handling. */
    private Boolean includeSubItems;

    /** Whether candidates must contain requirement keywords such as shall or must. */
    private Boolean requireKeyword;

    /** Minimum outline depth accepted by candidate selection. */
    private Integer minOutlineDepth;

    /** Requested title generation mode. */
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
