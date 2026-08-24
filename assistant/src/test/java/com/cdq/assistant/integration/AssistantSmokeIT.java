package com.cdq.assistant.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "RUN_LIVE_ASSISTANT", matches = "true")
class AssistantSmokeIT {

    private static final ObjectMapper JSON = new ObjectMapper();

    @LocalServerPort
    int port;

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void testCountryInfo() throws Exception {
        try (TestClient testClient = new TestClient(port)) {
            var events = testClient.postChat("What is the capital city of Germany?");

            expectedEventNamesShouldBeIn(events);

            toolExecutionShouldHappen(events, "get_country_by_name", "germany");

            assistantResponseShouldContain(events, "berlin");
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void testTemperature() throws Exception {
        try (TestClient testClient = new TestClient(port)) {
            var events = testClient.postChat("What is the temperature currently in Munich?");

            expectedEventNamesShouldBeIn(events);

            toolExecutionShouldHappen(events, "get-weather", "munich");

            assistantResponseShouldNotBeBlank(events);
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void testTemperatureOfCapitalByCountry() throws Exception {
        try (TestClient testClient = new TestClient(port)) {
            var events = testClient.postChat("What is the temperature of the capital of Germany currently?");

            expectedEventNamesShouldBeIn(events);

            toolExecutionShouldHappen(events, "get-weather", "berlin");

            toolExecutionShouldHappen(events, "get_country_by_name", "germany");

            assistantResponseShouldNotBeBlank(events);
        }
    }
    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void testCityInfo() throws Exception {
        try (TestClient testClient = new TestClient(port)) {
            var events = testClient.postChat("What do you know about Berlin?");

            expectedEventNamesShouldBeIn(events);

            toolExecutionShouldHappen(events, "get_country_by_capital", "berlin");

            assistantResponseShouldNotBeBlank(events);
        }
    }

    private static void expectedEventNamesShouldBeIn(List<TestClient.SseEvent> events) {
        assertThat(events)
                .isNotEmpty()
                .extracting(TestClient.SseEvent::name)
                .doesNotContain("error")
                .contains("toolExecution", "message", "done");
    }

    private static void toolExecutionShouldHappen(List<TestClient.SseEvent> events, String toolName, String argumentsPart) {
        assertThat(events)
                .filteredOn(event -> "toolExecution".equals(event.name()))
                .anySatisfy(event -> {
                    JsonNode tool = JSON.readTree(event.data());
                    assertThat(tool.path("name").asText()).isEqualTo(toolName);
                    assertThat(tool.path("arguments").asText()).containsIgnoringCase(argumentsPart);
                });
    }

    private static void assistantResponseShouldContain(List<TestClient.SseEvent> events, String messagePart) {
        assertThat(events)
                .filteredOn(event -> "message".equals(event.name()))
                .extracting(TestClient.SseEvent::data)
                .anySatisfy(data ->
                        assertThat(data)
                                .isNotBlank()
                                .containsIgnoringCase(messagePart)
                );
    }

    private static void assistantResponseShouldNotBeBlank(List<TestClient.SseEvent> events) {
        assertThat(events)
                .filteredOn(event -> "message".equals(event.name()))
                .extracting(TestClient.SseEvent::data)
                .anySatisfy(data ->
                        assertThat(data)
                                .isNotBlank()
                );
    }

}
