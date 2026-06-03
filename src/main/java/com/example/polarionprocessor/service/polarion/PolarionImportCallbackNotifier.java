package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.config.PolarionProperties;
import com.example.polarionprocessor.model.polarion.PolarionImportCallbackRequest;
import com.example.polarionprocessor.model.polarion.PolarionModuleImportResponse;
import com.example.polarionprocessor.util.TextUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 任务结束后通知公司内部后端系统。
 */
@Service
public class PolarionImportCallbackNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarionImportCallbackNotifier.class);
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAIL = "FAIL";

    private final PolarionProperties properties;
    private final RestTemplate callbackRestTemplate;
    private final ObjectMapper objectMapper;

    public PolarionImportCallbackNotifier(PolarionProperties properties,
                                          @Qualifier("polarionImportCallbackRestTemplate") RestTemplate callbackRestTemplate,
                                          ObjectMapper objectMapper) {
        this.properties = properties;
        this.callbackRestTemplate = callbackRestTemplate;
        this.objectMapper = objectMapper;
    }

    public void notifyFinished(PolarionModuleImportResponse response) {
        PolarionProperties.ImportCallback callback = properties.getImportCallback();
        if (callback == null
                || !Boolean.TRUE.equals(callback.getEnabled())
                || !TextUtils.hasText(callback.getUrl())
                || response == null) {
            return;
        }
        PolarionImportCallbackRequest callbackRequest = new PolarionImportCallbackRequest(
                response.getJobId(),
                Boolean.TRUE.equals(response.getSuccess()) ? STATUS_COMPLETED : STATUS_FAIL,
                toResultJson(response));
        try {
            callbackRestTemplate.postForEntity(
                    callback.getUrl(),
                    new HttpEntity<PolarionImportCallbackRequest>(callbackRequest, jsonHeaders()),
                    String.class);
            LOGGER.info("Polarion import callback sent: jobId={}, statusCode={}",
                    callbackRequest.getJobId(),
                    callbackRequest.getStatusCode());
        } catch (RestClientException e) {
            LOGGER.warn("Polarion import callback failed: jobId={}, message={}",
                    callbackRequest.getJobId(),
                    e.getMessage());
        }
    }

    private String toResultJson(PolarionModuleImportResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Polarion import callback result JSON serialize failed: jobId={}, message={}",
                    response.getJobId(),
                    e.getMessage());
            return "{}";
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
