package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.config.PolarionProperties;
import com.example.polarionprocessor.model.polarion.WorkItemCreateRequest;
import com.example.polarionprocessor.model.polarion.WorkItemCreateResult;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

/**
 * 真实 Polarion Work Item 创建器的 HTTP 实现占位。
 */
@Service
public class HttpPolarionWorkItemCreator implements PolarionWorkItemCreator {

    private final PolarionProperties properties;

    public HttpPolarionWorkItemCreator(PolarionProperties properties) {
        this.properties = properties;
    }

    /**
     * 当前 Polarion API 参数尚未确定；配置不完整时明确失败，不生成任何假 ID。
     */
    @Override
    public WorkItemCreateResult createOne(WorkItemCreateRequest request) {
        PolarionProperties.WorkItemApi api = properties.getWorkItemApi();
        if (api == null
                || !Boolean.TRUE.equals(api.getEnabled())
                || !TextUtils.hasText(api.getCreateUrl())) {
            return WorkItemCreateResult.failure(
                    "POLARION_API_NOT_CONFIGURED",
                    "Polarion Work Item API is not configured");
        }
        return WorkItemCreateResult.failure(
                "POLARION_API_NOT_IMPLEMENTED",
                "Polarion Work Item API request body is not implemented yet");
    }
}
