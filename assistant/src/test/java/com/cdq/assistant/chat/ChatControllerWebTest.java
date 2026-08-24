package com.cdq.assistant.chat;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatController.class)
class ChatControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Assistant assistant;

    @Test
    void chatStartsSseStream() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(APPLICATION_JSON)
                        .content("{\"message\":\"What is the capital of Germany?\"}"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void chatRejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(APPLICATION_JSON)
                        .content("{\"message\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }
}
