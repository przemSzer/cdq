package com.cdq.rag;

import java.nio.file.Path;

public record RagProperties(
        String jdbcHost,
        int jdbcPort,
        String jdbcDatabase,
        String jdbcUser,
        String jdbcPassword,
        String ollamaBaseUrl,
        String embeddingModel,
        int embeddingDimension,
        Path ingestCacheDir) {

    public static final String DEFAULT_SOURCE_URL = "https://www.cdq.com/products/cdq-fraud-guard";
    public static final String DEFAULT_EMBEDDING_MODEL = "nomic-embed-text";
    public static final int DEFAULT_EMBEDDING_DIMENSION = 768;
    public static final String INGEST_CACHE_DIR_ENV = "RAG_INGEST_CACHE_DIR";

    public static Path defaultIngestCacheDir() {
        return Path.of(System.getProperty("java.io.tmpdir"), "ingestor");
    }

    public static RagProperties load() {
        return new RagProperties(
                envOrDefault("RAG_JDBC_HOST", "localhost"),
                envIntOrDefault("RAG_JDBC_PORT", 5432),
                envOrDefault("RAG_JDBC_DATABASE", "rag"),
                envOrDefault("RAG_JDBC_USER", "rag"),
                envOrDefault("RAG_JDBC_PASSWORD", "rag"),
                envOrDefault("RAG_OLLAMA_BASE_URL", "http://localhost:11434"),
                envOrDefault("RAG_EMBEDDING_MODEL", DEFAULT_EMBEDDING_MODEL),
                envIntOrDefault("RAG_EMBEDDING_DIMENSION", DEFAULT_EMBEDDING_DIMENSION),
                Path.of(envOrDefault(INGEST_CACHE_DIR_ENV, defaultIngestCacheDir().toString())));
    }

    public String jdbcUrl() {
        return "jdbc:postgresql://%s:%d/%s".formatted(jdbcHost, jdbcPort, jdbcDatabase);
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static int envIntOrDefault(String name, int defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }
}