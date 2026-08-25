package com.cdq.rag.ingestor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;

public final class ChunkVectorWriter {

    private static final Logger logger = LoggerFactory.getLogger(ChunkVectorWriter.class);

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public ChunkVectorWriter(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    public int store(List<Chunk> chunks) {
        List<TextSegment> segments = chunks.stream()
                .filter(ChunkVectorWriter::hasContent)
                .map(ChunkVectorWriter::toSegment)
                .toList();
        if (segments.isEmpty()) {
            logger.warn("No chunks with content to store");
            return 0;
        }
        for (TextSegment segment : segments) {
            logger.info("Computing embedding for: {}", segment.text());
            Embedding embedding = embeddingModel.embed(segment.text()).content();
            logger.info("Storing embedding in vector db");
            embeddingStore.add(embedding, segment);
        }
        return segments.size();
    }

    static TextSegment toSegment(Chunk chunk) {
        return TextSegment.from(textOf(chunk), metadataOf(chunk));
    }

    private static boolean hasContent(Chunk chunk) {
        return chunk.content() != null && !chunk.content().isBlank();
    }

    private static String textOf(Chunk chunk) {
        if (chunk.title() == null || chunk.title().isBlank()) {
            return chunk.content();
        }
        return chunk.title() + "\n\n" + chunk.content();
    }

    private static Metadata metadataOf(Chunk chunk) {
        Metadata metadata = new Metadata();
        putIfPresent(metadata, "chunkId", chunk.chunkId());
        putIfPresent(metadata, "title", chunk.title());
        putIfPresent(metadata, "section", chunk.section());
        putIfPresent(metadata, "url", chunk.url());
        return metadata;
    }

    private static void putIfPresent(Metadata metadata, String key, String value) {
        if (value != null && !value.isBlank()) {
            metadata.put(key, value);
        }
    }
}
