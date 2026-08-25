package com.cdq.rag.ingestor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;

@ExtendWith(MockitoExtension.class)
class ChunkVectorWriterTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private EmbeddingStore<TextSegment> embeddingStore;

    @Captor
    private ArgumentCaptor<TextSegment> segmentCaptor;

    private ChunkVectorWriter writer;

    @BeforeEach
    void setUp() {
        writer = new ChunkVectorWriter(embeddingModel, embeddingStore);
    }

    @Test
    void storeEmbedsChunksAndAddsThemToTheVectorStore() {
        Chunk chunk = new Chunk(
                "1",
                "Fraud Guard",
                "overview",
                "https://www.cdq.com/products/cdq-fraud-guard",
                "CDQ Fraud Guard checks business partners.");
        Embedding embedding = Embedding.from(new float[] {0.1f, 0.2f});
        given(embeddingModel.embed(anyString())).willReturn(Response.from(embedding));

        int stored = writer.store(List.of(chunk));

        assertEquals(1, stored);
        then(embeddingStore).should().add(eq(embedding), segmentCaptor.capture());
        TextSegment segment = segmentCaptor.getValue();
        assertEquals("Fraud Guard\n\nCDQ Fraud Guard checks business partners.", segment.text());
        assertEquals("1", segment.metadata().getString("chunkId"));
        assertEquals("https://www.cdq.com/products/cdq-fraud-guard", segment.metadata().getString("url"));
    }

    @Test
    void storeSkipsChunksWithoutContent() {
        Chunk empty = new Chunk("2", "Empty", "n/a", "https://example", "  ");

        int stored = writer.store(List.of(empty));

        assertEquals(0, stored);
        then(embeddingModel).shouldHaveNoInteractions();
        then(embeddingStore).shouldHaveNoInteractions();
    }
}
