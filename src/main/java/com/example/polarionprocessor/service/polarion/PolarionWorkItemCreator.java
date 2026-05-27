package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.model.polarion.WorkItemCreateRequest;
import com.example.polarionprocessor.model.polarion.WorkItemCreateResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Polarion Work Item 创建扩展点。
 */
public interface PolarionWorkItemCreator {

    /**
     * 创建单个 Work Item。正式流程第二版只顺序调用该方法。
     */
    WorkItemCreateResult createOne(WorkItemCreateRequest request);

    /**
     * 预留批量创建默认实现，当前仍然逐条顺序调用 createOne。
     */
    default List<WorkItemCreateResult> createBatch(List<WorkItemCreateRequest> requests) {
        List<WorkItemCreateResult> results = new ArrayList<WorkItemCreateResult>();
        for (WorkItemCreateRequest request : requests) {
            results.add(createOne(request));
        }
        return results;
    }
}
