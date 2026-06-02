package com.example.polarionprocessor.controller;

import com.example.polarionprocessor.model.polarion.PolarionModuleImportRequest;
import com.example.polarionprocessor.service.polarion.PolarionModuleImportAsyncExecutor;
import com.example.polarionprocessor.service.polarion.PolarionModuleUrlParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PolarionModuleImportControllerTest {

    private static final String MODULE_URL =
            "http://alm.freetech.com/polarion/#/project/FDP_Demo/wiki/10%20Stakeholder%20Requirement/R171e4";

    @Test
    void jsonRequestShouldAcceptFrontendUrlAndUsernameAliases() throws Exception {
        PolarionModuleImportAsyncExecutor executor = mock(PolarionModuleImportAsyncExecutor.class);
        MockMvc mockMvc = mockMvc(executor);

        mockMvc.perform(post("/api/polarion/module/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"" + MODULE_URL + "\",\"username\":\"custom.author\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.projectId").value("FDP_Demo"))
                .andExpect(jsonPath("$.moduleName").value("R171e4"));

        ArgumentCaptor<PolarionModuleImportRequest> captor =
                ArgumentCaptor.forClass(PolarionModuleImportRequest.class);
        verify(executor).submit(captor.capture());
        assertEquals(MODULE_URL, captor.getValue().getModuleUrl());
        assertEquals("custom.author", captor.getValue().getAuthorId());
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
    }

    private MockMvc mockMvc(PolarionModuleImportAsyncExecutor executor) {
        return MockMvcBuilders.standaloneSetup(new PolarionModuleImportController(
                new ObjectMapper(),
                new PolarionModuleUrlParser(),
                executor)).build();
    }
}
