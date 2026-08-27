package com.cdq.assistant.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@SpringJUnitConfig(
        classes = AssistantPropertiesTest.TestConfig.class,
        initializers = ConfigDataApplicationContextInitializer.class)
class AssistantPropertiesTest {

    @Configuration
    @EnableConfigurationProperties(AssistantProperties.class)
    static class TestConfig {
    }

    @Autowired
    AssistantProperties properties;

    @Test
    void bindsEveryPropertyFromApplicationYml() {
        assertNotNull(properties);

        assertNotNull(properties.countriesMcp());
        assertEquals("http://localhost:8081/mcp", properties.countriesMcp().url());

        assertNotNull(properties.weather());
        assertEquals("node", properties.weather().command());
        assertNotNull(properties.weather().directory());
        assertEquals("mcp-weather/weather-mcp.mjs", properties.weather().script());
        assertNotNull(properties.weather().apiKey());
        assertEquals("https://api.weatherapi.com/v1/current.json", properties.weather().apiUrl());

        assertNotNull(properties.rag());
        assertEquals("localhost", properties.rag().jdbcHost());
        assertEquals(5432, properties.rag().jdbcPort());
        assertEquals("rag", properties.rag().jdbcDatabase());
        assertEquals("rag", properties.rag().jdbcUser());
        assertEquals("rag", properties.rag().jdbcPassword());
        assertEquals("http://localhost:11434", properties.rag().ollamaBaseUrl());
        assertEquals("nomic-embed-text", properties.rag().embeddingModel());
        assertEquals(768, properties.rag().embeddingDimension());
        assertEquals(4, properties.rag().maxResults());
        assertEquals(0.8, properties.rag().minScore());

        assertEquals(properties.chat().emitterTimeout(), Duration.ofMinutes(5));

    }

    @Test
    void weatherCommandUsesConfiguredCommandDirectoryAndScript() {
        List<String> command = properties.weatherLaunchCommand();

        assertEquals("node", command.get(0));
        assertEquals(
                Path.of("mcp-weather", "weather-mcp.mjs").toAbsolutePath().toString(),
                command.get(1)
        );
        assertEquals("--mcp", command.get(2));
    }
}
