package com.example.polarionprocessor.service;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.model.ImportItemResult;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

/**
 * Version-1 Work Item id provider that returns deterministic mock ids.
 */
@Service
public class MockWorkItemIdProvider implements WorkItemIdProvider {

    private final ModuleProcessorProperties properties;

    public MockWorkItemIdProvider(ModuleProcessorProperties properties) {
        this.properties = properties;
    }

    /**
     * Builds ids as PREFIX-000001, PREFIX-000002, and so on.
     */
    @Override
    public String provide(ImportItemResult item, int index) {
        String prefix = item.getWorkItemId();
        if (!TextUtils.hasText(prefix)) {
            prefix = properties.getDefaultMockIdPrefix();
        }
        if (!TextUtils.hasText(prefix)) {
            prefix = "MOCK";
        }
        return String.format("%s-%06d", prefix.trim(), index);
    }
}
