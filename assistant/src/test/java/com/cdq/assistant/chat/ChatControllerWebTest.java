package com.cdq.assistant.chat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;

@WebMvcTest(ChatController.class)
class ChatControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Assistant assistant;

    @Test
    void chatReturnsJsonReply() throws Exception {
        TokenStream tokenStream = mock(TokenStream.class, Answers.RETURNS_SELF);
        ChatResponse response = ChatResponse.builder()
                .aiMessage(AiMessage.from("Berlin"))
                .build();
        given(assistant.chat("What is the capital of Germany?")).willReturn(tokenStream);
        willAnswer(invocation -> {
            Consumer<ChatResponse> onComplete = invocation.getArgument(0);
            willAnswer(start -> {
                onComplete.accept(response);
                return null;
            }).given(tokenStream).start();
            return tokenStream;
        }).given(tokenStream).onCompleteResponse(any());

        mockMvc.perform(post("/api/chat")
                        .contentType(APPLICATION_JSON)
                        .content("{\"message\":\"What is the capital of Germany?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Berlin"));
    }

    @Test
    void chatRejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(APPLICATION_JSON)
                        .content("{\"message\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }
}
