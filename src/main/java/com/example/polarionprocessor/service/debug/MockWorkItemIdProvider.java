package com.example.polarionprocessor.service.debug;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.model.shared.ImportItemResult;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

/**
 * 第一版 Work Item id 提供器，返回确定性的 mock id。
 */
@Service
public class MockWorkItemIdProvider implements WorkItemIdProvider {

    private final ModuleProcessorProperties properties;

    public MockWorkItemIdProvider(ModuleProcessorProperties properties) {
        this.properties = properties;
    }

    /**
     * 生成 PREFIX-000001、PREFIX-000002 这种连续 id。
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
