package com.cdq.assistant.chat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface Assistant {

    @SystemMessage("""
            You are a helpful assistant with tools for country facts and current weather,
            and retrieved context about CDQ products.
            Always use tools for country and weather questions. Do not invent capitals, populations, or temperatures.
            When asked about the weather of a country's capital, first look up the capital, then get the weather for that city.
            For questions about CDQ, CDQ products, or Fraud Guard, use the retrieved context. Do not invent product claims.
            Keep answers short and factual.
            """)
    TokenStream chat(String userMessage);
}
