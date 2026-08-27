package com.cdq.assistant.chat;

import com.cdq.assistant.config.AssistantProperties;
import dev.langchain4j.service.TokenStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@EnableConfigurationProperties(AssistantProperties.class)
class ChatControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Assistant assistant;

    private final TokenStream tokenStream = mock(TokenStream.class);

    @Test
    void chatStartsSseStream() throws Exception {
        given(assistant.answer(anyString()))
                .willReturn(tokenStream);
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
