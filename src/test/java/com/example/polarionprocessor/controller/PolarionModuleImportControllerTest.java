package com.example.polarionprocessor.controller;

import com.example.polarionprocessor.model.polarion.PolarionModuleImportRequest;
import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.service.polarion.PolarionModuleImportAsyncExecutor;
import com.example.polarionprocessor.service.polarion.PolarionModuleUrlParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PolarionModuleImportControllerTest {

    private static final String MODULE_URL =
            "http://alm.freetech.com/polarion/#/project/FDP_Demo/wiki/10%20Stakeholder%20Requirement/R171e4";

    @TempDir
    Path tempDir;

    @Test
    void jsonRequestShouldAcceptFrontendUrlAndUsernameAliases() throws Exception {
        PolarionModuleImportAsyncExecutor executor = mock(PolarionModuleImportAsyncExecutor.class);
        MockMvc mockMvc = mockMvc(executor);

        mockMvc.perform(post("/api/polarion/module/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"" + MODULE_URL + "\",\"username\":\"custom.author\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.jobId").exists())
                .andExpect(jsonPath("$.progressLogFile").value("progress.log"))
                .andExpect(jsonPath("$.projectId").value("FDP_Demo"))
                .andExpect(jsonPath("$.moduleName").value("R171e4"));

        ArgumentCaptor<PolarionModuleImportRequest> captor =
                ArgumentCaptor.forClass(PolarionModuleImportRequest.class);
        verify(executor).submit(captor.capture());
        assertEquals(MODULE_URL, captor.getValue().getModuleUrl());
        assertEquals("custom.author", captor.getValue().getAuthorId());
        assertEquals("subterra:data-service:objects:/default/FDP_Demo${Module}{moduleFolder}10 Stakeholder Requirement#R171e4",
                captor.getValue().getModuleURI());
    }

    @Test
    void formRequestShouldAcceptFrontendUrlAndUsernameFields() throws Exception {
        PolarionModuleImportAsyncExecutor executor = mock(PolarionModuleImportAsyncExecutor.class);
        MockMvc mockMvc = mockMvc(executor);
        String body = "url=" + URLEncoder.encode(MODULE_URL, "UTF-8")
                + "&username=" + URLEncoder.encode("custom.author", "UTF-8");

        mockMvc.perform(post("/api/polarion/module/import")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.projectId").value("FDP_Demo"))
                .andExpect(jsonPath("$.moduleName").value("R171e4"));

        ArgumentCaptor<PolarionModuleImportRequest> captor =
                ArgumentCaptor.forClass(PolarionModuleImportRequest.class);
        verify(executor).submit(captor.capture());
        assertEquals(MODULE_URL, captor.getValue().getModuleUrl());
        assertEquals("custom.author", captor.getValue().getAuthorId());
        assertEquals("subterra:data-service:objects:/default/FDP_Demo${Module}{moduleFolder}10 Stakeholder Requirement#R171e4",
                captor.getValue().getModuleURI());
    }

    @Test
    void explicitModuleUriShouldOverrideParsedModuleUri() throws Exception {
        PolarionModuleImportAsyncExecutor executor = mock(PolarionModuleImportAsyncExecutor.class);
        MockMvc mockMvc = mockMvc(executor);
        String explicitModuleURI = "subterra:data-service:objects:/default/Override${Module}{moduleFolder}Folder#Doc";

        mockMvc.perform(post("/api/polarion/module/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"" + MODULE_URL + "\",\"moduleURI\":\"" + explicitModuleURI + "\"}"))
                .andExpect(status().isAccepted());

        ArgumentCaptor<PolarionModuleImportRequest> captor =
                ArgumentCaptor.forClass(PolarionModuleImportRequest.class);
        verify(executor).submit(captor.capture());
        assertEquals(explicitModuleURI, captor.getValue().getModuleURI());
    }

    @Test
    void progressLogEndpointShouldReadJobProgressFile() throws Exception {
        PolarionModuleImportAsyncExecutor executor = mock(PolarionModuleImportAsyncExecutor.class);
        MockMvc mockMvc = mockMvc(executor);
        Path jobDir = tempDir.resolve("job-001");
        Files.createDirectories(jobDir);
        Files.write(jobDir.resolve("progress.log"),
                "任务已启动\n正在创建 Work Item\n".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/polarion/module/import/job-001/progress-log"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("正在创建 Work Item")));
    }

    private MockMvc mockMvc(PolarionModuleImportAsyncExecutor executor) {
        ModuleProcessorProperties moduleProperties = new ModuleProcessorProperties();
        moduleProperties.setOutputDir(tempDir.toString());
        return MockMvcBuilders.standaloneSetup(new PolarionModuleImportController(
                new ObjectMapper(),
                moduleProperties,
                new PolarionModuleUrlParser(),
                executor)).build();
    }
}
