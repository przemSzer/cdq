# CDQ AI assistant

Java assistant with a chat UI. It answers country and weather questions through MCP tools, and CDQ Fraud Guard questions through RAG over pgvector.

## Requirements

- JDK 25
- Maven Wrapper (`mvnw` / `mvnw.cmd` at the repo root)
- [Ollama](https://ollama.com/) with:
  - `qwen3:4b` (chat)
  - `nomic-embed-text` (embeddings)
- Node.js / `npx` (weather MCP)
- Docker (pgvector)
- `WEATHER_API_KEY` for [WeatherAPI](https://www.weatherapi.com/)
- `REST_COUNTRIES_API_KEY` for countries-mcp
- `OPENAI_API_KEY` only for the one-shot RAG ingest (chunking)
- `WEATHER_MCP_DIR` pointing at a local clone of [mcp-weather](https://github.com/semdin/mcp-weather)

## Run the services

### 1. Models

```powershell
ollama pull qwen3:4b
ollama pull nomic-embed-text
```

### 2. Postgres / pgvector

```powershell
docker compose up -d
```

JDBC URL: `jdbc:postgresql://localhost:5432/rag`  
User / password: `rag` / `rag`

### 3. Ingest CDQ Fraud Guard (once)

This downloads the product page, chunks it with OpenAI `gpt-5.4-mini`, embeds chunks with Ollama `nomic-embed-text`, and stores them in table `fraud_guard`.

```powershell
$env:OPENAI_API_KEY = "sk-..."
.\mvnw.cmd -pl rag -am exec:java
```

Chunk JSON is cached under `%TEMP%\ingestor` (override with `RAG_INGEST_CACHE_DIR`). Re-running ingest reuses the cache and rewrites the vector table.

### 4. Countries MCP

```powershell
$env:REST_COUNTRIES_API_KEY = "..."
.\mvnw.cmd -pl countries-mcp -am exec:java
```

Listens on `http://localhost:8081/mcp`.

### 5. Assistant

```powershell
$env:WEATHER_API_KEY = "..."
$env:WEATHER_MCP_DIR = "C:\path\to\mcp-weather"
.\mvnw.cmd -pl assistant -am spring-boot:run
```

Chat UI: http://localhost:8080

## Demo questions

Milestone 1:

- What is the capital of Germany?
- What is the temperature in Munich?
- What is the temperature of Germany’s capital?
- What do you know about Berlin?

Milestone 2 (RAG — after ingest):

- What is CDQ Fraud Guard and what does it do?

Recorded answer:

> CDQ Fraud Guard is a service that helps businesses verify global payment data to prevent fraud. It checks bank account information against a shared database of validated accounts and known fraud cases, assigns customizable trust scores, provides real-time fraud alerts, and enables community-driven fraud case management to ensure secure and compliant transactions.

## Tests

Default tests do not call Ollama, OpenAI, or Postgres.

```powershell
.\mvnw.cmd test
```
