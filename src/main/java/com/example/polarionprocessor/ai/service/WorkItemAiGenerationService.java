package com.example.polarionprocessor.ai.service;

import com.example.polarionprocessor.ai.model.AiGenerateRequest;
import com.example.polarionprocessor.ai.model.AiGenerateResult;

/**
 * 单个 Work Item 的 AI 字段生成服务。
 */
public interface WorkItemAiGenerationService {

    boolean shouldRun(boolean dryRun);

    AiGenerateResult generate(AiGenerateRequest request);
}
