package com.nimish.pingslayer.endpoint;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// Ensure these static imports are present:
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class EndpointControllerTest {
  @Autowired
  MockMvc mvc;

  @Test
  void create_validPayload_returns201() throws Exception {
    mvc.perform(post("/api/endpoints").contentType(MediaType.APPLICATION_JSON)
                                      .content("""
                  {"name":"Google","url":"https://google.com","method":"GET","intervalSeconds":30,"timeoutSeconds":5}"""))
       .andExpect(status().isCreated())
       .andExpect(jsonPath("$.id").exists());
  }

  @Test void create_blankUrl_returns400() throws Exception {
    mvc.perform(post("/api/endpoints").contentType(MediaType.APPLICATION_JSON)
                                      .content("""
                  {"name":"Bad","url":"","method":"GET","intervalSeconds":30,"timeoutSeconds":5}"""))
       .andExpect(status().isBadRequest());
  }

  @Test void create_intervalTooLow_returns400() throws Exception {
    mvc.perform(post("/api/endpoints").contentType(MediaType.APPLICATION_JSON)
                                      .content("""
                  {"name":"Fast","url":"http://x.com","method":"GET","intervalSeconds":1,"timeoutSeconds":5}"""))
       .andExpect(status().isBadRequest());
  }
}