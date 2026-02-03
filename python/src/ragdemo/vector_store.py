"""
Vector store module.

RAG Flow Step 3: Store
- OpenAIEmbeddings: Converts text to vector embeddings
- PGVector: Stores embeddings in Supabase/PostgreSQL for persistent similarity search

This module uses Supabase (PostgreSQL with pgvector extension) for storage,
allowing the vector store to be shared between Java and Python applications.
"""

import os
from langchain_openai import OpenAIEmbeddings
from langchain_postgres import PGVector


def get_connection_string() -> str:
    """
    Build PostgreSQL connection string from environment variables.

    Expected environment variables:
    - SUPABASE_HOST: Database host (e.g., aws-0-us-east-1.pooler.supabase.com)
    - SUPABASE_PORT: Database port (default: 6543 for Supabase pooler)
    - SUPABASE_DATABASE: Database name (default: postgres)
    - SUPABASE_USER: Database user (default: postgres.your-project-ref)
    - SUPABASE_PASSWORD: Database password
    """
    host = os.getenv("SUPABASE_HOST", "localhost")
    port = os.getenv("SUPABASE_PORT", "5432")
    database = os.getenv("SUPABASE_DATABASE", "postgres")
    user = os.getenv("SUPABASE_USER", "postgres")
    password = os.getenv("SUPABASE_PASSWORD", "")

    return f"postgresql+psycopg://{user}:{password}@{host}:{port}/{database}"


def create_vector_store(
    chunks: list | None = None,
    model: str = "text-embedding-3-small",
    collection_name: str = "vector_store",
) -> PGVector:
    """
    Create or connect to a PGVector store in Supabase.

    Uses the same table as the Java implementation, enabling cross-language
    interoperability. Both apps use the same embedding model (text-embedding-3-small)
    so vectors are compatible.

    Args:
        chunks: Optional list of Document objects to add. If None, just connects
                to existing store.
        model: OpenAI embedding model to use (must match Java config)
        collection_name: Table name in PostgreSQL (must match Java config)

    Returns:
        PGVector store instance
    """
    embeddings = OpenAIEmbeddings(model=model)
    connection_string = get_connection_string()

    # Connect to or create the vector store
    vector_store = PGVector(
        embeddings=embeddings,
        collection_name=collection_name,
        connection=connection_string,
        use_jsonb=True,  # Store metadata as JSONB for efficient filtering
    )

    # Add documents if provided
    if chunks:
        # Filter out duplicates before adding
        new_chunks = filter_existing_documents(vector_store, chunks)
        if new_chunks:
            vector_store.add_documents(new_chunks)
            print(f"Added {len(new_chunks)} new documents to vector store")
        else:
            print("All documents already exist in vector store, skipping")
    else:
        print("Connected to existing vector store")

    return vector_store


def filter_existing_documents(vector_store: PGVector, chunks: list) -> list:
    """
    Filter out documents that already exist in the vector store.

    Uses source metadata to detect duplicates. This allows both Java and Python
    apps to load documents without creating duplicates.

    Args:
        vector_store: The PGVector store to check against
        chunks: List of Document objects to filter

    Returns:
        List of Document objects that don't already exist
    """
    # Get unique sources from the chunks we want to add
    sources_to_add = {chunk.metadata.get("source") for chunk in chunks if chunk.metadata.get("source")}

    if not sources_to_add:
        return chunks

    # Check which sources already exist
    existing_sources = set()
    for source in sources_to_add:
        try:
            # Query for documents with this source
            results = vector_store.similarity_search(
                query="test",  # Minimal query
                k=1,
                filter={"source": source},
            )
            if results:
                existing_sources.add(source)
                print(f"Document '{source}' already exists, skipping")
        except Exception:
            # If filter fails, assume source doesn't exist
            pass

    # Filter out chunks from existing sources
    new_chunks = [
        chunk for chunk in chunks
        if chunk.metadata.get("source") not in existing_sources
    ]

    return new_chunks


def document_exists(vector_store: PGVector, source: str) -> bool:
    """
    Check if a document with the given source exists in the vector store.

    Args:
        vector_store: The PGVector store to check
        source: The source filename to look for

    Returns:
        True if documents from this source exist
    """
    try:
        results = vector_store.similarity_search(
            query="test",
            k=1,
            filter={"source": source},
        )
        return len(results) > 0
    except Exception:
        return False
