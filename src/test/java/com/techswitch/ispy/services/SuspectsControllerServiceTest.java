package com.techswitch.ispy.services;

import com.techswitch.ispy.config.IntegrationTestConfig;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("IntegrationTestConfig")
@ContextConfiguration(classes = IntegrationTestConfig.class)
class SuspectsControllerServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Jdbi jdbi;

    @BeforeEach
    void init() {
        jdbi.withHandle(handle -> handle.execute("CREATE TABLE all_suspects (\n" +
                " \tid serial  primary key, \n" +
                " \tname varchar(100),\n" +
                " \timage_url varchar(500)\n" +
                " );"));
        jdbi.withHandle(handle -> handle.execute("INSERT INTO all_suspects(name,image_url) values('Harry Potter','https://www.fbi.gov/wanted/additional/cesar-munguia/@@images/image/thumb');"));
    }

    @AfterEach
    void dropTable() {
        jdbi.withHandle(handle -> handle.execute("DROP TABLE all_suspects;"));
    }

    @Test
    void getListOfSuspects() throws Exception {

        mockMvc.perform(get("htttp://localhost/allsuspects/page=1&page_size=10"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("[0].name").value("Harry Potter"))
                .andExpect(jsonPath("[0].imageUrl").value("https://www.fbi.gov/wanted/additional/cesar-munguia/@@images/image/thumb"))
                .andReturn();
    }

    @Test
    public void suspectController() throws Exception {
        mockMvc.perform(get("http://localhost/suspect"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("BaddieMcBad"))
                .andReturn();
    }

}