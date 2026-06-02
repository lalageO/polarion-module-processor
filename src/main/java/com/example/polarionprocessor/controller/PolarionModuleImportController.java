package com.example.polarionprocessor.controller;

import com.example.polarionprocessor.enums.JobStatus;
import com.example.polarionprocessor.model.polarion.PolarionModuleImportRequest;
import com.example.polarionprocessor.model.polarion.PolarionModuleImportResponse;
import com.example.polarionprocessor.model.polarion.PolarionModuleLocation;
import com.example.polarionprocessor.service.polarion.PolarionModuleImportAsyncExecutor;
import com.example.polarionprocessor.service.polarion.PolarionModuleUrlParser;
import com.example.polarionprocessor.service.shared.ModuleProcessException;
import com.example.polarionprocessor.util.TextUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 正式业务接口：从 Polarion 下载 module.xml，创建真实 Work Item，并生成替换后的 XML。
 */
@RestController
@RequestMapping("/api/polarion/module")
public class PolarionModuleImportController {

    private final ObjectMapper objectMapper;
    private final PolarionModuleUrlParser moduleUrlParser;
    private final PolarionModuleImportAsyncExecutor importAsyncExecutor;

    public PolarionModuleImportController(ObjectMapper objectMapper,
                                          PolarionModuleUrlParser moduleUrlParser,
                                          PolarionModuleImportAsyncExecutor importAsyncExecutor) {
        this.objectMapper = objectMapper;
        this.moduleUrlParser = moduleUrlParser;
        this.importAsyncExecutor = importAsyncExecutor;
    }

    @PostMapping("/import")
    public ResponseEntity<PolarionModuleImportResponse> importModule(@RequestBody String requestBody) {
        PolarionModuleImportRequest request;
        try {
            request = parseRequest(requestBody);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PolarionModuleImportResponse.failure("REQUEST_BODY_INVALID: " + e.getMessage()));
        } catch (ModuleProcessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PolarionModuleImportResponse.failure(e.getErrorCode() + ": " + e.getMessage()));
        }
        try {
            validateRequest(request);
        } catch (ModuleProcessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PolarionModuleImportResponse.failure(e.getErrorCode() + ": " + e.getMessage()));
        }
        importAsyncExecutor.submit(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(buildSubmittedResponse(request));
    }

    /**
     * 正式入口以 URL 为核心输入；兼容纯 URL、JSON 字符串和 JSON 对象三种 body。
     */
    private PolarionModuleImportRequest parseRequest(String requestBody) throws JsonProcessingException {
        String body = requestBody == null ? "" : requestBody.trim();
        if (!TextUtils.hasText(body)) {
            return new PolarionModuleImportRequest();
        }
        PolarionModuleImportRequest request;
        if (body.startsWith("{")) {
            request = objectMapper.readValue(body, PolarionModuleImportRequest.class);
        } else if (isFormBody(body)) {
            request = parseFormRequest(body);
        } else {
            request = new PolarionModuleImportRequest();
            request.setModuleUrl(unquote(body));
        }
        enrichFromModuleUrl(request);
        return request;
    }

    private String unquote(String value) throws JsonProcessingException {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return objectMapper.readValue(value, String.class);
        }
        return value;
    }

    private boolean isFormBody(String body) {
        int equalsIndex = body.indexOf('=');
        if (equalsIndex <= 0) {
            return false;
        }
        return isKnownFormKey(decodeFormValue(body.substring(0, equalsIndex)));
    }

    private PolarionModuleImportRequest parseFormRequest(String body) {
        PolarionModuleImportRequest request = new PolarionModuleImportRequest();
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            int equalsIndex = pair.indexOf('=');
            if (equalsIndex <= 0) {
                continue;
            }
            String key = normalizeFormKey(decodeFormValue(pair.substring(0, equalsIndex)));
            String value = decodeFormValue(pair.substring(equalsIndex + 1));
            applyFormValue(request, key, value);
        }
        return request;
    }

    private void applyFormValue(PolarionModuleImportRequest request, String key, String value) {
        if ("moduleurl".equals(key) || "url".equals(key) || "documenturl".equals(key)) {
            request.setModuleUrl(value);
        } else if ("baseurl".equals(key)) {
            request.setBaseUrl(value);
        } else if ("projectid".equals(key)) {
            request.setProjectId(value);
        } else if ("modulefolder".equals(key)) {
            request.setModuleFolder(value);
        } else if ("modulename".equals(key)) {
            request.setModuleName(value);
        } else if ("workitemtype".equals(key)) {
            request.setWorkItemType(value);
        } else if ("authorid".equals(key) || "anthorname".equals(key) || "username".equals(key) || "user".equals(key)) {
            request.setAuthorId(value);
        } else if ("authorname".equals(key)) {
            request.setAuthorName(value);
        } else if ("dryrun".equals(key)) {
            request.setDryRun(Boolean.valueOf(value));
        } else if ("requirekeyword".equals(key)) {
            request.setRequireKeyword(Boolean.valueOf(value));
        }
    }

    private boolean isKnownFormKey(String key) {
        String normalized = normalizeFormKey(key);
        return "moduleurl".equals(normalized)
                || "url".equals(normalized)
                || "documenturl".equals(normalized)
                || "baseurl".equals(normalized);
    }

    private String normalizeFormKey(String key) {
        return key == null ? "" : key.trim().replace("-", "").replace("_", "").toLowerCase();
    }

    private String decodeFormValue(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ModuleProcessException("REQUEST_BODY_INVALID", "Form body decode failed", e);
        }
    }

    /**
     * 在 Controller 层把 URL 拆成 Request 的显式字段，Service 后续只按统一 request 处理。
     */
    private void enrichFromModuleUrl(PolarionModuleImportRequest request) {
        if (request == null || !TextUtils.hasText(request.getModuleUrl())) {
            return;
        }
        request.setModuleUrl(normalizeModuleUrl(request.getModuleUrl()));
        PolarionModuleLocation location = moduleUrlParser.parse(request.getModuleUrl());
        request.setProjectId(location.getProjectId());
        request.setModuleFolder(location.getModuleFolder());
        request.setModuleName(location.getModuleName());
        request.setBaseUrl(location.getBaseUrl());
    }

    private String normalizeModuleUrl(String moduleUrl) {
        return moduleUrl == null ? null : moduleUrl.trim().replace(" ", "%20");
    }

    private void validateRequest(PolarionModuleImportRequest request) {
        if (request == null) {
            throw new ModuleProcessException("REQUEST_PARAMETER_INVALID", "request body is required");
        }
        if (TextUtils.hasText(request.getModuleUrl())) {
            return;
        }
        if (TextUtils.hasText(request.getBaseUrl())
                && TextUtils.hasText(request.getProjectId())
                && TextUtils.hasText(request.getModuleFolder())
                && TextUtils.hasText(request.getModuleName())) {
            return;
        }
        throw new ModuleProcessException(
                "REQUEST_PARAMETER_INVALID",
                "moduleUrl is required, or baseUrl/projectId/moduleFolder/moduleName must all be provided");
    }

    private PolarionModuleImportResponse buildSubmittedResponse(PolarionModuleImportRequest request) {
        PolarionModuleImportResponse response = new PolarionModuleImportResponse();
        response.setSuccess(true);
        response.setStatus(JobStatus.SUBMITTED.name());
        response.setProjectId(request.getProjectId());
        response.setModuleFolder(request.getModuleFolder());
        response.setModuleName(request.getModuleName());
        response.setDryRun(request.getDryRun() == null ? Boolean.FALSE : request.getDryRun());
        response.setMessage("任务已提交，正在处理中");
        return response;
    }
}
