package com.example.polarionprocessor.enums;

/**
 * 非候选 item 的稳定跳过原因，供程序和人工排查使用。
 */
public enum SkipReason {
    /** 归一化后没有可见文本。 */
    EMPTY_PARAGRAPH,

    /** 缺少 Polarion 段落 id。 */
    NO_PARAGRAPH_ID,

    /** 段落 id 不符合预期的 polarion_N 格式。 */
    INVALID_PARAGRAPH_ID,

    /** 未识别到多级数字条款号。 */
    NO_OUTLINE_NO,

    /** 条款层级低于配置的最小层级。 */
    OUTLINE_DEPTH_TOO_LOW,

    /** 已开启关键词过滤，但没有命中需求类关键词。 */
    NO_REQUIREMENT_KEYWORD,

    /** 文本看起来只是标题，或层级 2 条款文本过短。 */
    TITLE_ONLY,

    /** 预留给目录识别。 */
    TOC,

    /** 预留给后续语义层面的需求相似度判断。 */
    NOT_REQUIREMENT_LIKE
}
