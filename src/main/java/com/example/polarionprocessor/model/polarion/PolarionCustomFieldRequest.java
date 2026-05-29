package com.example.polarionprocessor.model.polarion;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 公司内部 createItem 接口的 customFields 单项结构。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolarionCustomFieldRequest {

    private String id;
    private String enumId;
    private Boolean multi;
    private String name;
    private Boolean required;
    private String type;
    private List<PolarionEnumOptionRequest> enumOptions;
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

    public String getEnumId() {
        return enumId;
    }

    public void setEnumId(String enumId) {
        this.enumId = enumId;
    }

    public Boolean getMulti() {
        return multi;
    }

    public void setMulti(Boolean multi) {
        this.multi = multi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<PolarionEnumOptionRequest> getEnumOptions() {
        return enumOptions;
    }

    public void setEnumOptions(List<PolarionEnumOptionRequest> enumOptions) {
        this.enumOptions = enumOptions;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
