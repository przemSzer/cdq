package com.cdq.assistant.chat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface Assistant {

    @SystemMessage(fromResource = "prompts/assistant-system-message.md")
    TokenStream answer(String userMessage);
}
