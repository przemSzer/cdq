package com.cdq.countries.rest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class JdkHttpGetter implements HttpGetter {

    private final HttpClient httpClient;
    private final Map<String, String> extraHeaders;

    public JdkHttpGetter(HttpClient httpClient) {
        this(httpClient, Map.of());
    }

    public JdkHttpGetter(HttpClient httpClient, Map<String, String> extraHeaders) {
        this.httpClient = httpClient;
        this.extraHeaders = Map.copyOf(extraHeaders);
    }

    @Override
    public HttpResult get(URI uri) throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .header("User-Agent", "countries-mcp/0.1.0")
                .GET();
        extraHeaders.forEach(request::header);
        HttpResponse<String> response = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
        return new HttpResult(response.statusCode(), response.body());
    }
}
