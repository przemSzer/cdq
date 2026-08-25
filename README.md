# CDQ AI assistant

Java assistant with a chat UI. It answers country and weather questions through MCP tools, and CDQ Fraud Guard questions through RAG over pgvector.

## Requirements

In order to start an application you need the following:

- JDK 25
- Maven Wrapper (`mvnw` / `mvnw.cmd` at the repo root)
- [Ollama](https://ollama.com/) with:
  - `qwen3:4b` (chat)
  - `nomic-embed-text` (embeddings)
- Node.js / `npx` (weather MCP)
- Docker (pgvector)
- `WEATHER_API_KEY` for [WeatherAPI](https://www.weatherapi.com/)
- `REST_COUNTRIES_API_KEY` for countries-mcp
- `OPENAI_API_KEY` only if you re-chunk the CDQ page (the committed ingest cache is used by default)
- `WEATHER_MCP_DIR` pointing at a local clone of [mcp-weather](https://github.com/semdin/mcp-weather)

## Run the services

### 1. Pull Models with Ollama

```powershell
ollama pull qwen3:4b
ollama pull nomic-embed-text
```

### 2. Postgres / pgvector

Start the vector database with Docker Compose (`pgvector/pgvector:pg17`). Wait until the health check is green.

```powershell
docker compose up -d
```

- JDBC URL: `jdbc:postgresql://localhost:5432/rag`
- User / password: `rag` / `rag`
- Table: `fraud_guard` (created on first ingest)

Check that Postgres is up:

```powershell
docker compose exec pgvector psql -U rag -d rag -c "SELECT 1;"
```

### 3. Ingest CDQ Fraud Guard (once)

Needs Ollama with `nomic-embed-text` and a running pgvector. 
Chunks are already in [rag/ingest-cache](rag/ingest-cache); ingest embeds them and writes table `fraud_guard`.

```powershell
.\mvnw.cmd -pl rag exec:java
```

Re-running the same command drops `fraud_guard` and writes the embeddings again.

Useful checks after importing:

```powershell
docker compose exec pgvector psql -U rag -d rag -c "SELECT COUNT(*) FROM fraud_guard;
```

#### Skipping cached chunks

This step can be skipped.

To re-download and re-chunk the product page, delete the JSON under `rag/ingest-cache` (or point `RAG_INGEST_CACHE_DIR` env. variable at an empty directory) and set `OPENAI_API_KEY`.

This will create a LLM client (Chat GPT-5.4-mini) with download web page tool, and will use it to download a given page, and LLM will process it and remove unnecessary markup. After that it will create chunks, which later will be embedded and placed in pgvector. 

### 4. Countries MCP

```powershell
$env:REST_COUNTRIES_API_KEY = "..."
.\mvnw.cmd -pl countries-mcp exec:java
```

Listens on `http://localhost:8081/mcp`.

### 5. Assistant

```powershell
$env:WEATHER_API_KEY = "..."
$env:WEATHER_MCP_DIR = "C:\path\to\mcp-weather"
.\mvnw.cmd -pl assistant spring-boot:run
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
