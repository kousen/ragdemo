# RAG Demo — Python (LangChain)

A Retrieval-Augmented Generation demo using LangChain.

## Requirements

- Python 3.10 or higher
- OpenAI API key

## Quick Start

```bash
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r requirements.txt
export OPENAI_API_KEY=sk-...
python -m ragdemo.main
```

## Production Track

For persistence with Supabase + pgvector (instead of in-memory storage), use the `supabase-pgvector` branch at the repository root:

```bash
git switch supabase-pgvector
```

## Key Components

| Module               | Purpose                                                                     |
|----------------------|-----------------------------------------------------------------------------|
| `document_loader.py` | Loads PDFs with `PyPDFLoader`, chunks with `RecursiveCharacterTextSplitter` |
| `vector_store.py`    | Creates `InMemoryVectorStore` with `OpenAIEmbeddings`                       |
| `rag_chain.py`       | Builds LCEL chain: retriever → prompt → LLM                                 |
| `main.py`            | Interactive CLI entry point                                                 |

## How It Works

LangChain Expression Language (LCEL) makes the RAG flow explicit:

```python
rag_chain = (
    {"context": retriever | format_docs, "question": RunnablePassthrough()}
    | prompt
    | llm
    | StrOutputParser()
)
```

The chain:
1. Takes a question string as input
2. Passes it to the retriever to find similar chunks
3. Formats retrieved documents into a context string
4. Combines context + question into the prompt template
5. Sends to the LLM and parses the response

## Adding Documents

Drop PDF files into `documents/` — they'll be loaded automatically on startup.

## Running Tests

```bash
pip install -e ".[dev]"
pytest
```

## Dependencies

- langchain / langchain-openai
- pypdf
- OpenAI (gpt-5-mini, text-embedding-3-small)

See [requirements.txt](requirements.txt) or [pyproject.toml](pyproject.toml) for full dependency list.
