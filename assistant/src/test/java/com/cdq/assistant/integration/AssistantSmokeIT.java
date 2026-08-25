package com.cdq.assistant.integration;

import com.cdq.assistant.chat.AssistantResponseTask;
import com.cdq.assistant.chat.AssistantResponseTask.Events;
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

            expectedEventNamesShouldBeIn(events, Events.TOOL_EXECUTION.getName(), Events.MESSAGE.getName(), Events.DONE.getName());

            noDocsShouldBeRetrieved(events);

            toolExecutionShouldHappen(events, "get_country_by_name", "germany");

            assistantResponseShouldContain(events, "berlin");
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void testTemperature() throws Exception {
        try (TestClient testClient = new TestClient(port)) {
            var events = testClient.postChat("What is the temperature currently in Munich?");

            expectedEventNamesShouldBeIn(events, Events.TOOL_EXECUTION.getName(), Events.MESSAGE.getName(), Events.DONE.getName());

            toolExecutionShouldHappen(events, "get-weather", "munich");

            assistantResponseShouldNotBeBlank(events);
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void testTemperatureOfCapitalByCountry() throws Exception {
        try (TestClient testClient = new TestClient(port)) {
            var events = testClient.postChat("What is the temperature of the capital of Germany currently?");

            expectedEventNamesShouldBeIn(events, Events.TOOL_EXECUTION.getName(), Events.MESSAGE.getName(), Events.DONE.getName());

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

            expectedEventNamesShouldBeIn(events, Events.TOOL_EXECUTION.getName(), Events.MESSAGE.getName(), Events.DONE.getName());

            toolExecutionShouldHappen(events, "get_country_by_capital", "berlin");

            assistantResponseShouldNotBeBlank(events);
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void testRAGContent() throws Exception {
        try (TestClient testClient = new TestClient(port)) {
            var events = testClient.postChat("How can I protect my company from fraud invoices?");

            expectedEventNamesShouldBeIn(events, Events.RETRIEVED.getName(), Events.MESSAGE.getName(), Events.DONE.getName());

            eventsShouldContainRetrievedDocs(events);

            noToolsShouldBeCalled(events);

            assistantResponseShouldContain(events, "CDQ");
        }
    }

    private void noToolsShouldBeCalled(List<TestClient.SseEvent> events) {
        assertThat(events)
                .filteredOn(e -> Events.TOOL_EXECUTION.getName().equals(e.name()))
                .isEmpty();
    }

    private void noDocsShouldBeRetrieved(List<TestClient.SseEvent> events) {
        assertThat(events)
                .filteredOn(e -> Events.RETRIEVED.getName().equals(e.name()))
                .allSatisfy(e ->
                {
                    var content = JSON.treeToValue(JSON.readTree(e.data()), AssistantResponseTask.RetrievedContent.class);
                    assertThat(content.count())
                            .isEqualTo(0);
                    assertThat(content.documents())
                            .isEmpty();
                }
                );
    }

    private void eventsShouldContainRetrievedDocs(List<TestClient.SseEvent> events) {
        assertThat(events)
                .filteredOn(e -> Events.RETRIEVED.getName().equals(e.name()))
                .allSatisfy(e ->
                {
                    var content = JSON.treeToValue(JSON.readTree(e.data()), AssistantResponseTask.RetrievedContent.class);
                    assertThat(content.count())
                            .isGreaterThan(1);
                    assertThat(content.documents())
                            .isNotEmpty()
                            .allSatisfy(
                                    d -> assertThat(d.score())
                                            .isGreaterThanOrEqualTo(0.5)
                            );

                }
                );
    }

    private static void expectedEventNamesShouldBeIn(List<TestClient.SseEvent> events, String... names) {
        assertThat(events)
                .isNotEmpty()
                .extracting(TestClient.SseEvent::name)
                .doesNotContain("error")
                .contains(names);
    }

    private static void toolExecutionShouldHappen(List<TestClient.SseEvent> events, String toolName, String argumentsPart) {
        assertThat(events)
                .filteredOn(event -> Events.TOOL_EXECUTION.getName().equals(event.name()))
                .anySatisfy(event -> {
                    JsonNode tool = JSON.readTree(event.data());
                    assertThat(tool.path("name").asText()).isEqualTo(toolName);
                    assertThat(tool.path("arguments").asText()).containsIgnoringCase(argumentsPart);
                });
    }

    private static void assistantResponseShouldContain(List<TestClient.SseEvent> events, String messagePart) {
        assertThat(events)
                .filteredOn(event -> Events.MESSAGE.getName().equals(event.name()))
                .extracting(TestClient.SseEvent::data)
                .anySatisfy(data ->
                        assertThat(data)
                                .isNotBlank()
                                .containsIgnoringCase(messagePart)
                );
    }

    private static void assistantResponseShouldNotBeBlank(List<TestClient.SseEvent> events) {
        assertThat(events)
                .filteredOn(event -> Events.MESSAGE.getName().equals(event.name()))
                .extracting(TestClient.SseEvent::data)
                .anySatisfy(data ->
                        assertThat(data)
                                .isNotBlank()
                );
    }

}
