package com.cdq.assistant.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class AssistantPropertiesTest {

    @Test
    void weatherCommandUsesConfiguredCommandDirectoryAndScript() {
        AssistantProperties properties = new AssistantProperties();
        properties.getWeather().setCommand("node");
        properties.getWeather().setDirectory("C:/tools/mcp-weather");
        properties.getWeather().setScript("weather-mcp.mjs");

        List<String> command = properties.weatherLaunchCommand();

        assertEquals("node", command.get(0));
        assertEquals(
                Path.of("C:/tools/mcp-weather", "weather-mcp.mjs").toAbsolutePath().toString(),
                command.get(1));
        assertEquals("--mcp", command.get(2));
    }

    @Test
    void weatherCommandDefaultsToNodeAndVendoredScript() {
        AssistantProperties properties = new AssistantProperties();

        List<String> command = properties.weatherLaunchCommand();

        assertEquals("node", command.get(0));
        assertEquals(Path.of("mcp-weather/weather-mcp.mjs").toAbsolutePath().toString(), command.get(1));
        assertEquals("--mcp", command.get(2));
    }

    @Test
    void ragDefaultsMatchPgvectorComposeAndNomicEmbeddings() {
        AssistantProperties properties = new AssistantProperties();

        assertEquals("localhost", properties.getRag().getJdbcHost());
        assertEquals(5432, properties.getRag().getJdbcPort());
        assertEquals("rag", properties.getRag().getJdbcDatabase());
        assertEquals("nomic-embed-text", properties.getRag().toRagProperties().embeddingModel());
        assertEquals(768, properties.getRag().toRagProperties().embeddingDimension());
        assertEquals("jdbc:postgresql://localhost:5432/rag", properties.getRag().toRagProperties().jdbcUrl());
    }
}
