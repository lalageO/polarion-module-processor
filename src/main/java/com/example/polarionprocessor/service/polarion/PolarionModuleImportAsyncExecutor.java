package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.model.polarion.PolarionModuleImportRequest;
import com.example.polarionprocessor.model.polarion.PolarionModuleImportResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 正式导入链路的后台执行入口。
 */
@Service
public class PolarionModuleImportAsyncExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarionModuleImportAsyncExecutor.class);

    private final PolarionModuleImportService importService;

    public PolarionModuleImportAsyncExecutor(PolarionModuleImportService importService) {
        this.importService = importService;
    }

    @Async
    public void submit(PolarionModuleImportRequest request) {
        try {
            PolarionModuleImportResponse response = importService.importModule(request);
            LOGGER.info("Polarion module import finished. success={}, status={}, jobId={}, message={}",
                    response.getSuccess(),
                    response.getStatus(),
                    response.getJobId(),
                    response.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("Polarion module import async task failed before result was written.", e);
        }
    }
}
