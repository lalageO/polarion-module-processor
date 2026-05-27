package com.example.polarionprocessor.enums;

/**
 * 由 request.titleMode 指定的标题生成策略。
 */
public enum TitleMode {
    /** 去掉开头条款号后截断正文，再拼回规范化后的条款号。 */
    RULE_BASED,

    /** 直接使用原始文本开头作为标题。 */
    SOURCE_PREFIX,

    /** 返回固定 mock 标题。 */
    MOCK,

    /** 预留给后续 LLM 标题生成。 */
    LLM;

    /**
     * 解析可选的请求值；空值默认使用 RULE_BASED。
     */
    public static TitleMode from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return RULE_BASED;
        }
        return TitleMode.valueOf(value.trim().toUpperCase());
    }
}
