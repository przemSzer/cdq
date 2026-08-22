package com.cdq.countries.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.modelcontextprotocol.json.McpJsonDefaults;

@ExtendWith(MockitoExtension.class)
class RestCountriesClientTest {

    private static final String GERMANY_JSON = """
            [
              {
                "name": { "common": "Germany", "official": "Federal Republic of Germany" },
                "capital": ["Berlin"],
                "region": "Europe",
                "population": 83240525
              }
            ]
            """;

    @Mock
    private HttpGetter httpGetter;

    @Captor
    private ArgumentCaptor<URI> uriCaptor;

    private RestCountriesClient client;

    @BeforeEach
    void setUp() {
        client = new RestCountriesClient(
                httpGetter,
                new CountryResponseFormatter(McpJsonDefaults.getMapper()),
                RestCountriesClient.DEFAULT_BASE_URL);
    }

    @Test
    void findByNameFormatsSuccessfulResponse() throws Exception {
        given(httpGetter.get(any())).willReturn(new HttpResult(200, GERMANY_JSON));

        String summary = client.findByName("Germany");

        then(httpGetter).should().get(uriCaptor.capture());
        assertEquals("Germany. Capital: Berlin. Region: Europe. Population: 83240525.", summary);
        assertEquals(
                URI.create("https://api.restcountries.com/countries/v5/names.common/Germany?response_fields=names.common,capitals,region,population"),
                uriCaptor.getValue());
    }

    @Test
    void findByCapitalFormatsSuccessfulResponse() throws Exception {
        given(httpGetter.get(any())).willReturn(new HttpResult(200, GERMANY_JSON));

        String summary = client.findByCapital("Berlin");

        then(httpGetter).should().get(uriCaptor.capture());
        assertEquals("Germany. Capital: Berlin. Region: Europe. Population: 83240525.", summary);
        assertTrue(uriCaptor.getValue().toString().contains("/capitals/Berlin"));
    }

    @Test
    void findByNameReturnsNotFoundFor404() throws Exception {
        given(httpGetter.get(any())).willReturn(new HttpResult(404, "{\"status\":404}"));

        assertEquals("No country found for name 'Atlantis'.", client.findByName("Atlantis"));
    }

    @Test
    void findByNameReturnsNotFoundForEmptyList() throws Exception {
        given(httpGetter.get(any())).willReturn(new HttpResult(200, "[]"));

        assertEquals("No country found for name 'X'.", client.findByName("X"));
    }

    @Test
    void findByNameReturnsFailureForNonSuccessStatus() throws Exception {
        given(httpGetter.get(any())).willReturn(new HttpResult(500, "error"));

        assertEquals("Failed to look up name 'Germany'.", client.findByName("Germany"));
    }

    @Test
    void findByNameReturnsFailureForUnauthorized() throws Exception {
        given(httpGetter.get(any())).willReturn(new HttpResult(401, "{\"errors\":[{\"message\":\"unauthorized\"}]}"));

        assertEquals("Failed to look up name 'Germany'.", client.findByName("Germany"));
    }

    @Test
    void findByNameRejectsBlankQuery() {
        assertEquals("A name is required.", client.findByName("  "));
    }
}
