package com.cdq.rag.ingestor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
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
    private ArgumentCaptor<List<Embedding>> embeddingsCaptor;

    @Captor
    private ArgumentCaptor<List<TextSegment>> segmentsCaptor;

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
        given(embeddingModel.embedAll(anyList())).willReturn(Response.from(List.of(embedding)));

        int stored = writer.store(List.of(chunk));

        assertEquals(1, stored);
        then(embeddingStore).should().addAll(embeddingsCaptor.capture(), segmentsCaptor.capture());
        TextSegment segment = segmentsCaptor.getValue().getFirst();
        assertEquals("Fraud Guard\n\nCDQ Fraud Guard checks business partners.", segment.text());
        assertEquals("1", segment.metadata().getString("chunkId"));
        assertEquals("https://www.cdq.com/products/cdq-fraud-guard", segment.metadata().getString("url"));
        assertEquals(List.of(embedding), embeddingsCaptor.getValue());
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
