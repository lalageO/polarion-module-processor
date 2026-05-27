package com.example.polarionprocessor.model.polarion;

import java.util.ArrayList;
import java.util.List;

/**
 * 公司内部 Polarion createItem HTTP 接口请求体。
 */
public class WorkItemCreateApiRequest {

    private String polarionId;
    private String type;
    private String title;
    private String authorId;
    private Boolean isNewPdp = Boolean.FALSE;
    private Boolean onlyCreate = Boolean.TRUE;
    private String cdescription;
    private List<PolarionCustomFieldRequest> customFields = new ArrayList<PolarionCustomFieldRequest>();

    public String getPolarionId() {
        return polarionId;
    }

    public void setPolarionId(String polarionId) {
        this.polarionId = polarionId;
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

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
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

    public String getCdescription() {
        return cdescription;
    }

    public void setCdescription(String cdescription) {
        this.cdescription = cdescription;
    }

    public List<PolarionCustomFieldRequest> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(List<PolarionCustomFieldRequest> customFields) {
        this.customFields = customFields;
    }
}
