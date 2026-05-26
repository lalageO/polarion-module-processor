package com.example.polarionprocessor.service;

import com.example.polarionprocessor.model.ImportItemResult;

/**
 * Extension point for assigning mock or real Polarion Work Item ids to candidate items.
 */
public interface WorkItemIdProvider {

    /**
     * Returns the Work Item id for the given candidate index.
     */
    String provide(ImportItemResult item, int index);
}
