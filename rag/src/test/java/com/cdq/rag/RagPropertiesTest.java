package com.cdq.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RagPropertiesTest {

    @TempDir
    Path cwd;

    @Test
    void resolveIngestCacheDirPrefersModuleIngestCache() throws Exception {
        Path moduleCache = cwd.resolve("ingest-cache");
        Files.createDirectory(moduleCache);

        assertEquals(moduleCache.toAbsolutePath(), RagProperties.resolveIngestCacheDir(cwd));
    }

    @Test
    void resolveIngestCacheDirPrefersRepoIngestCacheWhenCwdIsRepoRoot() throws Exception {
        Path repoCache = cwd.resolve("rag").resolve("ingest-cache");
        Files.createDirectories(repoCache);

        assertEquals(repoCache.toAbsolutePath(), RagProperties.resolveIngestCacheDir(cwd));
    }

    @Test
    void resolveIngestCacheDirFallsBackToTempWhenNoCommittedCacheExists() {
        Path expected = Path.of(System.getProperty("java.io.tmpdir"), "ingestor");

        assertEquals(expected, RagProperties.resolveIngestCacheDir(cwd));
    }
}
