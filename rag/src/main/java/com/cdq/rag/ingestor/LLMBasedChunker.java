package com.cdq.rag.ingestor;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;

public interface LLMBasedChunker {

    @SystemMessage(fromResource = "com/cdq/rag/ingestor/ingestor-system-message.md")
    Result<Chunks> performChunking(String message);
}