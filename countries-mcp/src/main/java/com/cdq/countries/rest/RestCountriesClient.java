package com.cdq.countries.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.cdq.countries.CountriesMCPProperties;
import com.cdq.countries.CountryLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RestCountriesClient implements CountryLookup {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestCountriesClient.class);
    static final String DEFAULT_BASE_URL = CountriesMCPProperties.DEFAULT_BASE_URL;
    private static final String FIELDS = "response_fields=names.common,capitals,region,population";

    private final HttpGetter httpGetter;
    private final String baseUrl;
    private final CountryResponseFormatter formatter;

    public RestCountriesClient(HttpGetter httpGetter, CountryResponseFormatter formatter) {
        this(httpGetter, formatter, DEFAULT_BASE_URL);
    }

    public RestCountriesClient(HttpGetter httpGetter, CountryResponseFormatter formatter, String baseUrl) {
        this.httpGetter = httpGetter;
        this.formatter = formatter;
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    @Override
    public String findByName(String name) {
        return lookup("name", name, "/names.common/");
    }

    @Override
    public String findByCapital(String capital) {
        return lookup("capital", capital, "/capitals/");
    }

    private String lookup(String kind, String query, String path) {
        LOGGER.info("Looking up {} with query: {}", kind, query);
        if (query == null || query.isBlank()) {
            return "A " + kind + " is required.";
        }
        String trimmed = query.trim();
        URI uri = URI.create(baseUrl + path + encode(trimmed) + "?" + FIELDS);
        LOGGER.debug("GET {}", uri);
        try {
            HttpResult result = httpGetter.get(uri);
            if (result.statusCode() == 401) {
                LOGGER.warn("REST Countries rejected the API key (401). Check {}", CountriesMCPProperties.API_KEY_ENV);
                return "Failed to look up " + kind + " '" + trimmed + "'.";
            }
            if (result.statusCode() == 404) {
                LOGGER.info("No country found for {} '{}'", kind, trimmed);
                return notFound(kind, trimmed);
            }
            if (result.statusCode() != 200) {
                LOGGER.warn("REST Countries returned {} body: '{}' for {} '{}'", 
                    result.statusCode(), result.body(),
                    kind, trimmed
                );
                return "Failed to look up " + kind + " '" + trimmed + "'.";
            }
            List<String> summaries = formatter.formatAll(result.body());
            if (summaries.isEmpty()) {
                LOGGER.info("No country found for {} '{}'", kind, trimmed);
                return notFound(kind, trimmed);
            }
            return String.join("\n", summaries);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Lookup interrupted for {} '{}'", kind, trimmed);
            return "Failed to look up " + kind + " '" + trimmed + "'.";
        } catch (IOException | RuntimeException ex) {
            LOGGER.error("Failed to look up {} '{}': {}", kind, trimmed, ex.getMessage());
            return "Failed to look up " + kind + " '" + trimmed + "'.";
        }
    }

    private static String notFound(String kind, String query) {
        return "No country found for " + kind + " '" + query + "'.";
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
