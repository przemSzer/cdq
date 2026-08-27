package com.cdq.assistant.chat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;

import com.cdq.assistant.config.AssistantProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private Assistant assistant;

    private ChatController controller;
    private final Duration emitterTimeout = Duration.ofMinutes(10);

    @BeforeEach
    void setUp() {
        AssistantProperties assistantProperties = new AssistantProperties(
                null, null, null, new AssistantProperties.Chat(emitterTimeout)
        );
        controller = new ChatController(assistant,assistantProperties);
    }

    @Test
    void chatReturnsSseEmitter() {
        SseEmitter emitter = controller.chat(new ChatRequest("What is the capital of Germany?"));

        assertNotNull(emitter);
        assertEquals(emitter.getTimeout(), emitterTimeout.toMillis());
    }

    @Test
    void chatRejectsBlankMessage() {
        var messageRequest = new ChatRequest("  ");
        assertThrows(ResponseStatusException.class, () -> controller.chat(messageRequest));
        then(assistant).shouldHaveNoInteractions();
    }
}
