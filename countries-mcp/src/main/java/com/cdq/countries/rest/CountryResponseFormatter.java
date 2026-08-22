package com.cdq.countries.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.json.McpJsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CountryResponseFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountryResponseFormatter.class);

    private final McpJsonMapper jsonMapper;

    public CountryResponseFormatter(McpJsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public List<String> formatAll(String json) {
        LOGGER.debug("Formatting JSON: {}", json);
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<Map<String, Object>> countries = countriesFrom(jsonMapper.readValue(json, Object.class));
            if (countries.isEmpty()) {
                return List.of();
            }
            List<String> summaries = new ArrayList<>();
            for (Map<String, Object> country : countries) {
                summaries.add(format(country));
            }
            return List.copyOf(summaries);
        } catch (IOException | RuntimeException ex) {
            LOGGER.error("Error formatting JSON: {}", ex.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> countriesFrom(Object parsed) {
        if (parsed instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        if (parsed instanceof Map<?, ?> root) {
            Object data = root.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object objects = dataMap.get("objects");
                if (objects instanceof List<?> list) {
                    return (List<Map<String, Object>>) list;
                }
            }
        }
        return List.of();
    }

    private static String format(Map<String, Object> country) {
        return "%s. Capital: %s. Region: %s. Population: %s.".formatted(
                commonName(country),
                capital(country),
                region(country),
                population(country));
    }

    private static String commonName(Map<String, Object> country) {
        Object names = country.get("names");
        if (names instanceof Map<?, ?> namesMap) {
            Object common = namesMap.get("common");
            if (common != null && !common.toString().isBlank()) {
                return common.toString();
            }
        }
        Object name = country.get("name");
        if (name instanceof String common && !common.isBlank()) {
            return common;
        }
        if (name instanceof Map<?, ?> nameMap) {
            Object common = nameMap.get("common");
            if (common != null && !common.toString().isBlank()) {
                return common.toString();
            }
            Object official = nameMap.get("official");
            if (official != null && !official.toString().isBlank()) {
                return official.toString();
            }
        }
        return "Unknown country";
    }

    private static String capital(Map<String, Object> country) {
        Object capitals = country.get("capitals");
        if (capitals instanceof List<?> cities && !cities.isEmpty()) {
            Object first = cities.getFirst();
            if (first instanceof Map<?, ?> city && city.get("name") != null) {
                return city.get("name").toString();
            }
            if (first != null) {
                return first.toString();
            }
        }
        Object capital = country.get("capital");
        if (capital instanceof List<?> cities && !cities.isEmpty() && cities.getFirst() != null) {
            return cities.getFirst().toString();
        }
        if (capital instanceof String city && !city.isBlank()) {
            return city;
        }
        return "unknown";
    }

    private static String region(Map<String, Object> country) {
        Object region = country.get("region");
        if (region == null || region.toString().isBlank()) {
            return "unknown";
        }
        return region.toString();
    }

    private static String population(Map<String, Object> country) {
        Object population = country.get("population");
        if (population instanceof Number number) {
            return Long.toString(number.longValue());
        }
        if (population != null) {
            return population.toString();
        }
        return "unknown";
    }
}
