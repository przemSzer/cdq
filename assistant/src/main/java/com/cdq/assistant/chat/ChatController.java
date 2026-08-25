package com.cdq.assistant.chat;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final Assistant assistant;

    public ChatController(Assistant assistant) {
        this.assistant = assistant;
    }

    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody ChatRequest request){
        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message is required");
        }
        //TODO: move timeout to application.yml
        final var emitter = new SseEmitter(60_000L * 5L);
        String userMessage = request.message().trim();
        var task = new AssistantResponseTask(emitter, assistant);
        task.start(userMessage);
        return emitter;
    }


}
