package com.example.polarionprocessor.ai.model;

/**
 * Work Item AI 生成提示词类型。
 */
public enum AiPromptType {
    /** 普通项目，只生成标题。 */
    TITLE_ONLY,

    /** RMT 特例项目，生成标题和三个自定义字段。 */
    RMT_FIELDS
}
