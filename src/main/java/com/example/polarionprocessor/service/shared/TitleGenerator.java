package com.example.polarionprocessor.service.shared;

import com.example.polarionprocessor.model.shared.ImportItemResult;
import com.example.polarionprocessor.model.debug.ModuleProcessRequest;

/**
 * 标题生成扩展点，支持本地规则、mock，以及后续 LLM 实现。
 */
public interface TitleGenerator {

    /**
     * 为单个候选 item 生成标题。
     */
    String generate(ImportItemResult item, ModuleProcessRequest request);
}
