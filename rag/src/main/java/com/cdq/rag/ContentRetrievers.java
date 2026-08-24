package com.cdq.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;

public final class ContentRetrievers {

    public static final int DEFAULT_MAX_RESULTS = 4;

    public static ContentRetriever embeddingStore(RagProperties properties) {
        return embeddingStore(
                EmbeddingStores.pgVector(properties, false),
                EmbeddingStores.ollama(properties),
                DEFAULT_MAX_RESULTS);
    }

    public static ContentRetriever embeddingStore(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel,
            int maxResults) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .build();
    }

    private ContentRetrievers() {
    }
}
