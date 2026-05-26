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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleProcessServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void processMockReplacementEndToEnd() throws Exception {
        ModuleProcessService service = buildService();

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<module>\n"
                + "  <field id=\"title\">R171e2</field>\n"
                + "  <field id=\"homePageContent\" text-type=\"text/html\"><![CDATA[\n"
                + "    <p id=\"polarion_101\">5.1.1. The system shall operate safely.</p>\n"
                + "    <p id=\"polarion_102\">5.3.5. Response to System boundaries</p>\n"
                + "  ]]></field>\n"
                + "</module>";

        ModuleProcessRequest request = request(xml);
        request.setDryRun(false);
        request.setReplaceMode("MOCK");

        ModuleProcessResponse response = service.process(request);

        assertTrue(response.getSuccess(), response.getMessage());
        assertEquals("R171e2", response.getModuleName());
        assertEquals(Integer.valueOf(2), response.getTotalParagraphCount());
        assertEquals(Integer.valueOf(2), response.getTotalItemCount());
        assertEquals(Integer.valueOf(2), response.getCandidateCount());
        assertEquals(Integer.valueOf(2), response.getReplacedCount());
        assertEquals(Integer.valueOf(0), response.getSkippedCount());
        assertNotNull(response.getOutputDir());

        Path processedXml = tempDir.resolve(response.getJobId()).resolve("processed_module.xml");
        String processedXmlContent = new String(Files.readAllBytes(processedXml), StandardCharsets.UTF_8);
        assertTrue(processedXmlContent.contains("module-workitem;params=id=MOCK-000001"));
        assertTrue(processedXmlContent.contains("module-workitem;params=id=MOCK-000002"));

        assertTrue(Files.exists(tempDir.resolve(response.getJobId()).resolve("import_result.json")));
        assertTrue(Files.exists(tempDir.resolve(response.getJobId()).resolve("import_preview.csv")));
    }

    @Test
    void groupsNumberedParagraphWithFollowingSubParagraphs() throws Exception {
        ModuleProcessService service = buildService();

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<module>\n"
                + "  <field id=\"title\">R171e2</field>\n"
                + "  <field id=\"homePageContent\" text-type=\"text/html\"><![CDATA[\n"
                + "    <p id=\"polarion_201\">2.2. Vehicle Type with regard to DCAS means a group of vehicles, which do not differ in such essential aspects as:</p>\n"
                + "    <p id=\"polarion_202\">(a) The system characteristics and design of DCAS;</p>\n"
                + "    <p id=\"polarion_203\">(b) Vehicle features which significantly influence the performances of DCAS.</p>\n"
                + "    <p id=\"polarion_204\">If within the manufacturer's designation of the vehicle type, DCAS consists of multiple features.</p>\n"
                + "    <p id=\"polarion_205\">2.3. Short heading</p>\n"
                + "  ]]></field>\n"
                + "</module>";

        ModuleProcessRequest request = request(xml);
        request.setDryRun(false);
        request.setReplaceMode("MOCK");
        request.setMockIdPrefix("FDP");

        ModuleProcessResponse response = service.process(request);

        assertTrue(response.getSuccess(), response.getMessage());
        assertEquals(Integer.valueOf(5), response.getTotalParagraphCount());
        assertEquals(Integer.valueOf(2), response.getTotalItemCount());
        assertEquals(Integer.valueOf(1), response.getCandidateCount());
        assertEquals(Integer.valueOf(1), response.getReplacedCount());
        assertEquals(Integer.valueOf(1), response.getSkippedCount());

        Path processedXml = tempDir.resolve(response.getJobId()).resolve("processed_module.xml");
        String processedXmlContent = new String(Files.readAllBytes(processedXml), StandardCharsets.UTF_8);
        assertTrue(processedXmlContent.contains("module-workitem;params=id=FDP-000001"));
        assertTrue(processedXmlContent.contains("2.3. Short heading"));
        assertFalse(processedXmlContent.contains("polarion_202"));
        assertFalse(processedXmlContent.contains("If within the manufacturer"));
    }

    private ModuleProcessService buildService() {
        ModuleProcessorProperties properties = new ModuleProcessorProperties();
        properties.setOutputDir(tempDir.toString());
        return new ModuleProcessService(
                properties,
                new ModuleXmlExtractor(),
                new ParagraphScanner(),
                new NumberedItemGrouper(),
                new ParagraphCandidateSelector(),
                new RuleBasedTitleGenerator(properties),
                new MockWorkItemIdProvider(properties),
                new ModuleXmlRewriter(),
                new ImportResultWriter(new ObjectMapper()),
                new ImportPreviewCsvWriter(properties));
    }

    private ModuleProcessRequest request(String xml) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "module.xml",
                "text/xml",
                xml.getBytes(StandardCharsets.UTF_8));
        ModuleProcessRequest request = new ModuleProcessRequest();
        request.setFile(file);
        return request;
    }
}
