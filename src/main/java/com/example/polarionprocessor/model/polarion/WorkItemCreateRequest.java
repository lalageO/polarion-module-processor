package com.example.polarionprocessor.model.polarion;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 调用 Polarion API 创建 Work Item 时使用的请求对象。
 */
public class WorkItemCreateRequest {

    private String projectId;
    private String type;
    private String title;
    private String description;
    private Map<String, Object> fields = new LinkedHashMap<String, Object>();

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }
}
