"""
Vector store module.

RAG Flow Step 3: Store
- OpenAIEmbeddings: Converts text to vector embeddings
- InMemoryVectorStore: Stores embeddings for similarity search
"""

from langchain_core.vectorstores import InMemoryVectorStore
from langchain_openai import OpenAIEmbeddings


def create_vector_store(
    chunks: list,
    model: str = "text-embedding-3-small",
) -> InMemoryVectorStore:
    """
    Create a vector store from document chunks.

    The InMemoryVectorStore is suitable for demos and small datasets.
    For production, consider Chroma, PGVector, or Redis.

    Args:
        chunks: List of Document objects to embed and store
        model: OpenAI embedding model to use

    Returns:
        Populated InMemoryVectorStore
    """
    # Create embeddings model
    embeddings = OpenAIEmbeddings(model=model)

    # Create vector store and add documents
    # This automatically embeds each chunk
    vector_store = InMemoryVectorStore.from_documents(
        documents=chunks,
        embedding=embeddings,
    )
    print(f"Created vector store with {len(chunks)} documents")

    return vector_store
