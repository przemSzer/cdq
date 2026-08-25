package com.cdq.rag.ingestor;

import java.time.Duration;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cdq.rag.DownloadWebPageTool;
import com.cdq.rag.EmbeddingStores;
import com.cdq.rag.RagProperties;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String OPENAI_API_KEY_ENV = "OPENAI_API_KEY";
    private static final String CHAT_MODEL = "gpt-5.4-mini";

    public static void main(String[] args) {
        var properties = RagProperties.load();
        var url = urlFromArgsOrDefault(args);
        logger.info("Will use ingest cache: {}", properties.ingestCacheDir().toAbsolutePath());

        var chunks = new Ingestor(Main::createLLMBasedChunker, properties.ingestCacheDir())
                .ingest(url);

        chunks.forEach(chunk -> logger.info(
                "[{}] {} / {} — {}",
                chunk.chunkId(),
                chunk.title(),
                chunk.section(),
                chunk.content()));

        int stored = new ChunkVectorWriter(
                    EmbeddingStores.ollama(properties),
                    EmbeddingStores.pgVector(properties, true)
            )
                .store(chunks);
        logger.info("Stored {} embeddings in {}", stored, EmbeddingStores.TABLE);
    }

    private static @NonNull String urlFromArgsOrDefault(String[] args) {
        return args.length > 0 && !args[0].isBlank()
                ? args[0]
                : RagProperties.DEFAULT_SOURCE_URL;
    }

    private static LLMBasedChunker createLLMBasedChunker() {
        var chatModel = OpenAiChatModel.builder()
                .apiKey(requireOpenAiApiKey())
                .modelName(CHAT_MODEL)
                .timeout(Duration.ofMinutes(3))
                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .strictJsonSchema(true)
                .build();

        return AiServices.builder(LLMBasedChunker.class)
                .chatModel(chatModel)
                .tools(new DownloadWebPageTool())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }

    private static String requireOpenAiApiKey() {
        String apiKey = System.getenv(OPENAI_API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Set " + OPENAI_API_KEY_ENV + " to run ingest chunking.");
        }
        return apiKey.trim();
    }

    private Main() {
    }
}
