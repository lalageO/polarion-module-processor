package com.example.polarionprocessor.enums;

/**
 * processed_module.xml 生成阶段的替换模式。
 */
public enum ReplaceMode {
    /** 不替换候选 HTML。 */
    NONE,

    /** 使用 mock 的 module-workitem div 替换候选块。 */
    MOCK,

    /** 预留给外部传入 ID 映射的模式，第一版暂不实现。 */
    MAPPING;

    /**
     * 基于请求值和默认值解析替换模式；非法值会显式抛出异常。
     */
    public static ReplaceMode from(String value, String defaultValue) {
        String mode = value == null || value.trim().isEmpty() ? defaultValue : value;
        if (mode == null || mode.trim().isEmpty()) {
            return NONE;
        }
        return ReplaceMode.valueOf(mode.trim().toUpperCase());
    }
}
