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
import dev.langchain4j.rag.query.Query;
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
        given(embeddingModel.embed("query"))
                .willReturn(Response.from(Embedding.from(new float[] {0.1f})));
        given(embeddingStore.search(any(EmbeddingSearchRequest.class)))
                .willReturn(new EmbeddingSearchResult<>(List.of()));

        var retriever = ContentRetrievers.create(
                embeddingStore, embeddingModel,
                ContentRetrievers.DEFAULT_MAX_RESULTS,
                ContentRetrievers.DEFAULT_MIN_SCORE
        );
        retriever.retrieve(Query.from("query"));

        then(embeddingStore)
                .should()
                .search(searchRequestCaptor.capture());
        var searchRequest = searchRequestCaptor.getValue();
        assertEquals(ContentRetrievers.DEFAULT_MAX_RESULTS, searchRequest.maxResults());
        assertEquals(ContentRetrievers.DEFAULT_MIN_SCORE, searchRequest.minScore());
    }
}
