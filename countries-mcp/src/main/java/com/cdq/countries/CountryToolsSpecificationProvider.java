package com.cdq.countries;

import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public final class CountryToolsSpecificationProvider {

    static final String GET_COUNTRY_BY_NAME = "get_country_by_name";
    static final String GET_COUNTRY_BY_CAPITAL = "get_country_by_capital";

    private final CountryLookup countries;

    public CountryToolsSpecificationProvider(CountryLookup countries) {
        this.countries = countries;
    }

    public List<SyncToolSpecification> specifications() {
        return List.of(byNameToolSpec(), byCapitalToolSpec());
    }

    private SyncToolSpecification byNameToolSpec() {
        return SyncToolSpecification.builder()
                .tool(getCountryByNameToolSchema())
                .callHandler(
                    (exchange, request) -> toTextToolResult(countries.findByName(extractArgumentFromRequest(request, "name")))
                )
                .build();
    }

    private static Tool getCountryByNameToolSchema() {
        return Tool.builder(GET_COUNTRY_BY_NAME, stringArgumentSchema("name", "Country name, for example Germany"))
                .description("Look up a country by its common or official name. Returns a short summary.")
                .build();
    }

    private SyncToolSpecification byCapitalToolSpec() {
        return SyncToolSpecification.builder()
                .tool(getCountryByCapitalToolSchema())
                .callHandler((exchange, request) -> toTextToolResult(countries.findByCapital(extractArgumentFromRequest(request, "capital"))))
                .build();
    }

    private static Tool getCountryByCapitalToolSchema() {
        return Tool.builder(GET_COUNTRY_BY_CAPITAL, stringArgumentSchema("capital", "Capital city, for example Berlin"))
                .description("Look up a country by its capital city. Returns a short summary for the country.")
                .build();
    }

    private static Map<String, Object> stringArgumentSchema(String name, String description) {
        return Map.of(
                "type", "object",
                "properties", Map.of(name, Map.of(
                        "type", "string",
                        "description", description)),
                "required", List.of(name),
                "additionalProperties", false);
    }

    private static String extractArgumentFromRequest(CallToolRequest request, String name) {
        Object value = request.arguments() == null ? null : request.arguments().get(name);
        return value == null ? "" : value.toString();
    }

    private static CallToolResult toTextToolResult(String text) {
        return CallToolResult.builder()
                .addTextContent(text)
                .build();
    }
}
