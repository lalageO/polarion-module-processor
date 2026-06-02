package com.example.polarionprocessor.ai.model;

import com.example.polarionprocessor.model.polarion.PolarionEnumOptionRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 单个候选 Work Item 的 AI 生成输入。
 */
public class AiGenerateRequest {

    private String jobId;
    private String projectId;
    private String moduleName;
    private Integer itemSeq;
    private String itemKey;
    private String outlineNo;
    private String ruleTitle;
    private String description;
    private Map<String, List<PolarionEnumOptionRequest>> enumOptions =
            new LinkedHashMap<String, List<PolarionEnumOptionRequest>>();

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

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Integer getItemSeq() {
        return itemSeq;
    }

    public void setItemSeq(Integer itemSeq) {
        this.itemSeq = itemSeq;
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public String getOutlineNo() {
        return outlineNo;
    }

    public void setOutlineNo(String outlineNo) {
        this.outlineNo = outlineNo;
    }

    public String getRuleTitle() {
        return ruleTitle;
    }

    public void setRuleTitle(String ruleTitle) {
        this.ruleTitle = ruleTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, List<PolarionEnumOptionRequest>> getEnumOptions() {
        return enumOptions;
    }

    public void setEnumOptions(Map<String, List<PolarionEnumOptionRequest>> enumOptions) {
        this.enumOptions = enumOptions == null
                ? new LinkedHashMap<String, List<PolarionEnumOptionRequest>>()
                : enumOptions;
    }
}
