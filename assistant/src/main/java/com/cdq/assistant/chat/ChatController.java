package com.cdq.assistant.chat;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.tool.BeforeToolExecution;
import dev.langchain4j.service.tool.ToolExecution;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final Assistant assistant;

    public ChatController(Assistant assistant) {
        this.assistant = assistant;
    }

    @PostMapping("/chat")
    public ChatReply chat(@RequestBody ChatRequest request) throws ExecutionException, InterruptedException {
        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message is required");
        }
        var replyStream = assistant.chat(request.message().trim());
        CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();

        replyStream
                .onPartialResponse((String partialResponse) -> logger.debug("partialResponse: {}", partialResponse))
                .onPartialThinking((PartialThinking partialThinking) -> logger.debug("partialThinking: {}", partialThinking))
                .onRetrieved((List<Content> contents) -> logger.debug("contents: {}", contents))
                .onIntermediateResponse((ChatResponse intermediateResponse) -> logger.debug("intermediateResponse: {}", intermediateResponse))
                .onPartialToolCall((PartialToolCall partialToolCall) -> logger.debug("partialToolCall: {}", partialToolCall))
                .beforeToolExecution((BeforeToolExecution beforeToolExecution) -> logger.debug("beforeToolExecution: {}", beforeToolExecution))
                .onToolExecuted((ToolExecution toolExecution) -> logger.debug("toolExecution: {}", toolExecution))
                .onUnmappedRawEvent((Object rawEvent) -> logger.debug("rawEvent: {}", rawEvent))
                .onCompleteResponse(futureResponse::complete)
                .onError(futureResponse::completeExceptionally)
                .start();

        futureResponse.join();

        return new ChatReply(futureResponse.get().aiMessage().text());
    }
}
