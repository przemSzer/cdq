package com.cdq.assistant.chat;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private Assistant assistant;

    @InjectMocks
    private ChatController controller;

    @Test
    void chatReturnsSseEmitter() {
        SseEmitter emitter = controller.chat(new ChatRequest("What is the capital of Germany?"));

        assertNotNull(emitter);
    }

    @Test
    void chatRejectsBlankMessage() {
        var messageRequest = new ChatRequest("  ");
        assertThrows(ResponseStatusException.class, () -> controller.chat(messageRequest));
        then(assistant).shouldHaveNoInteractions();
    }
}
