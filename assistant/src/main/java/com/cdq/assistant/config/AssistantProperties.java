package com.cdq.assistant.config;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.cdq.rag.RagProperties;

@ConfigurationProperties(prefix = "assistant")
public record AssistantProperties(
        CountriesMcp countriesMcp,
        Weather weather,
        Rag rag,
        Chat chat
) {

    public List<String> weatherLaunchCommand() {
        return List.of(weather.command(), resolvedWeatherScript(), "--mcp");
    }
    
    String resolvedWeatherScript() {
        String script = weather.script();
        String directory = weather.directory();
        if (directory != null && !directory.isBlank()) {
            return Path.of(directory, script).toAbsolutePath().toString();
        }
        return Path.of(script).toAbsolutePath().toString();
    }

    public record CountriesMcp(String url) {
    }

    public record Weather(
            String command,
            String directory,
            String script,
            String apiKey,
            String apiUrl) {
    }

    public record Rag(
            String jdbcHost,
            int jdbcPort,
            String jdbcDatabase,
            String jdbcUser,
            String jdbcPassword,
            String ollamaBaseUrl,
            String embeddingModel,
            int embeddingDimension,
            int maxResults,
            double minScore) {
        public RagProperties toRagProperties() {
            return new RagProperties(
                    jdbcHost,
                    jdbcPort,
                    jdbcDatabase,
                    jdbcUser,
                    jdbcPassword,
                    ollamaBaseUrl,
                    embeddingModel,
                    embeddingDimension,
                    RagProperties.defaultIngestCacheDir());
        }
    }

    public record Chat(Duration emitterTimeout) {
    }
}
