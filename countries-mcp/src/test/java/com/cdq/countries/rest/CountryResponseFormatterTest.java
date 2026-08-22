package com.cdq.countries.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.json.McpJsonDefaults;

class CountryResponseFormatterTest {

    private CountryResponseFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new CountryResponseFormatter(McpJsonDefaults.getMapper());
    }

    @Test
    void mapsRestCountriesJsonToShortString() {
        String json = """
                [
                  {
                    "name": { "common": "Germany", "official": "Federal Republic of Germany" },
                    "capital": ["Berlin"],
                    "region": "Europe",
                    "population": 83240525
                  }
                ]
                """;

        List<String> summaries = formatter.formatAll(json);

        assertEquals(List.of("Germany. Capital: Berlin. Region: Europe. Population: 83240525."), summaries);
    }

    @Test
    void mapsFlatCountryJsonToShortString() {
        String json = """
                [
                  {
                    "name": "Germany",
                    "capital": "Berlin",
                    "region": "Europe",
                    "population": 83240525
                  }
                ]
                """;

        List<String> summaries = formatter.formatAll(json);

        assertEquals(List.of("Germany. Capital: Berlin. Region: Europe. Population: 83240525."), summaries);
    }

    @Test
    void mapsV5EnvelopeToShortString() {
        String json = """
                {
                  "data": {
                    "objects": [
                      {
                        "names": { "common": "Germany" },
                        "capitals": [{ "name": "Berlin", "attributes": { "primary": true } }],
                        "region": "Europe",
                        "population": 83240525
                      }
                    ]
                  }
                }
                """;

        List<String> summaries = formatter.formatAll(json);

        assertEquals(List.of("Germany. Capital: Berlin. Region: Europe. Population: 83240525."), summaries);
    }

    @Test
    void returnsEmptyListForEmptyArray() {
        assertTrue(formatter.formatAll("[]").isEmpty());
    }

    @Test
    void returnsEmptyListForInvalidJson() {
        assertTrue(formatter.formatAll("{\"status\":404}").isEmpty());
        assertTrue(formatter.formatAll("not-json").isEmpty());
        assertTrue(formatter.formatAll("").isEmpty());
    }
}
