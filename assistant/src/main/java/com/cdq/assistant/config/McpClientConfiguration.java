package com.cdq.assistant.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.service.tool.ToolProvider;

@Configuration
@EnableConfigurationProperties(AssistantProperties.class)
public class McpClientConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(McpClientConfiguration.class);
    
    @Bean
    McpClient countriesMcpClient(AssistantProperties properties) {
        var transport = StreamableHttpMcpTransport.builder()
                .url(properties.getCountriesMcp().getUrl())
                .timeout(Duration.ofSeconds(30))
                .setHttpVersion1_1()
                .logRequests(true)
                .logResponses(true)
                .build();
        return DefaultMcpClient.builder()
                .key("countries")
                .transport(transport)
                .toolExecutionTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Bean
    McpClient weatherMcpClient(AssistantProperties properties) {
        logger.info("Starting weather MCP client");
        AssistantProperties.Weather weather = properties.getWeather();
        if (weather.getApiKey() == null || weather.getApiKey().isBlank()) {
            throw new IllegalStateException("Set WEATHER_API_KEY for the weather MCP server.");
        }
        Map<String, String> environment = new HashMap<>();
        environment.put("WEATHER_API_KEY", weather.getApiKey());
        environment.put("WEATHER_API_URL", weather.getApiUrl());
        logger.info("Will use the following command: {}", properties.weatherLaunchCommand());
        var transport = StdioMcpTransport.builder()
                .command(properties.weatherLaunchCommand())
                .environment(environment)
                .logEvents(false)
                .build();
        return DefaultMcpClient.builder()
                .key("weather")
                .transport(transport)
                .toolExecutionTimeout(Duration.ofSeconds(45))
                .build();
    }

    @Bean
    ToolProvider mcpToolProvider(List<McpClient> mcpClients) {
        return McpToolProvider.builder()
                .mcpClients(mcpClients)
                .failIfOneServerFails(false)
                .build();
    }
}
