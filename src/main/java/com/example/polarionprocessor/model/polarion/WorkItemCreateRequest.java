package com.example.polarionprocessor.model.polarion;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 调用 Polarion API 创建 Work Item 时使用的请求对象。
 */
public class WorkItemCreateRequest {

    private String status;
    private String wkId;
    private String projectId;
    private String type;
    private String title;
    private String description;
    private String authorName;
    private String authorId;
    private List<String> assigneeIds;
    private String dueDate;
    private String startDate;
    private String parentWkId;
    private String moduleURI;
    private Boolean isNewPdp;
    private Boolean onlyCreate;
    private String commentContent;
    private Boolean removedLink;
    private String initialEstimate;
    private String timeSpent;
    private Map<String, Object> fields = new LinkedHashMap<String, Object>();
    private List<PolarionCustomFieldRequest> customFields = new ArrayList<PolarionCustomFieldRequest>();

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWkId() {
        return wkId;
    }

    public void setWkId(String wkId) {
        this.wkId = wkId;
    }

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

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public List<String> getAssigneeIds() {
        return assigneeIds;
    }

    public void setAssigneeIds(List<String> assigneeIds) {
        this.assigneeIds = assigneeIds;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getParentWkId() {
        return parentWkId;
    }

    public void setParentWkId(String parentWkId) {
        this.parentWkId = parentWkId;
    }

    public String getModuleURI() {
        return moduleURI;
    }

    public void setModuleURI(String moduleURI) {
        this.moduleURI = moduleURI;
    }

    public Boolean getIsNewPdp() {
        return isNewPdp;
    }

    public void setIsNewPdp(Boolean isNewPdp) {
        this.isNewPdp = isNewPdp;
    }

    public Boolean getOnlyCreate() {
        return onlyCreate;
    }

    public void setOnlyCreate(Boolean onlyCreate) {
        this.onlyCreate = onlyCreate;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public Boolean getRemovedLink() {
        return removedLink;
    }

    public void setRemovedLink(Boolean removedLink) {
        this.removedLink = removedLink;
    }

    public String getInitialEstimate() {
        return initialEstimate;
    }

    public void setInitialEstimate(String initialEstimate) {
        this.initialEstimate = initialEstimate;
    }

    public String getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(String timeSpent) {
        this.timeSpent = timeSpent;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields == null ? new LinkedHashMap<String, Object>() : fields;
    }

    public List<PolarionCustomFieldRequest> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(List<PolarionCustomFieldRequest> customFields) {
        this.customFields = customFields == null ? new ArrayList<PolarionCustomFieldRequest>() : customFields;
    }
}
