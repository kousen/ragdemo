# RAG Demo — Java (LangChain4j)

A Retrieval-Augmented Generation demo using LangChain4j 1.10.0.

## Requirements

- Java 21 or higher
- OpenAI API key

## Quick Start

```bash
export OPENAI_API_KEY=sk-...
./gradlew run
```

## Production Track

For persistence with Supabase + pgvector (instead of in-memory storage), use the `supabase-pgvector` branch at the repository root:

```bash
git switch supabase-pgvector
```

## Key Components

| Class | Purpose |
|---|---|
| `RagDemo` | Plain Java CLI entry point and interactive loop |
| `DocumentLoader` | Loads PDF content (Apache Tika) |
| `RagService` | RAG pipeline (ingest, retrieve, generate) |

## How It Works

This implementation keeps the flow explicit in plain Java:
1. Load a PDF from `src/main/resources/documents/`
2. Split into chunks
3. Embed chunks and store in an in-memory embedding store
4. Retrieve relevant chunks for each question
5. Generate answers with the LLM

## Adding Documents

Drop PDF files into `src/main/resources/documents/` and restart the app.

## Running Tests

```bash
./gradlew test
```

## Dependencies

- LangChain4j 1.10.0
- OpenAI (gpt-5-mini, text-embedding-3-small)

See `build.gradle.kts` for the full dependency list.
