package com.example.polarionprocessor.ai.service;

import com.example.polarionprocessor.ai.client.AiChatClient;
import com.example.polarionprocessor.ai.config.AiProperties;
import com.example.polarionprocessor.ai.model.AiChatResponse;
import com.example.polarionprocessor.ai.model.AiGenerateRequest;
import com.example.polarionprocessor.ai.model.AiGenerateResult;
import com.example.polarionprocessor.ai.parser.AiFieldValidator;
import com.example.polarionprocessor.ai.parser.AiResponseParser;
import com.example.polarionprocessor.ai.prompt.PromptRenderer;
import com.example.polarionprocessor.ai.prompt.PromptTemplateLoader;
import com.example.polarionprocessor.model.polarion.PolarionEnumOptionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultWorkItemAiGenerationServiceTest {

    @Test
    void shouldNotRunWhenDisabled() {
        AiProperties properties = new AiProperties();
        DefaultWorkItemAiGenerationService service = service(properties, "{\"title\":\"AI标题\"}");

        assertFalse(service.shouldRun(false));
    }

    @Test
    void rmtProjectGeneratesTitleAndCustomFields() {
        AiProperties properties = new AiProperties();
        properties.setEnabled(true);
        properties.setFieldGenerationProjects(Arrays.asList("RMT_Platfrom_Demo"));
        DefaultWorkItemAiGenerationService service = service(
                properties,
                "模型说明 {\"title\":\"驾驶员参与能力评估\",\"requirementsource\":\"regulation\","
                        + "\"reqType\":\"FUNCTIONAL\",\"verificationcriteria\":\"<p>通过场景测试验证能力。</p>\"}");

        AiGenerateRequest request = new AiGenerateRequest();
        request.setProjectId("RMT_Platfrom_Demo");
        request.setModuleName("R171_01");
        request.setOutlineNo("5.1.1.");
        request.setRuleTitle("5.1.1 Driver engagement");
        request.setDescription("DCAS shall evaluate driver involvement.");
        Map<String, java.util.List<PolarionEnumOptionRequest>> enumOptions =
                new LinkedHashMap<String, java.util.List<PolarionEnumOptionRequest>>();
        enumOptions.put("requirementsource", Arrays.asList(
                new PolarionEnumOptionRequest("internal", "Internal内部的需求"),
                new PolarionEnumOptionRequest("external", "External来自外部的需求"),
                new PolarionEnumOptionRequest("Regulation", "Regulation法规需求")));
        enumOptions.put("reqType", Arrays.asList(
                new PolarionEnumOptionRequest("functional", "Functional Requirement 功能需求"),
                new PolarionEnumOptionRequest("constraint", "Constraint 约束条件")));
        request.setEnumOptions(enumOptions);

        AiGenerateResult result = service.generate(request);

        assertTrue(result.getSuccess(), result.getErrorMessage());
        assertEquals("驾驶员参与能力评估", result.getTitle());
        assertEquals("Regulation", result.getFields().get("requirementsource"));
        assertEquals("functional", result.getFields().get("reqType"));
        assertEquals("通过场景测试验证能力。", result.getFields().get("verificationcriteria"));
    }

    private DefaultWorkItemAiGenerationService service(AiProperties properties, String content) {
        ObjectMapper objectMapper = new ObjectMapper();
        return new DefaultWorkItemAiGenerationService(
                properties,
                new PromptTemplateLoader(new DefaultResourceLoader()),
                new PromptRenderer(),
                new StaticAiChatClient(content),
                new AiResponseParser(objectMapper),
                new AiFieldValidator());
    }

    private static class StaticAiChatClient implements AiChatClient {

        private final String content;

        StaticAiChatClient(String content) {
            this.content = content;
        }

        @Override
        public AiChatResponse chat(String prompt) {
            AiChatResponse response = new AiChatResponse();
            response.setModel("primary");
            response.setContent(content);
            response.setRawResponse("{\"choices\":[{\"message\":{\"content\":\"" + content + "\"}}]}");
            return response;
        }
    }
}
