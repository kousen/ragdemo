"""Tests for vector store module."""

import pytest
from unittest.mock import patch
from langchain_core.documents import Document


class TestVectorStore:
    """Tests for the vector store module."""

    def test_create_vector_store_with_mock_embeddings(self, mock_embeddings):
        """Test vector store creation with mock embeddings."""
        from ragdemo.vector_store import create_vector_store

        # Create sample documents
        chunks = [
            Document(page_content="The Transformer architecture uses self-attention."),
            Document(page_content="Attention mechanisms allow the model to focus on relevant parts."),
        ]

        with patch('ragdemo.vector_store.OpenAIEmbeddings', return_value=mock_embeddings):
            vector_store = create_vector_store(chunks)

        assert vector_store is not None

    def test_create_vector_store_calls_embeddings(self, mock_embeddings):
        """Test that creating vector store embeds the documents."""
        from ragdemo.vector_store import create_vector_store

        chunks = [
            Document(page_content="Test document 1"),
            Document(page_content="Test document 2"),
        ]

        with patch('ragdemo.vector_store.OpenAIEmbeddings', return_value=mock_embeddings):
            create_vector_store(chunks)

        # Verify embeddings were called
        mock_embeddings.embed_documents.assert_called()

    @pytest.mark.skipif(
        not pytest.importorskip("langchain_openai", reason="langchain-openai not installed"),
        reason="langchain-openai not installed"
    )
    def test_vector_store_returns_retriever(self, mock_embeddings):
        """Test that vector store can create a retriever."""
        from ragdemo.vector_store import create_vector_store

        chunks = [
            Document(page_content="The Transformer model"),
        ]

        with patch('ragdemo.vector_store.OpenAIEmbeddings', return_value=mock_embeddings):
            vector_store = create_vector_store(chunks)
            retriever = vector_store.as_retriever()

        assert retriever is not None
