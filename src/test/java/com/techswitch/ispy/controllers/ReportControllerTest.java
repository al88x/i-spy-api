package com.techswitch.ispy.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techswitch.ispy.config.AppConfig;
import com.techswitch.ispy.config.IntegrationTestConfig;
import com.techswitch.ispy.models.Report;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = IntegrationTestConfig.class)
@ActiveProfiles("testDataSource")
public class ReportControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void givenValidRequest_thenStatusIsCreated() throws Exception {
        Report report = new Report(1L, "12-02-2019", "London", "description text");
        String requestJson = objectMapper.writeValueAsString(report);

        mockMvc.perform(post("http://localhost:8080/reports/create")
                .contentType(APPLICATION_JSON)
                .content(requestJson))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.suspectId").value("1"))
                .andExpect(jsonPath("$.dateOfSighting").value("2019-02-12"))
                .andExpect(jsonPath("$.location").value("London"))
                .andExpect(jsonPath("$.description").value("description text"))
                .andExpect(jsonPath("$.timestampSubmitted").exists())
                .andReturn();
    }

    @Test
    public void givenRequestWithEmptyDescription_thenStatusBadRequest() throws Exception {
        Report report = new Report(1L, "12-02-2019", "London", "");
        String requestJson = objectMapper.writeValueAsString(report);

        mockMvc.perform(post("http://localhost:8080/reports/create")
                .contentType(APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Description cannot be empty"));
    }

    @Test
    public void givenRequestWithWrongDateFormat_thenStatusBadRequest() throws Exception {
        Report report = new Report(1L, "12022019", "London", "description");
        String requestJson = objectMapper.writeValueAsString(report);

        mockMvc.perform(post("http://localhost:8080/reports/create")
                .contentType(APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.date").value("Please use a DD-MM-YYYY format for date"));
    }

    @Test
    public void givenRequestWithEmptyDate_thenStatusBadRequest() throws Exception {
        Report report = new Report(1L, "", "London", "description");
        String requestJson = objectMapper.writeValueAsString(report);

        mockMvc.perform(post("http://localhost:8080/reports/create")
                .contentType(APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.date").value("Please use a DD-MM-YYYY format for date"));
    }
}
