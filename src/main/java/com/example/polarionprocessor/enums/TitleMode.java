package com.example.polarionprocessor.enums;

public enum TitleMode {
    RULE_BASED,
    SOURCE_PREFIX,
    MOCK,
    LLM;

    public static TitleMode from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return RULE_BASED;
        }
        return TitleMode.valueOf(value.trim().toUpperCase());
    }
}
