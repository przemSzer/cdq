package com.cdq.countries;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import io.modelcontextprotocol.json.McpJsonMapper;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cdq.countries.rest.CountryResponseFormatter;
import com.cdq.countries.rest.JdkHttpGetter;
import com.cdq.countries.rest.RestCountriesClient;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;

public final class CountriesMcpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountriesMcpServer.class);

    static final int DEFAULT_PORT = 8081;

    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting countries-mcp");
        var properties = CountriesMCPProperties.fromArgs(args);
        var jsonMapper = McpJsonDefaults.getMapper();
        var countriesClient = createRestCountriesClient(properties, jsonMapper);
        var tools = new CountryToolsSpecificationProvider(countriesClient);

        var transport = createTransport(jsonMapper);

        McpServer.sync(transport)
                .serverInfo("countries-mcp", "0.1.0")
                .capabilities(ServerCapabilities.builder().tools(true).build())
                .tools(tools.specifications())
                .build();

        Server server = createJettyServer(properties.port(), transport);

        setupShutdownHook(server);

        server.start();
        LOGGER.info("countries-mcp listening on http://localhost:{}/mcp ({})", properties.port(), properties.baseUrl());
        server.join();
    }

    private static @NonNull Server createJettyServer(int port, HttpServletStreamableServerTransportProvider transport) {
        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        ServletHolder holder = new ServletHolder(transport);
        holder.setAsyncSupported(true);
        context.addServlet(holder, "/*");
        server.setHandler(context);
        return server;
    }

    private static void setupShutdownHook(Server server) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOGGER.info("Shutting down countries-mcp");
                server.stop();
            } catch (Exception ex) {
                LOGGER.warn("Error shutting down countries-mcp: {}", ex.getMessage());
            }
        }));
    }

    private static HttpServletStreamableServerTransportProvider createTransport(McpJsonMapper jsonMapper) {
        return HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(jsonMapper)
                .mcpEndpoint("/mcp")
                .build();
    }

    private static @NonNull RestCountriesClient createRestCountriesClient(CountriesMCPProperties properties, McpJsonMapper jsonMapper) {
        return new RestCountriesClient(
                new JdkHttpGetter(
                        HttpClient.newBuilder()
                                .connectTimeout(Duration.ofSeconds(10))
                                .followRedirects(HttpClient.Redirect.NORMAL)
                                .build(),
                        Map.of("Authorization", CountriesMCPProperties.bearer(properties.apiKey()))),
                new CountryResponseFormatter(jsonMapper),
                properties.baseUrl()
        );
    }

    static int resolvePort(String[] args) {
        if (args != null && args.length > 0 && !args[0].isBlank()) {
            return Integer.parseInt(args[0]);
        }
        String envPort = System.getenv("COUNTRIES_MCP_PORT");
        if (envPort != null && !envPort.isBlank()) {
            return Integer.parseInt(envPort);
        }
        return DEFAULT_PORT;
    }

    private CountriesMcpServer() {
    }
}
