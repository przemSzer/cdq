package com.cdq.rag.ingestor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.cdq.rag.RagProperties;

class ChunkCacheTest {

    @Test
    void fileNameForReplacesCharactersIllegalOnWindows() {
        assertEquals(
                "https___www.cdq.com_products_cdq-fraud-guard.json",
                ChunkCache.fileNameFor("https://www.cdq.com/products/cdq-fraud-guard"));
    }

    @Test
    void committedFraudGuardCacheCanBeRead() {
        Path cacheDir = Path.of("ingest-cache");
        assumeTrue(Files.isDirectory(cacheDir), "committed ingest-cache should exist when tests run from the rag module");

        Optional<Chunks> chunks = new ChunkCache(cacheDir).read(RagProperties.DEFAULT_SOURCE_URL);

        assertTrue(chunks.isPresent());
        assertFalse(chunks.get().chunks().isEmpty());
    }
}
