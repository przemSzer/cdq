package com.cdq.rag.ingestor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class ChunkCache {

    private static final String ILLEGAL_FILE_CHARS = "[<>:\"/\\\\|?*\\p{Cntrl}]";

    private final Path cacheDir;
    private final ObjectMapper objectMapper;

    public ChunkCache(Path cacheDir) {
        this(cacheDir, new ObjectMapper());
    }

    ChunkCache(Path cacheDir, ObjectMapper objectMapper) {
        this.cacheDir = cacheDir;
        this.objectMapper = objectMapper;
    }

    public Optional<Chunks> read(String url) {
        Path file = fileFor(url);
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(file.toFile(), Chunks.class));
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to read chunk cache " + file, ex);
        }
    }

    public void write(String url, Chunks chunks) {
        Path file = fileFor(url);
        try {
            Files.createDirectories(cacheDir);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), chunks);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to write chunk cache " + file, ex);
        }
    }

    Path fileFor(String url) {
        return cacheDir.resolve(fileNameFor(url));
    }

    static String fileNameFor(String url) {
        String sanitized = url.replaceAll(ILLEGAL_FILE_CHARS, "_");
        if (sanitized.isBlank()) {
            sanitized = "chunks";
        }
        return sanitized + ".json";
    }
}
