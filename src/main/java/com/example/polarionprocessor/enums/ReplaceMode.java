package com.example.polarionprocessor.enums;

/**
 * Replacement behavior for processed_module.xml generation.
 */
public enum ReplaceMode {
    /** Do not replace candidate HTML. */
    NONE,

    /** Replace candidate blocks with mock module-workitem divs. */
    MOCK,

    /** Reserved for caller-provided id mappings; not implemented in version 1. */
    MAPPING;

    /**
     * Resolves a request value with a default while keeping invalid values explicit.
     */
    public static ReplaceMode from(String value, String defaultValue) {
        String mode = value == null || value.trim().isEmpty() ? defaultValue : value;
        if (mode == null || mode.trim().isEmpty()) {
            return NONE;
        }
        return ReplaceMode.valueOf(mode.trim().toUpperCase());
    }
}
