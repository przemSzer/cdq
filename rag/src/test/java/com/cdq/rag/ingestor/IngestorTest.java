package com.cdq.rag.ingestor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.langchain4j.service.Result;

@ExtendWith(MockitoExtension.class)
class IngestorTest {

    private static final String URL = "https://www.cdq.com/products/cdq-fraud-guard";

    @TempDir
    Path cacheDir;

    @Mock
    private LLMBasedChunker chunker;

    @Mock
    private Supplier<LLMBasedChunker> chunkerFactory;

    private Ingestor ingestor;

    @BeforeEach
    void setUp() {
        ingestor = new Ingestor(chunkerFactory, cacheDir);
    }

    @Test
    void ingestCallsChunkerAndWritesCacheWhenFileIsMissing() throws Exception {
        Chunk chunk = new Chunk("1", "Fraud Guard", "overview", URL, "CDQ Fraud Guard checks business partners.");
        given(chunkerFactory.get())
                .willReturn(chunker);
        given(chunker.performChunking(URL))
                .willReturn(resultOf(new Chunks(List.of(chunk))));

        List<Chunk> chunks = ingestor.ingest(URL);

        assertEquals(List.of(chunk), chunks);
        then(chunkerFactory)
                .should()
                .get();
        then(chunker)
                .should()
                .performChunking(URL);
        Path cached = cacheDir.resolve(ChunkCache.fileNameFor(URL));
        assertTrue(Files.isRegularFile(cached));
        assertTrue(Files.readString(cached).contains("Fraud Guard"));
    }

    @Test
    void ingestUsesCacheAndDoesNotCallChunkerFactoryWhenFileExists() {
        Chunk chunk = new Chunk("1", "Fraud Guard", "overview", URL, "CDQ Fraud Guard checks business partners.");
        new ChunkCache(cacheDir).write(URL, new Chunks(List.of(chunk)));

        List<Chunk> chunks = ingestor.ingest(URL);

        assertEquals(List.of(chunk), chunks);
        then(chunkerFactory).shouldHaveNoInteractions();
    }

    private static Result<Chunks> resultOf(Chunks chunks) {
        return Result.<Chunks>builder()
                .content(chunks)
                .build();
    }
}
