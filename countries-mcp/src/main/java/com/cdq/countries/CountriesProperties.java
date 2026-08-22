package com.cdq.countries;

public record CountriesProperties(int port, String baseUrl, String apiKey) {

    public static final String DEFAULT_BASE_URL = "https://api.restcountries.com/countries/v5";
    public static final String API_KEY_ENV = "REST_COUNTRIES_API_KEY";
    static final String BASE_URL_ENV = "REST_COUNTRIES_BASE_URL";

    static CountriesProperties load(String[] args) {
        return new CountriesProperties(
                CountriesMcpServer.resolvePort(args),
                envOrDefault(BASE_URL_ENV, DEFAULT_BASE_URL),
                requireApiKey(System.getenv(API_KEY_ENV)));
    }

    public static String requireApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Set " + API_KEY_ENV + " to your REST Countries v5 API key.");
        }
        return apiKey.trim();
    }

    public static String bearer(String apiKey) {
        String trimmed = requireApiKey(apiKey);
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return trimmed;
        }
        return "Bearer " + trimmed;
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
