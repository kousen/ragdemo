# RAG Demo (Supabase/PgVector Branch)

Parallel implementations of Retrieval-Augmented Generation (RAG) in **Java** (Spring AI), **Java** (LangChain4j), and **Python** (LangChain) for teaching purposes.

**This branch uses Supabase (PostgreSQL with pgvector) for persistent vector storage.** All three applications share the same database, demonstrating cross-language interoperability.

Both demos follow the same flow: **Load PDF → Chunk → Embed → Store → Retrieve → Generate**

## Choose Your Implementation

| | Java (Spring AI) | Java (LangChain4j) | Python |
|---|---|---|---|
| **Framework** | Spring AI 1.0.3 | LangChain4j 1.10.0 | LangChain |
| **LLM** | OpenAI (gpt-5-mini) | OpenAI (gpt-5-mini) | OpenAI (gpt-5-mini) |
| **Embeddings** | text-embedding-3-small | text-embedding-3-small | text-embedding-3-small |
| **Vector Store** | PgVectorStore (Supabase) | PgVectorEmbeddingStore (Supabase) | PGVector (Supabase) |
| **PDF Parser** | PagePdfDocumentReader | Apache Tika | PyPDFLoader |
| **IDE** | IntelliJ IDEA | IntelliJ IDEA | PyCharm / VS Code |

## Prerequisites

- **OpenAI API Key** — Set as environment variable `OPENAI_API_KEY`
- **Supabase Account** — Free tier at https://supabase.com
- **Java 21+** (for Java versions)
- **Python 3.10+** (for Python version)

## Supabase Setup

1. **Create a Supabase project** at https://supabase.com
2. **Enable pgvector extension**:
   - Go to **Database** → **Extensions**
   - Search for `vector` and enable it
3. **Get connection credentials**:
   - Go to **Settings** → **Database**
   - Copy the connection details from "Connection string" → "URI"

Set these environment variables (both apps use the same credentials):

```bash
export SUPABASE_HOST=aws-0-us-east-1.pooler.supabase.com
export SUPABASE_PORT=6543
export SUPABASE_DATABASE=postgres
export SUPABASE_USER=postgres.your-project-ref
export SUPABASE_PASSWORD=your-database-password
```

> **Tip:** Add these to a `.env` file and `source .env` before running any implementation.

## Quick Start

### Java (Spring AI)

```bash
cd java
# Set environment variables (or use .env file)
export OPENAI_API_KEY=sk-...
export SUPABASE_HOST=aws-0-us-east-1.pooler.supabase.com
export SUPABASE_PORT=6543
export SUPABASE_USER=postgres.your-project-ref
export SUPABASE_PASSWORD=your-database-password

./gradlew bootRun
```

### Java (LangChain4j)

```bash
cd langchain4j
source .env   # or export env vars manually
./gradlew run
```

### Python (LangChain)

```bash
cd python
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -e .

# Set environment variables (or use .env file)
export OPENAI_API_KEY=sk-...
export SUPABASE_HOST=aws-0-us-east-1.pooler.supabase.com
export SUPABASE_PORT=6543
export SUPABASE_USER=postgres.your-project-ref
export SUPABASE_PASSWORD=your-database-password

python -m ragdemo.main
```

## Cross-Language Interoperability

All three applications share the same Supabase vector store. This means:

- **Documents loaded by any implementation are searchable from the others**
- **Duplicate detection** prevents re-loading the same PDFs
- Try it: Load documents with Spring AI, then query them from LangChain4j or Python!

This works because all three use the same:
- Embedding model: `text-embedding-3-small` (1536 dimensions)
- Table name: `vector_store`
- Metadata schema (source, page, etc.)

## Project Structure

```
ragdemo/
├── java/                            # Spring AI implementation
│   ├── build.gradle.kts
│   └── src/main/java/edu/trincoll/ragdemo/
│       ├── RagDemoApplication.java      # Spring Boot entry point
│       ├── RagDemoRunner.java           # Interactive CLI
│       ├── config/RagConfig.java        # VectorStore + ChatClient beans
│       └── service/
│           ├── DocumentLoaderService.java   # PDF loading + chunking
│           └── RagService.java              # Q&A via ChatClient
│
├── langchain4j/                     # LangChain4j implementation (no Spring)
│   ├── build.gradle.kts
│   └── src/main/java/edu/trincoll/ragdemo/
│       ├── RagDemo.java                 # Plain Java CLI entry point
│       ├── RagService.java              # RAG pipeline + PgVectorEmbeddingStore
│       └── DocumentLoader.java          # PDF loading via Apache Tika
│
└── python/                          # Python/LangChain implementation
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
- Java (Spring AI): `java/src/main/resources/documents/`
- Java (LangChain4j): `langchain4j/src/main/resources/documents/`
- Python: `python/documents/`

The application will automatically load all PDFs on startup.

## Running Tests

### Java (Spring AI)
```bash
cd java
./gradlew test
```

### Java (LangChain4j)
```bash
cd langchain4j
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
