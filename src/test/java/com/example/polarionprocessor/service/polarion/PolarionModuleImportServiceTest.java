package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.config.PolarionProperties;
import com.example.polarionprocessor.enums.ItemStatus;
import com.example.polarionprocessor.model.polarion.PolarionCustomFieldRequest;
import com.example.polarionprocessor.model.polarion.PolarionEnumOptionRequest;
import com.example.polarionprocessor.model.polarion.PolarionImportItemResult;
import com.example.polarionprocessor.model.polarion.PolarionModuleImportRequest;
import com.example.polarionprocessor.model.polarion.PolarionModuleImportResponse;
import com.example.polarionprocessor.model.polarion.SvnCommitResult;
import com.example.polarionprocessor.model.polarion.WorkItemCreateApiRequest;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PolarionModuleImportServiceTest {

    private static final String CREATE_URL = "http://10.179.60.154:30000/workitem/ws/create";

    @TempDir
    Path tempDir;

    @Test
    void moduleUrlParserExtractsLocationAndDownloaderBuildsSvnUrl() {
        PolarionProperties properties = new PolarionProperties();
        properties.setBaseUrl("http://alm.freetech.com/");
        ModuleXmlDownloader downloader = new ModuleXmlDownloader(properties);
        PolarionModuleUrlParser parser = new PolarionModuleUrlParser();

        com.example.polarionprocessor.model.polarion.PolarionModuleLocation location = parser.parse(
                "http://alm.freetech.com/polarion/#/project/FDP_Demo/wiki/10%20Stakeholder%20Requirement/R171e");
        String url = downloader.buildSvnModuleUrl(
                location.getBaseUrl(),
                location.getProjectId(),
                location.getModuleFolder(),
                location.getModuleName());

        assertEquals("http://alm.freetech.com", location.getBaseUrl());
        assertEquals("FDP_Demo", location.getProjectId());
        assertEquals("10 Stakeholder Requirement", location.getModuleFolder());
        assertEquals("R171e", location.getModuleName());
        assertEquals("http://alm.freetech.com/repo/FDP_Demo/modules/10 Stakeholder Requirement/R171e/", url);
    }

    @Test
    void buildApiRequestMatchesDifyPayload() {
        PolarionProperties properties = apiProperties();
        WorkItemCreateApiRequestBuilder builder = new WorkItemCreateApiRequestBuilder(properties);
        WorkItemCreateRequest request = createRequest();
        request.setAuthorName("尹何营");
        request.setAssigneeIds(Arrays.asList("reviewer.one", "reviewer.two"));
        request.setDueDate("2026-06-30");
        Map<String, Object> fields = new LinkedHashMap<String, Object>();
        fields.put("status", "draft");
        fields.put("verificationcriteria", "测试验证：通过模拟不同驾驶场景确认能力。");
        Map<String, Object> reqType = new LinkedHashMap<String, Object>();
        reqType.put("id", "reqType");
        reqType.put("multi", false);
        reqType.put("name", "Requirement Type");
        reqType.put("required", true);
        reqType.put("type", "EnumOptionId");
        reqType.put("enumOptions", Arrays.asList(
                Collections.<String, Object>singletonMap("id", "functional")));
        reqType.put("value", "functional");
        fields.put("reqType", reqType);
        request.setFields(fields);

        WorkItemCreateApiRequest apiRequest = builder.build(request);

        assertEquals("draft", apiRequest.getStatus());
        assertEquals("FDP_Demo", apiRequest.getPolarionId());
        assertEquals("stakeholderrequirement", apiRequest.getType());
        assertEquals("11 DCAS 应具备评估驾驶员持续参与能力", apiRequest.getTitle());
        assertEquals("尹何营", apiRequest.getAuthorName());
        assertEquals("yiming.yuan", apiRequest.getAuthorId());
        assertEquals(Arrays.asList("reviewer.one", "reviewer.two"), apiRequest.getAssigneeIds());
        assertEquals("2026-06-30", apiRequest.getDueDate());
        assertEquals(Boolean.FALSE, apiRequest.getIsNewPdp());
        assertEquals(Boolean.TRUE, apiRequest.getOnlyCreate());
        assertEquals("【条款 11】原文：DCAS shall have means to evaluate continuous driver involvement.", apiRequest.getCdescription());
        assertEquals(3, apiRequest.getCustomFields().size());
        assertEquals("requirementsource", apiRequest.getCustomFields().get(0).getId());
        assertEquals("EnumOptionId", apiRequest.getCustomFields().get(0).getType());
        assertEquals("Regulation", apiRequest.getCustomFields().get(0).getValue());
        assertEquals("verificationcriteria", apiRequest.getCustomFields().get(1).getId());
        assertEquals("text/html", apiRequest.getCustomFields().get(1).getType());
        assertEquals("reqType", apiRequest.getCustomFields().get(2).getId());
        assertEquals("Requirement Type", apiRequest.getCustomFields().get(2).getName());
        assertEquals(Boolean.TRUE, apiRequest.getCustomFields().get(2).getRequired());
        assertEquals("functional", apiRequest.getCustomFields().get(2).getValue());
        assertEquals("functional", apiRequest.getCustomFields().get(2).getEnumOptions().get(0).getId());
    }

    @Test
    void projectCustomFieldsOnlyApplyToMatchingProject() {
        PolarionProperties properties = new PolarionProperties();
        PolarionProperties.WorkItemApi api = properties.getWorkItemApi();
        api.setDefaultCustomFields(Collections.<PolarionCustomFieldRequest>emptyList());
        List<PolarionCustomFieldRequest> rmtFields = new ArrayList<PolarionCustomFieldRequest>();
        PolarionCustomFieldRequest reqType = new PolarionCustomFieldRequest("reqType", Boolean.FALSE, "EnumOptionId", "functional");
        reqType.setEnumOptions(Arrays.asList(new PolarionEnumOptionRequest("functional", "Functional")));
        rmtFields.add(reqType);
        Map<String, List<PolarionCustomFieldRequest>> projectCustomFields =
                new LinkedHashMap<String, List<PolarionCustomFieldRequest>>();
        projectCustomFields.put("RMT_Platform", rmtFields);
        api.setProjectCustomFields(projectCustomFields);
        WorkItemCreateApiRequestBuilder builder = new WorkItemCreateApiRequestBuilder(properties);

        WorkItemCreateRequest rmtRequest = createRequest();
        rmtRequest.setProjectId("RMT_Platform");
        WorkItemCreateRequest fdpRequest = createRequest();
        fdpRequest.setProjectId("FDP_Demo");

        assertEquals(1, builder.build(rmtRequest).getCustomFields().size());
        assertEquals("reqType", builder.build(rmtRequest).getCustomFields().get(0).getId());
        assertTrue(builder.build(fdpRequest).getCustomFields().isEmpty());
    }

    @Test
    void projectCustomFieldTemplatesProvideEnumOptionsAndMergeAiValues() {
        PolarionProperties properties = new PolarionProperties();
        PolarionProperties.WorkItemApi api = properties.getWorkItemApi();
        api.setDefaultCustomFields(Collections.<PolarionCustomFieldRequest>emptyList());

        List<PolarionCustomFieldRequest> rmtFields = new ArrayList<PolarionCustomFieldRequest>();
        PolarionCustomFieldRequest requirementSource =
                new PolarionCustomFieldRequest("requirementsouce", Boolean.FALSE, "EnumOptionId", null);
        requirementSource.setEnumOptions(Arrays.asList(
                new PolarionEnumOptionRequest("internal", "Internal内部的需求"),
                new PolarionEnumOptionRequest("external", "External来自外部的需求"),
                new PolarionEnumOptionRequest("Regulation", "Regulation法规需求")));
        rmtFields.add(requirementSource);
        rmtFields.add(new PolarionCustomFieldRequest("verificationcriteria", Boolean.FALSE, "text/html", null));
        PolarionCustomFieldRequest reqType =
                new PolarionCustomFieldRequest("reqType", Boolean.FALSE, "EnumOptionId", null);
        reqType.setEnumOptions(Arrays.asList(
                new PolarionEnumOptionRequest("functional", "Functional Requirement 功能需求"),
                new PolarionEnumOptionRequest("nofunctional", "Non-Functional Requirement 非功能需求(性能等)"),
                new PolarionEnumOptionRequest("interface", "Interface Requirement 接口需求"),
                new PolarionEnumOptionRequest("constraint", "Constraint 约束条件"),
                new PolarionEnumOptionRequest("information", "Information 信息类需求")));
        rmtFields.add(reqType);
        Map<String, List<PolarionCustomFieldRequest>> projectCustomFields =
                new LinkedHashMap<String, List<PolarionCustomFieldRequest>>();
        projectCustomFields.put("RMT_Platform", rmtFields);
        api.setProjectCustomFields(projectCustomFields);

        WorkItemCreateApiRequestBuilder builder = new WorkItemCreateApiRequestBuilder(properties);
        WorkItemCreateRequest request = createRequest();
        request.setProjectId("RMT_Platform");
        assertTrue(builder.build(request).getCustomFields().isEmpty());

        Map<String, Object> aiFields = new LinkedHashMap<String, Object>();
        aiFields.put("requirementsouce", "Regulation");
        aiFields.put("reqType", "constraint");
        aiFields.put("verificationcriteria", "AI 生成的验证准则");
        request.setFields(aiFields);

        List<PolarionCustomFieldRequest> customFields = builder.build(request).getCustomFields();

        assertEquals(3, customFields.size());
        assertEquals("requirementsource", customFields.get(0).getId());
        assertEquals("EnumOptionId", customFields.get(0).getType());
        assertEquals("Regulation", customFields.get(0).getValue());
        assertEquals(3, customFields.get(0).getEnumOptions().size());
        assertEquals("verificationcriteria", customFields.get(1).getId());
        assertEquals("text/html", customFields.get(1).getType());
        assertEquals("AI 生成的验证准则", customFields.get(1).getValue());
        assertEquals("reqType", customFields.get(2).getId());
        assertEquals("constraint", customFields.get(2).getValue());
        assertEquals(5, customFields.get(2).getEnumOptions().size());
    }

    @Test
    void createOneSuccessReturnsDataAsWorkItemId() {
        PolarionProperties properties = apiProperties();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        HttpPolarionWorkItemCreator creator = new HttpPolarionWorkItemCreator(
                properties,
                restTemplate,
                new WorkItemCreateApiRequestBuilder(properties));
        server.expect(requestTo(CREATE_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.polarionId").value("FDP_Demo"))
                .andExpect(jsonPath("$.type").value("stakeholderrequirement"))
                .andExpect(jsonPath("$.title").value("11 DCAS 应具备评估驾驶员持续参与能力"))
                .andExpect(jsonPath("$.authorId").value("yiming.yuan"))
                .andExpect(jsonPath("$.isNewPdp").value(false))
                .andExpect(jsonPath("$.onlyCreate").value(true))
                .andExpect(jsonPath("$.cdescription").value("【条款 11】原文：DCAS shall have means to evaluate continuous driver involvement."))
                .andExpect(jsonPath("$.description").doesNotExist())
                .andRespond(withSuccess(
                        "{\"code\":\"0\",\"data\":\"FDP-7018\",\"extension\":{},\"success\":true,\"msg\":\"请求成功\"}",
                        MediaType.APPLICATION_JSON));

        WorkItemCreateResult result = creator.createOne(createRequest());

        assertTrue(result.getSuccess());
        assertEquals("FDP-7018", result.getWorkItemId());
        server.verify();
    }

    @Test
    void createOneFailureDoesNotGenerateFakeId() {
        PolarionProperties properties = apiProperties();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        HttpPolarionWorkItemCreator creator = new HttpPolarionWorkItemCreator(
                properties,
                restTemplate,
                new WorkItemCreateApiRequestBuilder(properties));
        server.expect(requestTo(CREATE_URL))
                .andRespond(withSuccess(
                        "{\"code\":\"CREATE_FAILED\",\"data\":null,\"success\":false,\"msg\":\"创建失败\"}",
                        MediaType.APPLICATION_JSON));

        WorkItemCreateResult result = creator.createOne(createRequest());

        assertFalse(result.getSuccess());
        assertNull(result.getWorkItemId());
        assertEquals("CREATE_FAILED", result.getErrorCode());
        assertTrue(result.getErrorMessage().contains("创建失败"));
        server.verify();
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
        assertFalse(Files.exists(jobDir.resolve("module.xml")));
        assertEquals(null, response.getProcessedXmlFile());
    }

    @Test
    void processedXmlFileNameShouldBeModuleXml() throws Exception {
        RecordingCreator creator = new RecordingCreator("FDP-7016", "FDP-7017");
        PolarionModuleImportService service = buildService(creator, new StaticDownloader(sampleXml()), new ModuleXmlRewriter());

        PolarionModuleImportResponse response = service.importModule(request(false));

        Path jobDir = tempDir.resolve(response.getJobId());
        assertTrue(response.getSuccess(), response.getMessage());
        assertEquals("module.xml", response.getProcessedXmlFile());
        assertTrue(Files.exists(jobDir.resolve("module.xml")));
        assertFalse(Files.exists(jobDir.resolve("processed_module.xml")));
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
        String processedXml = read(tempDir.resolve(response.getJobId()).resolve("module.xml"));
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

    @Test
    void importServiceShouldCallCommitterAfterRewrite() throws Exception {
        RecordingCreator creator = new RecordingCreator("FDP-7016", "FDP-7017");
        RecordingCommitter committer = new RecordingCommitter(SvnCommitResult.committed("123456"));
        PolarionModuleImportService service = buildService(
                creator,
                new StaticDownloader(sampleXml()),
                new ModuleXmlRewriter(),
                committer);

        PolarionModuleImportResponse response = service.importModule(request(false));

        assertTrue(response.getSuccess(), response.getMessage());
        assertEquals(1, committer.getCallCount());
        assertEquals("module.xml", committer.getProcessedModuleXml().getFileName().toString());
        assertTrue(Files.exists(committer.getProcessedModuleXml()));
        assertEquals(SvnCommitResult.STATUS_COMMITTED, response.getSvnCommitStatus());
        assertEquals("123456", response.getSvnRevision());
    }

    @Test
    void anthorNameShouldOverrideDefaultAuthorIdWhenCreatingWorkItem() throws Exception {
        RecordingCreator creator = new RecordingCreator("FDP-7016", "FDP-7017");
        PolarionModuleImportService service = buildService(creator, new StaticDownloader(sampleXml()), new ModuleXmlRewriter());
        String requestJson = "{"
                + "\"moduleUrl\":\"http://alm.freetech.com/polarion/#/project/FDP_Demo/wiki/10%20Stakeholder%20Requirement/R171e2\","
                + "\"dryRun\":false,"
                + "\"requireKeyword\":false,"
                + "\"anthorName\":\"custom.author\""
                + "}";
        PolarionModuleImportRequest request = new ObjectMapper().readValue(requestJson, PolarionModuleImportRequest.class);

        PolarionModuleImportResponse response = service.importModule(request);

        assertTrue(response.getSuccess(), response.getMessage());
        assertEquals("custom.author", request.getAuthorId());
        assertEquals("custom.author", creator.getAuthorIds().get(0));
        String resultJson = read(tempDir.resolve(response.getJobId()).resolve("import_result.json"));
        assertTrue(resultJson.contains("\"authorId\" : \"custom.author\""));
    }

    private PolarionProperties apiProperties() {
        PolarionProperties properties = new PolarionProperties();
        PolarionProperties.WorkItemApi api = properties.getWorkItemApi();
        api.setEnabled(true);
        api.setCreateUrl(CREATE_URL);
        api.setDefaultPolarionId("FDP_Demo");
        api.setDefaultType("stakeholderrequirement");
        api.setDefaultAuthorId("yiming.yuan");
        List<PolarionCustomFieldRequest> defaultCustomFields = new ArrayList<PolarionCustomFieldRequest>();
        defaultCustomFields.add(new PolarionCustomFieldRequest("requirementsource", Boolean.FALSE, "EnumOptionId", "Regulation"));
        api.setDefaultCustomFields(defaultCustomFields);
        return properties;
    }

    private WorkItemCreateRequest createRequest() {
        WorkItemCreateRequest request = new WorkItemCreateRequest();
        request.setProjectId("FDP_Demo");
        request.setType("stakeholderrequirement");
        request.setTitle("11 DCAS 应具备评估驾驶员持续参与能力");
        request.setAuthorId("yiming.yuan");
        request.setDescription("【条款 11】原文：DCAS shall have means to evaluate continuous driver involvement.");
        return request;
    }

    private PolarionModuleImportService buildService(PolarionWorkItemCreator creator,
                                                     ModuleXmlDownloader downloader,
                                                     ModuleXmlRewriter rewriter) {
        return buildService(creator, downloader, rewriter, new RecordingCommitter(SvnCommitResult.noChange()));
    }

    private PolarionModuleImportService buildService(PolarionWorkItemCreator creator,
                                                     ModuleXmlDownloader downloader,
                                                     ModuleXmlRewriter rewriter,
                                                     SvnModuleCommitter committer) {
        ModuleProcessorProperties moduleProperties = new ModuleProcessorProperties();
        moduleProperties.setOutputDir(tempDir.toString());
        PolarionProperties polarionProperties = new PolarionProperties();
        return new PolarionModuleImportService(
                moduleProperties,
                polarionProperties,
                new PolarionModuleUrlParser(),
                downloader,
                new ModuleXmlExtractor(),
                new ParagraphScanner(),
                new NumberedItemGrouper(),
                new ParagraphCandidateSelector(),
                new RuleBasedTitleGenerator(moduleProperties),
                creator,
                new WorkItemCreateApiRequestBuilder(polarionProperties),
                new ModuleWorkItemMacroRenderer(),
                rewriter,
                committer,
                new PolarionImportResultWriter(new ObjectMapper()),
                new PolarionImportPreviewCsvWriter(moduleProperties));
    }

    private PolarionModuleImportRequest request(boolean dryRun) {
        PolarionModuleImportRequest request = new PolarionModuleImportRequest();
        request.setModuleUrl("http://alm.freetech.com/polarion/#/project/FDP_Demo/wiki/10%20Stakeholder%20Requirement/R171e2");
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
        public String download(String baseUrl, String projectId, String moduleFolder, String moduleName) {
            assertEquals("http://alm.freetech.com", baseUrl);
            assertEquals("FDP_Demo", projectId);
            assertEquals("10 Stakeholder Requirement", moduleFolder);
            assertEquals("R171e2", moduleName);
            return xml;
        }
    }

    private static class RecordingCreator implements PolarionWorkItemCreator {
        private final List<WorkItemCreateResult> results;
        private final java.util.ArrayList<String> titles = new java.util.ArrayList<String>();
        private final java.util.ArrayList<String> authorIds = new java.util.ArrayList<String>();

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
            authorIds.add(request.getAuthorId());
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

        List<String> getAuthorIds() {
            return authorIds;
        }
    }

    private static class RecordingCommitter extends SvnModuleCommitter {

        private final SvnCommitResult result;
        private int callCount;
        private Path processedModuleXml;

        RecordingCommitter(SvnCommitResult result) {
            super(new PolarionProperties(), new ModuleXmlDownloader(new PolarionProperties()), new SvnCommandExecutor());
            this.result = result;
        }

        @Override
        public SvnCommitResult commit(String jobId,
                                      Path outputDir,
                                      String baseUrl,
                                      String projectId,
                                      String moduleFolder,
                                      String moduleName,
                                      Path processedModuleXml,
                                      String commitMessage) {
            this.callCount++;
            this.processedModuleXml = processedModuleXml;
            return result;
        }

        int getCallCount() {
            return callCount;
        }

        Path getProcessedModuleXml() {
            return processedModuleXml;
        }
    }
}
