"""Tests for RAG chain module."""

from unittest.mock import patch
from langchain_core.documents import Document


class TestRagChain:
    """Tests for the RAG chain module."""

    def test_format_docs(self):
        """Test document formatting function."""
        from ragdemo.rag_chain import format_docs

        docs = [
            Document(page_content="First document"),
            Document(page_content="Second document"),
        ]

        result = format_docs(docs)

        assert "First document" in result
        assert "Second document" in result
        assert "\n\n" in result  # Documents should be separated

    def test_format_docs_empty_list(self):
        """Test formatting empty document list."""
        from ragdemo.rag_chain import format_docs

        result = format_docs([])

        assert result == ""

    def test_create_rag_chain_returns_runnable(self, mock_embeddings, mock_llm):
        """Test that create_rag_chain returns a runnable chain."""
        from ragdemo.rag_chain import create_rag_chain
        from ragdemo.vector_store import create_vector_store

        chunks = [
            Document(page_content="The Transformer uses attention."),
        ]

        with patch('ragdemo.vector_store.OpenAIEmbeddings', return_value=mock_embeddings):
            vector_store = create_vector_store(chunks)

        with patch('ragdemo.rag_chain.ChatOpenAI', return_value=mock_llm):
            chain = create_rag_chain(vector_store)

        assert chain is not None
        assert hasattr(chain, 'invoke')

    def test_rag_template_contains_placeholders(self):
        """Test that RAG template has required placeholders."""
        from ragdemo.rag_chain import RAG_TEMPLATE

        assert "{context}" in RAG_TEMPLATE
        assert "{question}" in RAG_TEMPLATE

    def test_create_rag_chain_with_custom_k(self, mock_embeddings, mock_llm):
        """Test creating RAG chain with custom retrieval count."""
        from ragdemo.rag_chain import create_rag_chain
        from ragdemo.vector_store import create_vector_store

        chunks = [
            Document(page_content=f"Document {i}") for i in range(10)
        ]

        with patch('ragdemo.vector_store.OpenAIEmbeddings', return_value=mock_embeddings):
            vector_store = create_vector_store(chunks)

        with patch('ragdemo.rag_chain.ChatOpenAI', return_value=mock_llm):
            chain = create_rag_chain(vector_store, k=2)

        assert chain is not None
