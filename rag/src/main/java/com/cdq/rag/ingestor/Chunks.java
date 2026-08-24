package com.cdq.rag.ingestor;

import java.util.List;

public record Chunks(List<Chunk> chunks) {

    public Chunks {
        chunks = chunks == null ? List.of() : List.copyOf(chunks);
    }
}
