package com.cdq.rag.ingestor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ChunkCacheTest {

    @Test
    void fileNameForReplacesCharactersIllegalOnWindows() {
        assertEquals(
                "https___www.cdq.com_products_cdq-fraud-guard.json",
                ChunkCache.fileNameFor("https://www.cdq.com/products/cdq-fraud-guard"));
    }
}
