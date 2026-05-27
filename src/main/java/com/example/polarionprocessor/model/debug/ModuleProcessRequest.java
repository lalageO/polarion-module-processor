package com.example.polarionprocessor.model.debug;

import org.springframework.web.multipart.MultipartFile;

/**
 * 从 multipart 上传请求中收集到的 API 处理参数。
 */
public class ModuleProcessRequest {

    /** 上传的 module.xml 文件。 */
    private MultipartFile file;

    /** 可选逻辑模块名；未传时依次回退到 XML title 和应用默认值。 */
    private String moduleName;

    /** 为 true 时不执行实际替换语义。 */
    private Boolean dryRun;

    /** 请求的替换模式，例如 NONE 或 MOCK。 */
    private String replaceMode;

    /** mock Work Item id 生成时使用的前缀，例如 FDP。 */
    private String mockIdPrefix;

    /** 预留开关，用于后续子 item 处理。 */
    private Boolean includeSubItems;

    /** 候选文本是否必须包含 shall、must 等需求关键词。 */
    private Boolean requireKeyword;

    /** 候选筛选允许的最小条款层级。 */
    private Integer minOutlineDepth;

    /** 请求的标题生成模式。 */
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
