package com.example.polarionprocessor.service;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.model.ModuleProcessRequest;
import com.example.polarionprocessor.model.ModuleProcessResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleProcessServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void processMockReplacementEndToEnd() throws Exception {
        ModuleProcessorProperties properties = new ModuleProcessorProperties();
        properties.setOutputDir(tempDir.toString());

        ModuleProcessService service = new ModuleProcessService(
                properties,
                new ModuleXmlExtractor(),
                new ParagraphScanner(),
                new ParagraphCandidateSelector(),
                new RuleBasedTitleGenerator(properties),
                new MockWorkItemIdProvider(properties),
                new ModuleXmlRewriter(),
                new ImportResultWriter(new ObjectMapper()),
                new ImportPreviewCsvWriter(properties));

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<module>\n"
                + "  <field id=\"title\">R171e2</field>\n"
                + "  <field id=\"homePageContent\" text-type=\"text/html\"><![CDATA[\n"
                + "    <p id=\"polarion_101\">5.1.1. The system shall operate safely.</p>\n"
                + "    <p id=\"polarion_102\">5.3.5. Response to System boundaries</p>\n"
                + "  ]]></field>\n"
                + "</module>";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "module.xml",
                "text/xml",
                xml.getBytes(StandardCharsets.UTF_8));
        ModuleProcessRequest request = new ModuleProcessRequest();
        request.setFile(file);
        request.setDryRun(false);
        request.setReplaceMode("MOCK");

        ModuleProcessResponse response = service.process(request);

        assertTrue(response.getSuccess());
        assertEquals("R171e2", response.getModuleName());
        assertEquals(Integer.valueOf(2), response.getTotalParagraphCount());
        assertEquals(Integer.valueOf(1), response.getCandidateCount());
        assertEquals(Integer.valueOf(1), response.getReplacedCount());
        assertEquals(Integer.valueOf(1), response.getSkippedCount());
        assertNotNull(response.getOutputDir());

        Path processedXml = tempDir.resolve(response.getJobId()).resolve("processed_module.xml");
        String processedXmlContent = new String(Files.readAllBytes(processedXml), StandardCharsets.UTF_8);
        assertTrue(processedXmlContent.contains("module-workitem;params=id=MOCK-000001"));
        assertTrue(processedXmlContent.contains("5.3.5. Response to System boundaries"));

        assertTrue(Files.exists(tempDir.resolve(response.getJobId()).resolve("import_result.json")));
        assertTrue(Files.exists(tempDir.resolve(response.getJobId()).resolve("import_preview.csv")));
    }
}
