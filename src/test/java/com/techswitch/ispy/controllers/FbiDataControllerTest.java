package com.techswitch.ispy.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techswitch.ispy.config.IntegrationTestConfig;
import com.techswitch.ispy.models.request.SuspectFbiRequestModel;
import com.techswitch.ispy.services.FbiDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = IntegrationTestConfig.class)
@ActiveProfiles({"testDataSource", "testSigner", "testLoginConfig"})
public class FbiDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FbiDataService fbiDataService;

    @Test
    public void fetchFbiData_givenAValidJsonAndToken_thenReturnStatusOk() throws Exception {
        List<SuspectFbiRequestModel> suspects = new ArrayList<>();
        File from = new File("src/test/java/com/techswitch/ispy/services/data/SuspectJson.json");
        SuspectFbiRequestModel suspect = objectMapper.readValue(from, SuspectFbiRequestModel.class);
        suspects.add(suspect);

        when(fbiDataService.getSuspectsFromFbiApi()).thenReturn(suspects);

        String token = createValidToken();

        mockMvc.perform(get("http://localhost:8080/admin/fetch-fbi-data")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", token))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.Success").value("1 row(s) has been added to database."));
    }

    @Test
    public void fetchFbiData_givenAInalidToken_thenReturnStatusBADREQUEST() throws Exception {
        mockMvc.perform(get("http://localhost:8080/admin/fetch-fbi-data")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", "invalid token"))
                .andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    public void fetchFbiData_givenANoToken_thenReturnStatusBADREQUEST() throws Exception {
        mockMvc.perform(get("http://localhost:8080/admin/fetch-fbi-data"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void fetchFbiData_givenValidJson_thenReturnStatusOkNoneAdded() throws Exception {
        when(fbiDataService.getSuspectsFromFbiApi()).thenReturn(new ArrayList<>());

        String token = createValidToken();

        mockMvc.perform(get("http://localhost:8080/admin/fetch-fbi-data")
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", token))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.Success").value("No data has been added. Database Up to date."));
    }

    private String createValidToken() throws JsonProcessingException {
        Algorithm algorithm = Algorithm.HMAC256("signerstring");
        return JWT.create()
                .withIssuer("techswitch-ispy")
                .sign(algorithm);
    }
}
