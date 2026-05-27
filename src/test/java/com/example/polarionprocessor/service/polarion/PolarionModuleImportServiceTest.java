package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.config.PolarionProperties;
import com.example.polarionprocessor.enums.ItemStatus;
import com.example.polarionprocessor.model.polarion.PolarionImportItemResult;
import com.example.polarionprocessor.model.polarion.PolarionModuleImportRequest;
import com.example.polarionprocessor.model.polarion.PolarionModuleImportResponse;
import com.example.polarionprocessor.model.polarion.WorkItemCreateRequest;
import com.example.polarionprocessor.model.polarion.WorkItemCreateResult;
import com.example.polarionprocessor.service.shared.ModuleXmlExtractor;
import com.example.polarionprocessor.service.shared.ModuleXmlRewriter;
import com.example.polarionprocessor.service.shared.NumberedItemGrouper;
import com.example.polarionprocessor.service.shared.ParagraphCandidateSelector;
import com.example.polarionprocessor.service.shared.ParagraphScanner;
import com.example.polarionprocessor.service.shared.RuleBasedTitleGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolarionModuleImportServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void downloaderBuildsEncodedModuleUrl() {
        PolarionProperties properties = new PolarionProperties();
        properties.setBaseUrl("http://alm.freetech.com/");
        ModuleXmlDownloader downloader = new ModuleXmlDownloader(properties);

        String url = downloader.buildDownloadUrl("FDP_Demo", "10 Stakeholder Requirement", "R171e2");

        assertEquals("http://alm.freetech.com/polarion/svnwebclient/fileContent.jsp?url="
                + "FDP_Demo%2Fmodules%2F10%20Stakeholder%20Requirement%2FR171e2%2Fmodule.xml", url);
    }

    @Test
    void polarionImportDryRunProducesJsonAndCsv() throws Exception {
        RecordingCreator creator = new RecordingCreator();
        PolarionModuleImportService service = buildService(creator, new StaticDownloader(sampleXml()), new ModuleXmlRewriter());

        PolarionModuleImportRequest request = request(true);
        PolarionModuleImportResponse response = service.importModule(request);

        assertTrue(response.getSuccess(), response.getMessage());
        assertEquals(Boolean.TRUE, response.getDryRun());
        assertEquals(Integer.valueOf(0), response.getCreatedCount());
        assertEquals(0, creator.getCreateCount());
        Path jobDir = tempDir.resolve(response.getJobId());
        assertTrue(Files.exists(jobDir.resolve("original_module.xml")));
        assertTrue(Files.exists(jobDir.resolve("import_result.json")));
        assertTrue(Files.exists(jobDir.resolve("import_preview.csv")));
        assertFalse(Files.exists(jobDir.resolve("processed_module.xml")));
        assertEquals(null, response.getProcessedXmlFile());
    }

    @Test
    void polarionImportDoesNotGenerateMockIds() throws Exception {
        RecordingCreator creator = new RecordingCreator("FDP-7016", "FDP-7017");
        PolarionModuleImportService service = buildService(creator, new StaticDownloader(sampleXml()), new ModuleXmlRewriter());

        PolarionModuleImportResponse response = service.importModule(request(false));

        assertTrue(response.getSuccess(), response.getMessage());
        String resultJson = read(tempDir.resolve(response.getJobId()).resolve("import_result.json"));
        String previewCsv = read(tempDir.resolve(response.getJobId()).resolve("import_preview.csv"));
        assertFalse(resultJson.contains("MOCK-000001"));
        assertFalse(resultJson.contains("FDP-000001"));
        assertFalse(previewCsv.contains("MOCK-000001"));
        assertFalse(previewCsv.contains("FDP-000001"));
    }

    @Test
    void polarionImportCreatesWorkItemsSequentially() throws Exception {
        RecordingCreator creator = new RecordingCreator("FDP-7016", "FDP-7017");
        PolarionModuleImportService service = buildService(creator, new StaticDownloader(sampleXml()), new ModuleXmlRewriter());

        PolarionModuleImportResponse response = service.importModule(request(false));

        assertTrue(response.getSuccess(), response.getMessage());
        assertEquals(Integer.valueOf(2), response.getCreatedCount());
        assertEquals(Integer.valueOf(2), response.getReplacedCount());
        assertEquals(2, creator.getTitles().size());
        assertTrue(creator.getTitles().get(0).startsWith("5.1.1 First requirement"));
        assertTrue(creator.getTitles().get(1).startsWith("5.1.2 Second requirement"));
        String processedXml = read(tempDir.resolve(response.getJobId()).resolve("processed_module.xml"));
        assertTrue(processedXml.contains("params=id=FDP-7016"));
        assertTrue(processedXml.contains("params=id=FDP-7017"));
    }

    @Test
    void createFailureDoesNotStopWholeJob() throws Exception {
        RecordingCreator creator = new RecordingCreator(
                WorkItemCreateResult.failure("CREATE_FAILED_FOR_TEST", "first failed"),
                WorkItemCreateResult.success("FDP-7017"));
        PolarionModuleImportService service = buildService(creator, new StaticDownloader(sampleXml()), new ModuleXmlRewriter());

        PolarionModuleImportResponse response = service.importModule(request(false));

        assertTrue(response.getSuccess(), response.getMessage());
        assertEquals(Integer.valueOf(1), response.getCreatedCount());
        assertEquals(Integer.valueOf(1), response.getFailedCount());
        String resultJson = read(tempDir.resolve(response.getJobId()).resolve("import_result.json"));
        assertTrue(resultJson.contains("\"status\" : \"CREATE_FAILED\""));
        assertTrue(resultJson.contains("\"workItemId\" : \"FDP-7017\""));
    }

    @Test
    void workItemIdWrittenBeforeRewrite() throws Exception {
        RecordingCreator creator = new RecordingCreator("FDP-7016", "FDP-7017");
        ModuleXmlRewriter failingRewriter = new ModuleXmlRewriter() {
            @Override
            public String rewritePolarion(com.example.polarionprocessor.model.shared.ModuleXmlContent moduleXmlContent,
                                          List<PolarionImportItemResult> items) {
                for (PolarionImportItemResult item : items) {
                    if (ItemStatus.CREATED.name().equals(item.getStatus())) {
                        item.setStatus(ItemStatus.REPLACE_FAILED.name());
                        item.setErrorMessage("replace failed for test");
                    }
                }
                return moduleXmlContent.rebuild(moduleXmlContent.getHtmlContent());
            }
        };
        PolarionModuleImportService service = buildService(creator, new StaticDownloader(sampleXml()), failingRewriter);

        PolarionModuleImportResponse response = service.importModule(request(false));

        assertTrue(response.getSuccess(), response.getMessage());
        String resultJson = read(tempDir.resolve(response.getJobId()).resolve("import_result.json"));
        assertTrue(resultJson.contains("\"workItemId\" : \"FDP-7016\""));
        assertTrue(resultJson.contains("\"status\" : \"REPLACE_FAILED\""));
    }

    private PolarionModuleImportService buildService(PolarionWorkItemCreator creator,
                                                     ModuleXmlDownloader downloader,
                                                     ModuleXmlRewriter rewriter) {
        ModuleProcessorProperties moduleProperties = new ModuleProcessorProperties();
        moduleProperties.setOutputDir(tempDir.toString());
        PolarionProperties polarionProperties = new PolarionProperties();
        return new PolarionModuleImportService(
                moduleProperties,
                polarionProperties,
                downloader,
                new ModuleXmlExtractor(),
                new ParagraphScanner(),
                new NumberedItemGrouper(),
                new ParagraphCandidateSelector(),
                new RuleBasedTitleGenerator(moduleProperties),
                creator,
                new ModuleWorkItemMacroRenderer(),
                rewriter,
                new PolarionImportResultWriter(new ObjectMapper()),
                new PolarionImportPreviewCsvWriter(moduleProperties));
    }

    private PolarionModuleImportRequest request(boolean dryRun) {
        PolarionModuleImportRequest request = new PolarionModuleImportRequest();
        request.setProjectId("FDP_Demo");
        request.setModuleFolder("10 Stakeholder Requirement");
        request.setModuleName("R171e2");
        request.setWorkItemType("stakeholderRequirement");
        request.setDryRun(dryRun);
        request.setRequireKeyword(false);
        request.setDefaultFields(Collections.<String, Object>singletonMap("requirementsource", "Regulation"));
        return request;
    }

    private String sampleXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<module>\n"
                + "  <field id=\"title\">R171e2</field>\n"
                + "  <field id=\"homePageContent\" text-type=\"text/html\"><![CDATA[\n"
                + "    <p id=\"polarion_101\">5.1.1. First requirement shall be created for test coverage.</p>\n"
                + "    <p id=\"polarion_102\">5.1.2. Second requirement shall be created for test coverage.</p>\n"
                + "  ]]></field>\n"
                + "</module>";
    }

    private String read(Path file) throws Exception {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    private static class StaticDownloader extends ModuleXmlDownloader {
        private final String xml;

        StaticDownloader(String xml) {
            super(new PolarionProperties());
            this.xml = xml;
        }

        @Override
        public String download(String projectId, String moduleFolder, String moduleName) {
            return xml;
        }
    }

    private static class RecordingCreator implements PolarionWorkItemCreator {
        private final List<WorkItemCreateResult> results;
        private final java.util.ArrayList<String> titles = new java.util.ArrayList<String>();

        RecordingCreator() {
            this.results = Collections.emptyList();
        }

        RecordingCreator(String... workItemIds) {
            java.util.ArrayList<WorkItemCreateResult> list = new java.util.ArrayList<WorkItemCreateResult>();
            for (String workItemId : workItemIds) {
                list.add(WorkItemCreateResult.success(workItemId));
            }
            this.results = list;
        }

        RecordingCreator(WorkItemCreateResult... results) {
            this.results = Arrays.asList(results);
        }

        @Override
        public WorkItemCreateResult createOne(WorkItemCreateRequest request) {
            titles.add(request.getTitle());
            if (titles.size() <= results.size()) {
                return results.get(titles.size() - 1);
            }
            return WorkItemCreateResult.failure("NO_TEST_RESULT", "No test result configured");
        }

        int getCreateCount() {
            return titles.size();
        }

        List<String> getTitles() {
            return titles;
        }
    }
}
