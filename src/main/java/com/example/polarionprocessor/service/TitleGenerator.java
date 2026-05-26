package com.example.polarionprocessor.service;

import com.example.polarionprocessor.model.ImportItemResult;
import com.example.polarionprocessor.model.ModuleProcessRequest;

/**
 * Extension point for local rule-based, mock, or future LLM title generation.
 */
public interface TitleGenerator {

    /**
     * Generates a title for one candidate item.
     */
    String generate(ImportItemResult item, ModuleProcessRequest request);
}
