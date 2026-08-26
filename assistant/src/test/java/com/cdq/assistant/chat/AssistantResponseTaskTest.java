package com.cdq.assistant.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.TokenStream;

@ExtendWith(MockitoExtension.class)
class AssistantResponseTaskTest {

    @Mock
    private Assistant assistant;

    @Mock(answer = Answers.RETURNS_SELF)
    private TokenStream tokenStream;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @Test
    void startRunsInferenceOnAssistant() {
        ChatResponse response = ChatResponse.builder()
                .aiMessage(AiMessage.from("Berlin"))
                .tokenUsage(new TokenUsage(10, 4))
                .modelName("qwen3:4b")
                .build();
        given(assistant.answer("What is the capital of Germany?")).willReturn(tokenStream);
        willAnswer(invocation -> {
            Consumer<ChatResponse> onComplete = invocation.getArgument(0);
            willAnswer(start -> {
                onComplete.accept(response);
                return null;
            }).given(tokenStream).start();
            return tokenStream;
        }).given(tokenStream).onCompleteResponse(any());

        var task = new AssistantResponseTask(new SseEmitter(), assistant, Runnable::run);
        task.start("What is the capital of Germany?");

        then(assistant).should().answer(messageCaptor.capture());
        assertEquals("What is the capital of Germany?", messageCaptor.getValue());
        then(tokenStream).should().start();
    }
}
