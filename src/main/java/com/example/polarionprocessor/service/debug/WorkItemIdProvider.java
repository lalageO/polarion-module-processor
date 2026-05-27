package com.example.polarionprocessor.service.debug;

import com.example.polarionprocessor.model.shared.ImportItemResult;

/**
 * Work Item id 分配扩展点，用于为候选 item 分配 mock 或真实 Polarion Work Item id。
 */
public interface WorkItemIdProvider {

    /**
     * 根据候选序号返回 Work Item id。
     */
    String provide(ImportItemResult item, int index);
}
