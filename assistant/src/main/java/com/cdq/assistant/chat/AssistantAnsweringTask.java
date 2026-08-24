package com.cdq.assistant.chat;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import dev.langchain4j.rag.content.ContentMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.tool.BeforeToolExecution;
import dev.langchain4j.service.tool.ToolExecution;

public class AssistantAnsweringTask {

    private static final Logger logger = LoggerFactory.getLogger(AssistantAnsweringTask.class);

    private final SseEmitter emitter;
    private final Assistant assistant;
    private final String taskId;
    private final Executor executor;

    public AssistantAnsweringTask(SseEmitter emitter, Assistant assistant) {
        this(emitter, assistant, null);
    }

    AssistantAnsweringTask(SseEmitter emitter, Assistant assistant, Executor executor) {
        this.emitter = emitter;
        this.assistant = assistant;
        this.taskId = UUID.randomUUID().toString();
        this.executor = executor != null
                ? executor
                : command -> Thread.ofVirtual().name("chat-inference-" + this.taskId).start(command);
    }

    public void start(String userMessage) {
        send("inferenceStarted", "started task " + taskId);
        executor.execute(() -> startInference(userMessage));
    }

    //TODO: Cancelling request to ollama on refresh
    private void startInference(String userMessage) {
        try {
            assistant.chat(userMessage)
                    .onRetrieved(this::onRetrieved)
                    .onPartialThinking((PartialThinking partialThinking) -> {
                        logger.debug("partialThinking: {}", partialThinking);
                        if (partialThinking != null && partialThinking.text() != null) {
                            send("partialThinking", partialThinking.text());
                        }
                    })
                    .beforeToolExecution((BeforeToolExecution beforeToolExecution) ->
                            logger.debug("beforeToolExecution: {}", beforeToolExecution))
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        logger.debug("toolExecution: {}", toolExecution);
                        var toolRequest = toolExecution.request();
                        send("toolExecution", new ToolCallSummary(
                                toolRequest == null ? "" : toolRequest.name(),
                                toolRequest == null ? "" : toolRequest.arguments(),
                                toolExecution.result()));
                    })
                    .onCompleteResponse(response -> {
                        var message = "";
                        if (response.aiMessage() != null) {
                            message = response.aiMessage().text();
                        }
                        send("message", message);
                        var usage = response.tokenUsage();
                        send("done", new ResponseSummary(
                                usage == null ? 0 : usage.outputTokenCount(),
                                usage == null ? 0 : usage.inputTokenCount(),
                                response.modelName()));
                        emitter.complete();
                    })
                    .onError(ex -> {
                        send("error", ex.getMessage());
                        emitter.completeWithError(ex);
                    })
                    .start();
        } catch (RuntimeException ex) {
            logger.debug("inference failed before stream started", ex);
            send("error", ex.getMessage());
            emitter.complete();
        }
    }

    private void onRetrieved(List<Content> contents) {
        logger.debug("retrieved: {}", contents);
        List<RetrievedDocument> documents = contents == null ? List.of() : contents.stream()
                .map(AssistantAnsweringTask::toRetrievedDocument)
                .toList();
        send("retrieved", new RetrievedContent(documents.size(), documents));
    }

    private static RetrievedDocument toRetrievedDocument(Content content) {
        if (content == null || content.textSegment() == null) {
            return new RetrievedDocument("", "", "", null);
        }
        var segment = content.textSegment();
        Object score = content.metadata() == null ? null : content.metadata().get(ContentMetadata.SCORE);
        Double scoreValue = score instanceof Number number ? number.doubleValue() : null;
        return new RetrievedDocument(
                metadataValue(segment.metadata(), "title"),
                metadataValue(segment.metadata(), "url"),
                segment.text() == null ? "" : segment.text(),
                scoreValue
        );
    }

    private static String metadataValue(Metadata metadata, String key) {
        if (metadata == null || !metadata.containsKey(key)) {
            return "";
        }
        String value = metadata.getString(key);
        return value == null ? "" : value;
    }

    private void send(String event, Object data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (IOException ex) {
            logger.debug("client gone, completing emitter", ex);
            emitter.complete();
        }
    }

    record ToolCallSummary(String name, String arguments, String result) {}

    record RetrievedDocument(String title, String url, String text, Double score) {}

    record RetrievedContent(int count, List<RetrievedDocument> documents) {}

    record ResponseSummary(int responseTokenCount, int messageTokenCount, String modelName) {}
}
