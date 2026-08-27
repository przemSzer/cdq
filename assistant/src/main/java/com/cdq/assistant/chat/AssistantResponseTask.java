package com.cdq.assistant.chat;

import com.cdq.assistant.chat.events.*;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialThinkingContext;
import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.tool.ToolExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class AssistantResponseTask {

    private static final Logger logger = LoggerFactory.getLogger(AssistantResponseTask.class);

    private final SseEmitter emitter;
    private final Assistant assistant;
    private final String taskId;
    private final Executor executor;
    private final AtomicBoolean shouldStopAsistantInference = new AtomicBoolean(false);

    public AssistantResponseTask(SseEmitter emitter, Assistant assistant) {
        this(emitter, assistant, null);
    }

    AssistantResponseTask(SseEmitter emitter, Assistant assistant, Executor executor) {
        this.emitter = emitter;
        this.assistant = assistant;
        this.taskId = UUID.randomUUID().toString();
        this.executor = executor != null
                ? executor
                : command -> Thread.ofVirtual().name("chat-inference-" + this.taskId).start(command);
    }

    public void start(String userMessage) {
        send(Events.INFERENCE_STARTED.getName(), "started task " + taskId);
        executor.execute(() -> startInference(userMessage));
    }

    private void startInference(String userMessage) {
        try {
            assistant.answer(userMessage)
                    .onRetrieved(this::onRetrieved)
                    .onPartialThinkingWithContext(this::onPartialThinking)
                    .onPartialResponseWithContext((pR, context) ->cancelStreamingIfNecessary(context.streamingHandle()))
                    .onPartialToolCallWithContext((pTC, context) -> cancelStreamingIfNecessary(context.streamingHandle()))
                    .onToolExecuted(this::onToolExecuted)
                    .onCompleteResponse(this::onCompleteResponse)
                    .onError(this::onError)
                    .start();
        } catch (RuntimeException ex) {
            logger.error("Inference failed before stream started", ex);
            onError(ex);
            shouldStopAsistantInference.set(true);
        }
    }

    private void onCompleteResponse(ChatResponse response) {
        var message = "";
        if (response.aiMessage() != null) {
            message = response.aiMessage().text();
        }
        send(Events.MESSAGE.getName(), message);
        var usage = response.tokenUsage();
        send(Events.DONE.getName(), new ResponseSummary(
                usage == null ? 0 : usage.outputTokenCount(),
                usage == null ? 0 : usage.inputTokenCount(),
                response.modelName()));
        emitter.complete();
    }

    private void cancelStreamingIfNecessary(StreamingHandle handle) {
        if (shouldStopAsistantInference.get()){
            logger.info("Stoping assistant inference");
            handle.cancel();
        }
    }

    private void onRetrieved(List<Content> contents) {
        logger.debug("retrieved: {}", contents);
        List<RetrievedDocument> documents = contents == null ? List.of() : contents.stream()
                .map(RetrievedDocument::toRetrievedDocument)
                .toList();
        send(Events.RETRIEVED.getName(), new RetrievedContent(documents.size(), documents));
    }

    private void onPartialThinking(PartialThinking partialThinking, PartialThinkingContext context) {
        logger.debug("partialThinking: {}", partialThinking);
        cancelStreamingIfNecessary(context.streamingHandle());
        if (partialThinking != null && partialThinking.text() != null) {
            send(Events.PARTIAL_THINKING.getName(), partialThinking.text());
        }
    }

    private void onToolExecuted(ToolExecution toolExecution) {
        logger.debug("toolExecution: {}", toolExecution);
        var toolRequest = toolExecution.request();
        send(Events.TOOL_EXECUTION.getName(), new ToolCallSummary(
                toolRequest == null ? "" : toolRequest.name(),
                toolRequest == null ? "" : toolRequest.arguments(),
                toolExecution.result())
        );
    }

    private void onError(Throwable ex) {
        send(Events.ERROR.getName(), ex.getMessage());
        emitter.completeWithError(ex);
    }

    private void send(String event, Object data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (IOException ex) {
            logger.debug("client gone, completing emitter", ex);
            shouldStopAsistantInference.set(true);
            emitter.complete();
        }
    }
}
