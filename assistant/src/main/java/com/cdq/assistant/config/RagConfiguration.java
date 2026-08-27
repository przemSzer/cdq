package com.cdq.assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cdq.rag.ContentRetrievers;
import com.cdq.rag.EmbeddingStores;

import dev.langchain4j.rag.content.retriever.ContentRetriever;

@Configuration
public class RagConfiguration {

    @Bean
    ContentRetriever contentRetriever(AssistantProperties properties) {
        AssistantProperties.Rag rag = properties.rag();
        return ContentRetrievers.create(
                EmbeddingStores.pgVector(rag.toRagProperties(), false),
                EmbeddingStores.ollama(rag.toRagProperties()),
                rag.maxResults(),
                rag.minScore()
        );
    }
}
