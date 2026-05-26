package com.example.polarionprocessor.enums;

/**
 * Per-item processing status written to JSON and CSV outputs.
 */
public enum ItemStatus {
    /** Item was not selected as a candidate. */
    SKIPPED,

    /** Item passed candidate selection and is waiting for replacement/import. */
    CANDIDATE,

    /** Item was replaced successfully in processed_module.xml. */
    REPLACED,

    /** Item was selected but could not be replaced. */
    REPLACE_FAILED,

    /** Reserved general failure status for later processing steps. */
    FAILED
}
