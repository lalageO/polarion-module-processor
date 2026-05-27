package com.example.polarionprocessor.controller;

import com.example.polarionprocessor.model.debug.ModuleProcessRequest;
import com.example.polarionprocessor.model.debug.ModuleProcessResponse;
import com.example.polarionprocessor.service.debug.ModuleProcessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/module")
@Deprecated
public class ModuleProcessController {

    private final ModuleProcessService moduleProcessService;

    public ModuleProcessController(ModuleProcessService moduleProcessService) {
        this.moduleProcessService = moduleProcessService;
    }

    @PostMapping("/process")
    @Deprecated
    public ResponseEntity<ModuleProcessResponse> process(@RequestParam("file") MultipartFile file,
                                                         @RequestParam(value = "moduleName", required = false) String moduleName,
                                                         @RequestParam(value = "dryRun", required = false) Boolean dryRun,
                                                         @RequestParam(value = "replaceMode", required = false) String replaceMode,
                                                         @RequestParam(value = "mockIdPrefix", required = false) String mockIdPrefix,
                                                         @RequestParam(value = "includeSubItems", required = false) Boolean includeSubItems,
                                                         @RequestParam(value = "requireKeyword", required = false) Boolean requireKeyword,
                                                         @RequestParam(value = "minOutlineDepth", required = false) Integer minOutlineDepth,
                                                         @RequestParam(value = "titleMode", required = false) String titleMode) {
        ModuleProcessRequest request = new ModuleProcessRequest();
        request.setFile(file);
        request.setModuleName(moduleName);
        request.setDryRun(dryRun);
        request.setReplaceMode(replaceMode);
        request.setMockIdPrefix(mockIdPrefix);
        request.setIncludeSubItems(includeSubItems);
        request.setRequireKeyword(requireKeyword);
        request.setMinOutlineDepth(minOutlineDepth);
        request.setTitleMode(titleMode);

        ModuleProcessResponse response = moduleProcessService.process(request);
        HttpStatus status = Boolean.TRUE.equals(response.getSuccess()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
}
