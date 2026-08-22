package com.cdq.countries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

@ExtendWith(MockitoExtension.class)
class CountryToolsTest {

    @Mock
    private CountryLookup countries;

    @Captor
    private ArgumentCaptor<String> queryCaptor;

    private CountryTools tools;

    @BeforeEach
    void setUp() {
        tools = new CountryTools(countries);
    }

    @Test
    void getCountryByNameDelegatesToClient() {
        given(countries.findByName("Germany"))
                .willReturn("Germany. Capital: Berlin. Region: Europe. Population: 83240525.");

        CallToolResult result = invoke(CountryTools.GET_COUNTRY_BY_NAME, Map.of("name", "Germany"));

        then(countries).should().findByName(queryCaptor.capture());
        assertEquals("Germany", queryCaptor.getValue());
        assertEquals(
                "Germany. Capital: Berlin. Region: Europe. Population: 83240525.",
                text(result));
    }

    @Test
    void getCountryByCapitalDelegatesToClient() {
        given(countries.findByCapital("Berlin"))
                .willReturn("Germany. Capital: Berlin. Region: Europe. Population: 83240525.");

        CallToolResult result = invoke(CountryTools.GET_COUNTRY_BY_CAPITAL, Map.of("capital", "Berlin"));

        then(countries).should().findByCapital(queryCaptor.capture());
        assertEquals("Berlin", queryCaptor.getValue());
        assertEquals(
                "Germany. Capital: Berlin. Region: Europe. Population: 83240525.",
                text(result));
    }

    private CallToolResult invoke(String toolName, Map<String, Object> arguments) {
        SyncToolSpecification spec = tools.specifications().stream()
                .filter(tool -> tool.tool().name().equals(toolName))
                .findFirst()
                .orElseThrow();
        return spec.callHandler().apply(null, CallToolRequest.builder(toolName).arguments(arguments).build());
    }

    private static String text(CallToolResult result) {
        return ((TextContent) result.content().getFirst()).text();
    }
}
