package com.cdq.rag.ingestor;

public record Chunk(String chunkId, String title, String section, String url, String content) {
}
