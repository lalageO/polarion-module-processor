package com.example.polarionprocessor.enums;

/**
 * 正式 Polarion 导入链路中单个条款的业务角色。
 */
public enum PolarionItemRole {
    /** 标题型 Work Item，对应 Polarion type=heading。 */
    HEADING,

    /** 需求型 Work Item，对应 Polarion type=stakeholderrequirement。 */
    REQUIREMENT,

    /** 不参与创建的内容。 */
    IGNORED
}
