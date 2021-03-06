package com.techswitch.ispy.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techswitch.ispy.config.IntegrationTestConfig;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"testDataSource","testSigner","testLoginConfig"})
@ContextConfiguration(classes = IntegrationTestConfig.class)
public class GetJsonFromUrlTest {

    @Mock
    RestTemplate restTemplate;
    @InjectMocks
    @Spy
    FbiDataService fbiDataService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void getJsonFromUrl_givenValidUrl_thenReturnJsonNode() throws IOException {

        String mockResponseBody = objectMapper.readTree(new File("src/test/java/com/techswitch/ispy/services/data/SuspectJsonWithRoot.json")).toString();
        ResponseEntity<String> responseEntity = ResponseEntity.ok().body(mockResponseBody);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

        when(restTemplate.exchange("https://fake.com",HttpMethod.GET,entity, String.class)).thenReturn(responseEntity);
        JsonNode response = fbiDataService.getJsonFromUrl("https://fake.com", "items");

        assertThat(response.get(0).get("hair_raw").textValue()).isEqualTo("Black");
        assertThat(response.get(0).get("warning_message").textValue()).isEqualTo("SHOULD BE CONSIDERED ARMED AND DANGEROUS");
    }
}

