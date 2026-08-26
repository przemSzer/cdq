# CDQ AI assistant

Java assistant with a chat UI. It answers country and weather questions through MCP tools, and CDQ Fraud Guard questions through RAG over pgvector.

It uses Langchain4J and implements countries MCP with java jdk for MCP.

## Modules 

## Countries-mcp

The MCP server, which exposes two tools, for finding capital by country and country by capital.

## Rag

Helper module, which populates the vector db, with data gathered from CDQ page.
Contains cached docs (chunks), which can embedded and send to db.

### Assistant

Implements the assistant service, wiring MCPs (country,weather) and retrieval.
The service:
- Starts weather MCP as a process in OS, and communicates with it via stdio.
- connects to countries MCP
- connects to pg vector
- uses qwen as LLM,
- exposes /api/chat endpoint, which starts the assistants inference task

## Requirements

In order to start an application you need the following:

- JDK 25
- Maven Wrapper (`mvnw` / `mvnw.cmd` at the repo root)
- [Ollama](https://ollama.com/) with:
  - `qwen3:4b` (chat)
  - `nomic-embed-text` (embeddings)
- [Node.js](https://nodejs.org/) 18+ (for weather MCP)
- Docker (pgvector)
- `WEATHER_API_KEY` for [WeatherAPI](https://www.weatherapi.com/)
- `REST_COUNTRIES_API_KEY` for [REST Countries](https://restcountries.com/)

## Run the services

### 1. Pull models with Ollama

```powershell
ollama pull qwen3:4b
ollama pull nomic-embed-text
```

### 2. Postgres / pgvector

Start the vector database with Docker Compose (`pgvector/pgvector:pg17`). Wait until the health check is green.

```powershell
docker compose up -d
```

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
### 4. Countries MCP
Execute commands, replacing: ... with API key.

```powershell
$env:REST_COUNTRIES_API_KEY = "..."
.\mvnw.cmd -pl countries-mcp exec:java
```

Listens on `http://localhost:8081/mcp`.

### 5. Assistant

The weather MCP is vendored under [assistant/mcp-weather](assistant/mcp-weather). The assistant starts it with `node` over stdio. If Maven cannot resolve the `rag` module, run `.\mvnw.cmd -pl rag install -DskipTests` first.
Replace `...` in first command with real api key.

```powershell
$env:WEATHER_API_KEY = "..."
.\mvnw.cmd -pl assistant spring-boot:run
```
### Open chat ui
Go to http://localhost:8080, you should see a simple web page.

## Demo questions

- What is the capital of Germany?
- What is the temperature in Munich?
- What is the temperature of Germany’s capital?
- What do you know about Berlin?

- What is CDQ Fraud Guard and what does it do?

Recorded answer:

> CDQ Fraud Guard is a service that helps businesses verify global payment data to prevent fraud. It checks bank account information against a shared database of validated accounts and known fraud cases, assigns customizable trust scores, provides real-time fraud alerts, and enables community-driven fraud case management to ensure secure and compliant transactions.

- Are there any clients, which uses CDQ software? What are their opinions?

> Yes, Clariant uses CDQ Fraud Guard. Their Global Process Expert, Arnab Kundu, states that implementing CDQ Trust Score reduced business partner onboarding time from one month to a more efficient process with green or yellow trust scores, eliminating additional documentation.
 

## Tests

Default tests do not call Ollama, OpenAI, or Postgres.

```powershell
.\mvnw.cmd test
```

## Optional overrides

**Re-chunk the CDQ product page**
To re-download and re-chunk the product page, delete the JSON under `rag/ingest-cache` (or point `RAG_INGEST_CACHE_DIR` env. variable at an empty directory) and set `OPENAI_API_KEY`.

This will create a LLM client (Chat GPT-5.4-mini) with download web page tool, and will use it to download a given page, and LLM will process it and remove unnecessary markup. After that it will create chunks, which later will be embedded and placed in pgvector. 

**Upstream TypeScript weather MCP** instead of the vendored script: clone [semdin/mcp-weather](https://github.com/semdin/mcp-weather) and point the assistant at it, for example:

```powershell
$env:WEATHER_MCP_DIR = "C:\path\to\mcp-weather"
```

You would also need to set `assistant.weather.command` / `assistant.weather.script` (`npx` + `tsx` + `src/index.ts`) to match that server.
