package com.example.polarionprocessor.model.polarion;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 公司内部 createItem 接口 customFields.enumOptions 单项结构。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolarionEnumOptionRequest {

    private String id;
    private String name;

    public PolarionEnumOptionRequest() {
    }

    public PolarionEnumOptionRequest(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
