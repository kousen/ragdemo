# RAG Demo

Parallel implementations of Retrieval-Augmented Generation (RAG) in **Java** (Spring AI) and **Python** (LangChain) for teaching purposes.

Both demos follow the same flow: **Load PDF → Chunk → Embed → Store → Retrieve → Generate**

## Choose Your Implementation

| | Java | Python |
|---|---|---|
| **Framework** | Spring AI 1.0.3 | LangChain |
| **LLM** | OpenAI (gpt-5-mini) | OpenAI (gpt-5-mini) |
| **Embeddings** | text-embedding-3-small | text-embedding-3-small |
| **Vector Store** | SimpleVectorStore (in-memory) | InMemoryVectorStore |
| **PDF Parser** | PagePdfDocumentReader | PyPDFLoader |
| **IDE** | IntelliJ IDEA | PyCharm / VS Code |

## Prerequisites

- **OpenAI API Key** — Set as environment variable `OPENAI_API_KEY`
- **Java 21+** (for Java version)
- **Python 3.10+** (for Python version)

## Quick Start

### Java (Spring AI)

```bash
cd java
export OPENAI_API_KEY=sk-...
./gradlew bootRun
```

### Python (LangChain)

```bash
cd python
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r requirements.txt
export OPENAI_API_KEY=sk-...
python -m ragdemo.main
```

## Project Structure

```
ragdemo/
├── java/
│   ├── build.gradle.kts
│   └── src/main/java/com/kousen/ragdemo/
│       ├── RagDemoApplication.java      # Spring Boot entry point
│       ├── RagDemoRunner.java           # Interactive CLI
│       ├── config/RagConfig.java        # VectorStore + ChatClient beans
│       └── service/
│           ├── DocumentLoaderService.java   # PDF loading + chunking
│           └── RagService.java              # Q&A via ChatClient
│
└── python/
    └── src/ragdemo/
        ├── main.py              # CLI entry point
        ├── document_loader.py   # PDF loading + chunking
        ├── vector_store.py      # Embedding + storage
        └── rag_chain.py         # LCEL chain (retriever → prompt → LLM)
```

## How RAG Works

```
┌─────────────────────────────────────────────────────────────────┐
│                        INDEXING PHASE                           │
├─────────────────────────────────────────────────────────────────┤
│  PDF  ──▶  Load  ──▶  Chunk  ──▶  Embed  ──▶  Vector Store     │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        QUERY PHASE                              │
├─────────────────────────────────────────────────────────────────┤
│  Question ──▶ Embed ──▶ Search ──▶ Retrieve ──▶ Augment ──▶ LLM │
└─────────────────────────────────────────────────────────────────┘
```

1. **Load** — Extract text from PDF documents
2. **Chunk** — Split into smaller pieces (~800 tokens) for better retrieval
3. **Embed** — Convert text chunks to vector embeddings
4. **Store** — Save embeddings in vector store for similarity search
5. **Retrieve** — Find chunks most similar to the user's question
6. **Generate** — Send question + retrieved context to LLM for answer

## Sample Questions

The demo includes the "Attention Is All You Need" paper. Try asking:

- What is the Transformer architecture?
- How does self-attention work?
- What are the key contributions of this paper?
- What is multi-head attention?

## Adding More Documents

Drop additional PDF files into the `documents/` folder:
- Java: `java/src/main/resources/documents/`
- Python: `python/documents/`

The application will automatically load all PDFs on startup.

## Running Tests

### Java
```bash
cd java
./gradlew test
```

### Python
```bash
cd python
pip install -e ".[dev]"
pytest
```

## License

MIT License — See [LICENSE](LICENSE) for details.

## Author

Kenneth Kousen
