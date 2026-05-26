package com.example.polarionprocessor.enums;

/**
 * Stable machine-readable skip reasons for non-candidate items.
 */
public enum SkipReason {
    /** No visible text after normalization. */
    EMPTY_PARAGRAPH,

    /** Missing Polarion paragraph id. */
    NO_PARAGRAPH_ID,

    /** Paragraph id does not match the expected polarion_N format. */
    INVALID_PARAGRAPH_ID,

    /** No multi-level numeric outline number was detected. */
    NO_OUTLINE_NO,

    /** Outline depth is below the configured minimum. */
    OUTLINE_DEPTH_TOO_LOW,

    /** Keyword filtering is enabled and no requirement-like keyword was found. */
    NO_REQUIREMENT_KEYWORD,

    /** Text appears to be only a heading or is too short for a level-2 clause. */
    TITLE_ONLY,

    /** Reserved for table-of-contents detection. */
    TOC,

    /** Reserved for future semantic requirement-likeness checks. */
    NOT_REQUIREMENT_LIKE
}
