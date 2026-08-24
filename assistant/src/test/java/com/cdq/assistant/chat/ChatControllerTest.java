package com.cdq.assistant.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private Assistant assistant;

    @Mock(answer = Answers.RETURNS_SELF)
    private TokenStream tokenStream;

    @InjectMocks
    private ChatController controller;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @Test
    void chatDelegatesToAssistant() throws Exception {
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

        ChatReply reply = controller.chat(new ChatRequest("What is the capital of Germany?"));

        then(assistant).should().chat(messageCaptor.capture());
        assertEquals("What is the capital of Germany?", messageCaptor.getValue());
        assertEquals("Berlin", reply.reply());
    }

    @Test
    void chatRejectsBlankMessage() {
        var messageRequest = new ChatRequest("  ");
        assertThrows(ResponseStatusException.class,
                () -> controller.chat(messageRequest));
        then(assistant).shouldHaveNoInteractions();
    }
}
