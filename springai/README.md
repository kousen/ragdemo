# RAG Demo — Java (Spring AI)

A Retrieval-Augmented Generation demo using Spring AI 1.0.3.

## Requirements

- Java 21 or higher
- OpenAI API key

## Quick Start

```bash
export OPENAI_API_KEY=sk-...
./gradlew bootRun
```

## Production Track

For persistence with Supabase + pgvector (instead of in-memory storage), use the `supabase-pgvector` branch at the repository root:

```bash
git switch supabase-pgvector
```

## Key Components

| Class                   | Purpose                                                                      |
|-------------------------|------------------------------------------------------------------------------|
| `RagConfig`             | Configures `SimpleVectorStore` and `ChatClient` with `QuestionAnswerAdvisor` |
| `DocumentLoaderService` | Loads PDFs, chunks them, adds to vector store                                |
| `RagService`            | Handles Q&A via `ChatClient`                                                 |
| `RagDemoRunner`         | Interactive CLI loop                                                         |

## How It Works

Spring AI's `QuestionAnswerAdvisor` handles the RAG pattern automatically:

```java
@Bean
public ChatClient chatClient(ChatClient.Builder builder, VectorStore vectorStore) {
    return builder
            .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
            .build();
}
```

When you call `chatClient.prompt().user(question).call()`, the advisor:
1. Embeds the question
2. Searches the vector store for similar chunks
3. Adds retrieved context to the prompt
4. Sends the augmented prompt to the LLM

## Adding Documents

Drop PDF files into `src/main/resources/documents/` — they'll be loaded automatically on startup.

## Running Tests

```bash
./gradlew test
```

## Dependencies

- Spring Boot 3.5.9
- Spring AI 1.0.3
- OpenAI (gpt-5-mini, text-embedding-3-small)

See [build.gradle.kts](build.gradle.kts) for full dependency list.
