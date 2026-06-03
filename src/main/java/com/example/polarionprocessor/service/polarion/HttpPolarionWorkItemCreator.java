package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.config.PolarionProperties;
import com.example.polarionprocessor.model.polarion.WorkItemCreateApiRequest;
import com.example.polarionprocessor.model.polarion.WorkItemCreateApiResponse;
import com.example.polarionprocessor.model.polarion.WorkItemCreateRequest;
import com.example.polarionprocessor.model.polarion.WorkItemCreateResult;
import com.example.polarionprocessor.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 真实 Polarion Work Item 创建器的 HTTP 实现。
 */
@Service
public class HttpPolarionWorkItemCreator implements PolarionWorkItemCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpPolarionWorkItemCreator.class);

    private final PolarionProperties properties;
    private final RestTemplate polarionWorkItemRestTemplate;
    private final WorkItemCreateApiRequestBuilder requestBuilder;

    public HttpPolarionWorkItemCreator(PolarionProperties properties,
                                       @Qualifier("polarionWorkItemRestTemplate") RestTemplate polarionWorkItemRestTemplate,
                                       WorkItemCreateApiRequestBuilder requestBuilder) {
        this.properties = properties;
        this.polarionWorkItemRestTemplate = polarionWorkItemRestTemplate;
        this.requestBuilder = requestBuilder;
    }

    /**
     * 调用公司内部 createItem 接口；失败只返回单项失败结果，不生成任何假 ID。
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
        WorkItemCreateApiRequest apiRequest = requestBuilder.build(request);
        try {
            LOGGER.info("Creating Polarion Work Item: title={}", safeTitle(request));
            ResponseEntity<WorkItemCreateApiResponse> response = polarionWorkItemRestTemplate.postForEntity(
                    api.getCreateUrl(),
                    new HttpEntity<WorkItemCreateApiRequest>(apiRequest, jsonHeaders()),
                    WorkItemCreateApiResponse.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                LOGGER.warn("Polarion Work Item API returned HTTP status={}", response.getStatusCodeValue());
                return WorkItemCreateResult.failure(
                        "POLARION_API_HTTP_ERROR",
                        "HTTP status " + response.getStatusCodeValue());
            }
            return parseResponse(response.getBody());
        } catch (HttpStatusCodeException e) {
            LOGGER.warn("Polarion Work Item API HTTP error status={}, body={}",
                    e.getRawStatusCode(),
                    abbreviate(e.getResponseBodyAsString(), 300));
            return WorkItemCreateResult.failure(
                    "POLARION_API_HTTP_ERROR",
                    "HTTP status " + e.getRawStatusCode() + ": " + abbreviate(e.getResponseBodyAsString(), 300));
        } catch (RestClientException e) {
            LOGGER.warn("Polarion Work Item API exception: {}", e.getMessage());
            return WorkItemCreateResult.failure("POLARION_API_EXCEPTION", e.getMessage());
        }
    }

    private WorkItemCreateResult parseResponse(WorkItemCreateApiResponse body) {
        if (body == null) {
            return WorkItemCreateResult.failure("POLARION_API_EMPTY_RESPONSE", "Polarion API response body is empty");
        }
        String workItemId = body.getData() == null ? null : body.getData().trim();
        if ("0".equals(body.getCode()) && TextUtils.hasText(workItemId)) {
            LOGGER.info("Polarion Work Item created: workItemId={}", workItemId);
            return WorkItemCreateResult.success(workItemId);
        }
        if ("0".equals(body.getCode())) {
            LOGGER.warn("Polarion Work Item API returned success code without workItemId: success={}, data={}, msg={}",
                    body.getSuccess(),
                    abbreviate(body.getData(), 120),
                    body.getMsg());
            return WorkItemCreateResult.failure(
                    "POLARION_API_NO_WORK_ITEM_ID",
                    "Polarion API returned code=0 but data/workItemId is empty; msg=" + body.getMsg());
        }
        String errorCode = TextUtils.hasText(body.getCode()) ? body.getCode() : "POLARION_API_FAILED";
        String errorMessage = TextUtils.hasText(body.getMsg()) ? body.getMsg() : "Polarion API returned failure";
        LOGGER.warn("Polarion Work Item create failed: code={}, success={}, data={}, msg={}",
                errorCode,
                body.getSuccess(),
                abbreviate(body.getData(), 120),
                errorMessage);
        return WorkItemCreateResult.failure(errorCode, errorMessage);
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String safeTitle(WorkItemCreateRequest request) {
        if (request == null) {
            return "";
        }
        return abbreviate(request.getTitle(), 120);
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
