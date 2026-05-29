package com.example.polarionprocessor.model.polarion;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 公司内部 Polarion createItem HTTP 接口请求体，对齐远程 WorkItemWsDto。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkItemCreateApiRequest {

    private String status;
    private String wkId;
    private String title;
    private String polarionId;
    private String authorName;
    private String authorId;
    private List<String> assigneeIds;
    private String type;
    private String dueDate;
    private String startDate;
    private String parentWkId;
    private Boolean isNewPdp = Boolean.FALSE;
    private Boolean onlyCreate = Boolean.TRUE;
    private String commentContent;
    private Boolean removedLink = Boolean.FALSE;
    @JsonProperty("cdescription")
    @JsonAlias({"cDescription"})
    private String cDescription;
    private String initialEstimate;
    private String timeSpent;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPolarionId() {
        return polarionId;
    }

    public void setPolarionId(String polarionId) {
        this.polarionId = polarionId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getCdescription() {
        return cDescription;
    }

    public void setCdescription(String cdescription) {
        this.cDescription = cdescription;
    }

    public void setCDescription(String cDescription) {
        this.cDescription = cDescription;
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

    public List<PolarionCustomFieldRequest> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(List<PolarionCustomFieldRequest> customFields) {
        this.customFields = customFields;
    }
}
