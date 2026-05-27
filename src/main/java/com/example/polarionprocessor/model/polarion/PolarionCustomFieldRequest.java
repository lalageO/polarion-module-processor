package com.example.polarionprocessor.model.polarion;

/**
 * 公司内部 createItem 接口的 customFields 单项结构。
 */
public class PolarionCustomFieldRequest {

    private String id;
    private Boolean multi;
    private String type;
    private Object value;

    public PolarionCustomFieldRequest() {
    }

    public PolarionCustomFieldRequest(String id, Boolean multi, String type, Object value) {
        this.id = id;
        this.multi = multi;
        this.type = type;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getMulti() {
        return multi;
    }

    public void setMulti(Boolean multi) {
        this.multi = multi;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
