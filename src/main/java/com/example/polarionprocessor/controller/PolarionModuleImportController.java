package com.example.polarionprocessor.controller;

import com.example.polarionprocessor.model.polarion.PolarionModuleImportRequest;
import com.example.polarionprocessor.model.polarion.PolarionModuleImportResponse;
import com.example.polarionprocessor.model.polarion.PolarionModuleLocation;
import com.example.polarionprocessor.service.polarion.PolarionModuleImportService;
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

/**
 * 正式业务接口：从 Polarion 下载 module.xml，创建真实 Work Item，并生成替换后的 XML。
 */
@RestController
@RequestMapping("/api/polarion/module")
public class PolarionModuleImportController {

    private final ObjectMapper objectMapper;
    private final PolarionModuleUrlParser moduleUrlParser;
    private final PolarionModuleImportService importService;

    public PolarionModuleImportController(ObjectMapper objectMapper,
                                          PolarionModuleUrlParser moduleUrlParser,
                                          PolarionModuleImportService importService) {
        this.objectMapper = objectMapper;
        this.moduleUrlParser = moduleUrlParser;
        this.importService = importService;
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
        PolarionModuleImportResponse response = importService.importModule(request);
        HttpStatus status = Boolean.TRUE.equals(response.getSuccess()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
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

    /**
     * 在 Controller 层把 URL 拆成 Request 的显式字段，Service 后续只按统一 request 处理。
     */
    private void enrichFromModuleUrl(PolarionModuleImportRequest request) {
        if (request == null || !TextUtils.hasText(request.getModuleUrl())) {
            return;
        }
        PolarionModuleLocation location = moduleUrlParser.parse(request.getModuleUrl());
        request.setProjectId(location.getProjectId());
        request.setModuleFolder(location.getModuleFolder());
        request.setModuleName(location.getModuleName());
        request.setBaseUrl(location.getBaseUrl());
    }
}
