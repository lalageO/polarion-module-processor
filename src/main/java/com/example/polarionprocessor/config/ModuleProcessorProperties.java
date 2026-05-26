package com.example.polarionprocessor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "module-processor")
public class ModuleProcessorProperties {

    private String outputDir = "output";
    private String defaultModuleName = "unknown-module";
    private String defaultReplaceMode = "NONE";
    private String defaultMockIdPrefix = "MOCK";
    private Integer defaultMinOutlineDepth = 2;
    private Boolean defaultRequireKeyword = false;
    private Boolean defaultIncludeSubItems = false;
    private Integer levelTwoMinTextLength = 80;
    private Integer titleMaxLength = 80;
    private Boolean csvWithBom = true;

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getDefaultModuleName() {
        return defaultModuleName;
    }

    public void setDefaultModuleName(String defaultModuleName) {
        this.defaultModuleName = defaultModuleName;
    }

    public String getDefaultReplaceMode() {
        return defaultReplaceMode;
    }

    public void setDefaultReplaceMode(String defaultReplaceMode) {
        this.defaultReplaceMode = defaultReplaceMode;
    }

    public String getDefaultMockIdPrefix() {
        return defaultMockIdPrefix;
    }

    public void setDefaultMockIdPrefix(String defaultMockIdPrefix) {
        this.defaultMockIdPrefix = defaultMockIdPrefix;
    }

    public Integer getDefaultMinOutlineDepth() {
        return defaultMinOutlineDepth;
    }

    public void setDefaultMinOutlineDepth(Integer defaultMinOutlineDepth) {
        this.defaultMinOutlineDepth = defaultMinOutlineDepth;
    }

    public Boolean getDefaultRequireKeyword() {
        return defaultRequireKeyword;
    }

    public void setDefaultRequireKeyword(Boolean defaultRequireKeyword) {
        this.defaultRequireKeyword = defaultRequireKeyword;
    }

    public Boolean getDefaultIncludeSubItems() {
        return defaultIncludeSubItems;
    }

    public void setDefaultIncludeSubItems(Boolean defaultIncludeSubItems) {
        this.defaultIncludeSubItems = defaultIncludeSubItems;
    }

    public Integer getLevelTwoMinTextLength() {
        return levelTwoMinTextLength;
    }

    public void setLevelTwoMinTextLength(Integer levelTwoMinTextLength) {
        this.levelTwoMinTextLength = levelTwoMinTextLength;
    }

    public Integer getTitleMaxLength() {
        return titleMaxLength;
    }

    public void setTitleMaxLength(Integer titleMaxLength) {
        this.titleMaxLength = titleMaxLength;
    }

    public Boolean getCsvWithBom() {
        return csvWithBom;
    }

    public void setCsvWithBom(Boolean csvWithBom) {
        this.csvWithBom = csvWithBom;
    }
}
