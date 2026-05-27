package com.example.polarionprocessor.controller;

import com.example.polarionprocessor.model.polarion.PolarionModuleImportRequest;
import com.example.polarionprocessor.model.polarion.PolarionModuleImportResponse;
import com.example.polarionprocessor.service.polarion.PolarionModuleImportService;
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

    private final PolarionModuleImportService importService;

    public PolarionModuleImportController(PolarionModuleImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/import")
    public ResponseEntity<PolarionModuleImportResponse> importModule(@RequestBody PolarionModuleImportRequest request) {
        PolarionModuleImportResponse response = importService.importModule(request);
        HttpStatus status = Boolean.TRUE.equals(response.getSuccess()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
}
