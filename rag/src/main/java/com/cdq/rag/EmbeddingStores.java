package com.cdq.rag;

import java.time.Duration;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.http.client.jdk.JdkHttpClient;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

public final class EmbeddingStores {

    public static final String TABLE = "fraud_guard";

    public static EmbeddingStore<TextSegment> pgVector(RagProperties properties, boolean dropTableFirst) {
        return PgVectorEmbeddingStore.builder()
                .host(properties.jdbcHost())
                .port(properties.jdbcPort())
                .database(properties.jdbcDatabase())
                .user(properties.jdbcUser())
                .password(properties.jdbcPassword())
                .table(TABLE)
                .dimension(properties.embeddingDimension())
                .createTable(true)
                .dropTableFirst(dropTableFirst)
                .build();
    }

    public static EmbeddingModel ollama(RagProperties properties) {
        return OllamaEmbeddingModel.builder()
                .baseUrl(properties.ollamaBaseUrl())
                .modelName(properties.embeddingModel())
                .timeout(Duration.ofSeconds(60))
                .httpClientBuilder(JdkHttpClient.builder())
                .build();
    }

    private EmbeddingStores() {
    }
}
