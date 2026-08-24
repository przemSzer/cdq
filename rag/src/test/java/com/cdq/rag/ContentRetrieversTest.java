package com.cdq.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;

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
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;

@ExtendWith(MockitoExtension.class)
class ContentRetrieversTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private EmbeddingStore<TextSegment> embeddingStore;

    @Captor
    private ArgumentCaptor<EmbeddingSearchRequest> searchRequestCaptor;

    @Test
    void retrieveEmbedsQueryAndReadsFromStore() {
        Embedding queryEmbedding = Embedding.from(new float[] {0.1f, 0.2f});
        given(embeddingModel.embed("What is CDQ Fraud Guard?")).willReturn(Response.from(queryEmbedding));
        TextSegment segment = TextSegment.from("CDQ Fraud Guard checks business partners.");
        given(embeddingStore.search(any(EmbeddingSearchRequest.class)))
                .willReturn(new EmbeddingSearchResult<>(List.of(
                        new EmbeddingMatch<>(0.91, "chunk-1", queryEmbedding, segment))));

        ContentRetriever retriever = ContentRetrievers.embeddingStore(embeddingStore, embeddingModel, 4);
        List<Content> contents = retriever.retrieve(Query.from("What is CDQ Fraud Guard?"));

        then(embeddingStore).should().search(searchRequestCaptor.capture());
        assertEquals(4, searchRequestCaptor.getValue().maxResults());
        assertEquals("CDQ Fraud Guard checks business partners.", contents.getFirst().textSegment().text());
    }
}
