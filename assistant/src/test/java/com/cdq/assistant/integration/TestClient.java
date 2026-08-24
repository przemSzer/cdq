package com.cdq.assistant.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.cdq.assistant.chat.ChatRequest;
import com.fasterxml.jackson.core.JsonProcessingException;

public class TestClient implements AutoCloseable{

    private final HttpClient restClient = HttpClient.newHttpClient();
    private final int port;
    private final ObjectMapper JSON = new ObjectMapper();

    public TestClient(int port) {
        this.port = port;
    }

    public List<SseEvent> postChat(String message) throws Exception {
        HttpRequest request = buildRequest(message);
        HttpResponse<Stream<String>> response =
                restClient.send(request, HttpResponse.BodyHandlers.ofLines());
        List<SseEvent> events = new ArrayList<>();
        String eventName = "message";
        StringBuilder data = new StringBuilder();
        try (var lines = response.body()) {
            for (String line : (Iterable<String>) lines::iterator) {
                if (line.isEmpty()) {
                    if (!data.isEmpty() || !"message".equals(eventName)) {
                        events.add(new SseEvent(eventName, data.toString()));
                    }
                    if ("done".equals(eventName) || "error".equals(eventName)) {
                        break;
                    }
                    eventName = "message";
                    data.setLength(0);
                    continue;
                }
                if (line.startsWith("event:")) {
                    eventName = line.substring("event:".length()).trim();
                } else if (line.startsWith("data:")) {
                    if (!data.isEmpty()) {
                        data.append('\n');
                    }
                    data.append(line.substring("data:".length()).trim());
                }
            }
        }
        return events;
    }

    private HttpRequest buildRequest(String message) {
        try{
        var payload =  JSON.writeValueAsString(new ChatRequest(message));
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/chat"))
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Accept", MediaType.TEXT_EVENT_STREAM_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        restClient.close();
    }

    public record SseEvent(String name, String data) {}
}
