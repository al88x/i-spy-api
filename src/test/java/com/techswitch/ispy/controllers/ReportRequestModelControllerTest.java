package com.techswitch.ispy.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techswitch.ispy.config.IntegrationTestConfig;
import com.techswitch.ispy.models.Token;
import com.techswitch.ispy.models.request.ReportRequestModel;
import com.techswitch.ispy.services.ReportService;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = IntegrationTestConfig.class)
@ActiveProfiles({"testDataSource", "testSigner", "testLoginConfig"})
public class ReportRequestModelControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Jdbi jdbi;

    @Test
    public void validRequest_createReport_thenStatusIsCreated() throws Exception {
        ReportRequestModel reportRequestModel = new ReportRequestModel(1L, "2018-04-13", "London", "description text");
        String requestJson = objectMapper.writeValueAsString(reportRequestModel);

        mockMvc.perform(post("http://localhost:8080/reports/create")
                .contentType(APPLICATION_JSON)
                .content(requestJson))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.suspectId").value("1"))
                .andExpect(jsonPath("$.dateOfSighting").value("2018-04-13"))
                .andExpect(jsonPath("$.location").value("London"))
                .andExpect(jsonPath("$.description").value("description text"))
                .andExpect(jsonPath("$.timestampSubmitted").exists())
                .andReturn();
    }

    @Test
    public void invalidRequest_createReport_returnsValidationError() throws Exception {
        ReportRequestModel reportRequestModel = new ReportRequestModel(1L, "2018-04-13", "London", "");
        String requestJson = objectMapper.writeValueAsString(reportRequestModel);

        mockMvc.perform(post("http://localhost:8080/reports/create")
                .contentType(APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Description cannot be empty"));
    }

    @Test
    public void givenRequestWithInvalidJson_theStatusBadRequest() throws Exception {
        String requestJson = "{\"suspectId\":}";
        mockMvc.perform(post("http://localhost:8080/reports/create")
                .contentType(APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.Error").value("Bad Request"));
    }


    @Test
    public void givenValidToken_thenReturnListOfReports() throws Exception {
        addFakeData(5);

        Algorithm algorithm = Algorithm.HMAC256("signerstring");
        String token = (JWT.create()
                .withIssuer("techswitch-ispy")
                .sign(algorithm));

        String requestJson = objectMapper.writeValueAsString(token);

        mockMvc.perform(get("http://localhost:8080/reports")
                .contentType(APPLICATION_JSON)
                .header("token",token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5)
                );
    }

    @Test
    public void givenInvalidToken_thenReturnBADREQUEST() throws Exception {

        mockMvc.perform(get("http://localhost:8080/reports")
                .contentType(APPLICATION_JSON)
                .header("token","invalid token"))
                .andExpect(status().isBadRequest()
                );
    }
    @Test
    public void givenNoToken_thenReturnBADREQUEST() throws Exception {

        mockMvc.perform(get("http://localhost:8080/reports"))
                .andExpect(status().isBadRequest()
                );
    }

    private void addFakeData(int numberOfEntries) {
        for (int i = 0; i < numberOfEntries; i++) {
            jdbi.withHandle(handle ->
                    handle.createUpdate("INSERT INTO reports (suspect_id, date_of_sighting, location, description, timestamp_submitted) " +
                            "VALUES (1, '11/02/1994'::date, 'Harlow' , 'he has a giant left hand', now()::timestamp) "
                    ).execute());
        }
    }
}