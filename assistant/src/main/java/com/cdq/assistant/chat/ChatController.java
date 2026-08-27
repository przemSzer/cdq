package com.cdq.assistant.chat;

import com.cdq.assistant.config.AssistantProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final Assistant assistant;
    private final AssistantProperties assistantProperties;

    public ChatController(Assistant assistant, AssistantProperties assistantProperties) {
        this.assistant = assistant;
        this.assistantProperties = assistantProperties;
        logger.info("Will use the following chat properties {}", assistantProperties.chat());
    }

    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody ChatRequest request){
        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message is required");
        }
        final var emitter = new SseEmitter(assistantProperties.chat().emitterTimeout().toMillis());
        String userMessage = request.message().trim();
        var task = new AssistantResponseTask(emitter, assistant);
        task.start(userMessage);
        return emitter;
    }


}
