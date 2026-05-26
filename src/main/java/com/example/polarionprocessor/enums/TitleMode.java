package com.example.polarionprocessor.enums;

/**
 * Title generation strategy selected by request.titleMode.
 */
public enum TitleMode {
    /** Remove the leading outline number, truncate the text, and prepend the normalized outline number. */
    RULE_BASED,

    /** Use the beginning of the original source text directly. */
    SOURCE_PREFIX,

    /** Return a fixed mock title. */
    MOCK,

    /** Reserved for future LLM title generation. */
    LLM;

    /**
     * Resolves an optional request value; blank values use RULE_BASED.
     */
    public static TitleMode from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return RULE_BASED;
        }
        return TitleMode.valueOf(value.trim().toUpperCase());
    }
}
