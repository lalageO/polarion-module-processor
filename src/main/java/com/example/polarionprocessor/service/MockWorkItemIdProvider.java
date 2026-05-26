package com.example.polarionprocessor.service;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.model.ImportItemResult;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

@Service
public class MockWorkItemIdProvider implements WorkItemIdProvider {

    private final ModuleProcessorProperties properties;

    public MockWorkItemIdProvider(ModuleProcessorProperties properties) {
        this.properties = properties;
    }

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
