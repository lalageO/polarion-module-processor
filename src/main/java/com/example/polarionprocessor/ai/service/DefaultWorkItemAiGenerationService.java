package com.example.polarionprocessor.ai.service;

import com.example.polarionprocessor.ai.client.AiChatClient;
import com.example.polarionprocessor.ai.config.AiProperties;
import com.example.polarionprocessor.ai.model.AiChatResponse;
import com.example.polarionprocessor.ai.model.AiGenerateRequest;
import com.example.polarionprocessor.ai.model.AiGenerateResult;
import com.example.polarionprocessor.ai.model.AiPromptType;
import com.example.polarionprocessor.ai.parser.AiFieldValidator;
import com.example.polarionprocessor.ai.parser.AiResponseParser;
import com.example.polarionprocessor.ai.prompt.PromptRenderer;
import com.example.polarionprocessor.ai.prompt.PromptTemplateLoader;
import com.example.polarionprocessor.model.polarion.PolarionEnumOptionRequest;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 默认 AI 字段生成实现。
 */
@Service
public class DefaultWorkItemAiGenerationService implements WorkItemAiGenerationService {

    private final AiProperties properties;
    private final PromptTemplateLoader templateLoader;
    private final PromptRenderer promptRenderer;
    private final AiChatClient chatClient;
    private final AiResponseParser responseParser;
    private final AiFieldValidator fieldValidator;

    public DefaultWorkItemAiGenerationService(AiProperties properties,
                                              PromptTemplateLoader templateLoader,
                                              PromptRenderer promptRenderer,
                                              AiChatClient chatClient,
                                              AiResponseParser responseParser,
                                              AiFieldValidator fieldValidator) {
        this.properties = properties;
        this.templateLoader = templateLoader;
        this.promptRenderer = promptRenderer;
        this.chatClient = chatClient;
        this.responseParser = responseParser;
        this.fieldValidator = fieldValidator;
    }

    @Override
    public boolean shouldRun(boolean dryRun) {
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            return false;
        }
        return !dryRun || Boolean.TRUE.equals(properties.getRunInDryRun());
    }

    @Override
    public AiGenerateResult generate(AiGenerateRequest request) {
        AiPromptType promptType = promptType(request == null ? null : request.getProjectId());
        String prompt = null;
        try {
            prompt = buildPrompt(promptType, request);
            AiChatResponse chatResponse = chatClient.chat(prompt);
            Map<String, Object> parsed = responseParser.parseObject(chatResponse.getContent());
            AiGenerateResult result = new AiGenerateResult();
            result.setSuccess(Boolean.TRUE);
            result.setPromptType(promptType);
            result.setPrompt(prompt);
            result.setModel(firstText(chatResponse.getModel(), properties.getModel()));
            result.setRawResponse(chatResponse.getRawResponse());
            result.setUsage(chatResponse.getUsage());
            result.setParsedFields(parsed);
            result.setTitle(fieldValidator.validateTitle(parsed.get("title")));
            result.setFields(fieldValidator.validateFields(
                    parsed,
                    request == null ? null : request.getEnumOptions(),
                    AiPromptType.RMT_FIELDS.equals(promptType)));
            if (!TextUtils.hasText(result.getTitle()) && result.getFields().isEmpty()) {
                result.setSuccess(Boolean.FALSE);
                result.setErrorMessage("AI response has no accepted title or custom field values");
            }
            return result;
        } catch (RuntimeException e) {
            AiGenerateResult result = AiGenerateResult.failure(promptType, e.getMessage());
            result.setPrompt(prompt);
            result.setModel(properties.getModel());
            return result;
        }
    }

    private String buildPrompt(AiPromptType promptType, AiGenerateRequest request) {
        String template = templateLoader.load(AiPromptType.RMT_FIELDS.equals(promptType)
                ? properties.getFieldPromptPath()
                : properties.getTitlePromptPath());
        Map<String, String> variables = new LinkedHashMap<String, String>();
        variables.put("PROJECT_ID", request == null ? "" : request.getProjectId());
        variables.put("MODULE_NAME", request == null ? "" : request.getModuleName());
        variables.put("OUTLINE_NO", request == null ? "" : request.getOutlineNo());
        variables.put("RULE_TITLE", request == null ? "" : request.getRuleTitle());
        variables.put("DESCRIPTION", request == null ? "" : request.getDescription());
        variables.put("REQUIREMENTSOURCE_OPTIONS", formatOptions(request, "requirementsource"));
        variables.put("REQTYPE_OPTIONS", formatOptions(request, "reqType"));
        return promptRenderer.render(template, variables);
    }

    private String formatOptions(AiGenerateRequest request, String fieldId) {
        if (request == null || request.getEnumOptions() == null || request.getEnumOptions().get(fieldId) == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        List<PolarionEnumOptionRequest> options = request.getEnumOptions().get(fieldId);
        for (PolarionEnumOptionRequest option : options) {
            if (option == null || !TextUtils.hasText(option.getId())) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(System.lineSeparator());
            }
            builder.append("* \"").append(option.getId()).append("\"");
            if (TextUtils.hasText(option.getName())) {
                builder.append(" - ").append(option.getName());
            }
        }
        return builder.toString();
    }

    private AiPromptType promptType(String projectId) {
        if (!TextUtils.hasText(projectId) || properties.getFieldGenerationProjects() == null) {
            return AiPromptType.TITLE_ONLY;
        }
        String normalizedProjectId = projectId.trim().toLowerCase(Locale.ROOT);
        for (String configuredProject : properties.getFieldGenerationProjects()) {
            if (configuredProject != null
                    && normalizedProjectId.equals(configuredProject.trim().toLowerCase(Locale.ROOT))) {
                return AiPromptType.RMT_FIELDS;
            }
        }
        return AiPromptType.TITLE_ONLY;
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (TextUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
