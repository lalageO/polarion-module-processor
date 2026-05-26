package com.example.polarionprocessor.enums;

public enum ReplaceMode {
    NONE,
    MOCK,
    MAPPING;

    public static ReplaceMode from(String value, String defaultValue) {
        String mode = value == null || value.trim().isEmpty() ? defaultValue : value;
        if (mode == null || mode.trim().isEmpty()) {
            return NONE;
        }
        return ReplaceMode.valueOf(mode.trim().toUpperCase());
    }
}
