package com.example.polarionprocessor.ai.prompt;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 轻量模板渲染器，按 ${KEY} 替换。
 */
@Service
public class PromptRenderer {

    public String render(String template, Map<String, String> variables) {
        String rendered = template == null ? "" : template;
        if (variables == null) {
            return rendered;
        }
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            rendered = rendered.replace("${" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
        }
        return rendered;
    }
}
