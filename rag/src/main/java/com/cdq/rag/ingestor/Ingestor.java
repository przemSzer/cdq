package com.cdq.rag.ingestor;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.service.Result;

public class Ingestor {

    private static final Logger logger = LoggerFactory.getLogger(Ingestor.class);

    private final LLMBasedChunker chunker;
    private final ChunkCache cache;

    public Ingestor(LLMBasedChunker chunker, Path cacheDir) {
        this(chunker, new ChunkCache(cacheDir));
    }

    Ingestor(LLMBasedChunker chunker, ChunkCache cache) {
        this.chunker = chunker;
        this.cache = cache;
    }

    public List<Chunk> ingest(String url) {
        Optional<Chunks> cached = cache.read(url);
        if (cached.isPresent()) {
            logger.info("Using cached chunks for {}", url);
            return cached.get().chunks();
        }
        logger.info("Chunking {}", url);
        Result<Chunks> result = chunker.performChunking(url);
        Chunks chunks = result.content() == null ? new Chunks(List.of()) : result.content();
        cache.write(url, chunks);
        logger.info("Created {} chunks from {} (tools: {})",
                chunks.chunks().size(), url, result.toolExecutions().size());
        return chunks.chunks();
    }
}
