package com.cdq.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;

public final class ContentRetrievers {

    public static final int DEFAULT_MAX_RESULTS = 4;
    public static final double DEFAULT_MIN_SCORE = 0.8;

    public static ContentRetriever create(RagProperties properties) {
        return create(
                EmbeddingStores.pgVector(properties, false),
                EmbeddingStores.ollama(properties),
                DEFAULT_MAX_RESULTS,
                DEFAULT_MIN_SCORE
        );
    }

    public static ContentRetriever create(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel,
            int maxResults,
            double minScore) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
    }

    private ContentRetrievers() {
    }
}
