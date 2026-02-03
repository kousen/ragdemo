"""Tests for vector store module."""

import os
import pytest
from unittest.mock import patch, MagicMock
from langchain_core.documents import Document


class TestVectorStore:
    """Tests for the vector store module."""

    def test_get_connection_string_from_env(self):
        """Test connection string generation from environment variables."""
        from ragdemo.vector_store import get_connection_string

        with patch.dict(os.environ, {
            "SUPABASE_HOST": "test-host.supabase.com",
            "SUPABASE_PORT": "6543",
            "SUPABASE_DATABASE": "testdb",
            "SUPABASE_USER": "testuser",
            "SUPABASE_PASSWORD": "testpass",
        }):
            conn_string = get_connection_string()

        assert "test-host.supabase.com" in conn_string
        assert "6543" in conn_string
        assert "testdb" in conn_string
        assert "testuser" in conn_string
        assert "testpass" in conn_string
        assert conn_string.startswith("postgresql+psycopg://")

    def test_get_connection_string_defaults(self):
        """Test connection string uses defaults when env vars missing."""
        from ragdemo.vector_store import get_connection_string

        with patch.dict(os.environ, {}, clear=True):
            conn_string = get_connection_string()

        assert "localhost" in conn_string
        assert "5432" in conn_string
        assert "postgres" in conn_string

    def test_filter_existing_documents_returns_new_only(self):
        """Test that filter_existing_documents removes duplicates."""
        from ragdemo.vector_store import filter_existing_documents

        # Create mock vector store that returns results for "existing.pdf"
        mock_store = MagicMock()
        mock_store.similarity_search.side_effect = lambda query, k, filter: (
            [Document(page_content="exists")] if filter.get("source") == "existing.pdf" else []
        )

        chunks = [
            Document(page_content="Old content", metadata={"source": "existing.pdf"}),
            Document(page_content="New content", metadata={"source": "new.pdf"}),
        ]

        result = filter_existing_documents(mock_store, chunks)

        # Should only return the new document
        assert len(result) == 1
        assert result[0].metadata["source"] == "new.pdf"

    def test_document_exists_returns_true_when_found(self):
        """Test document_exists returns True when document is found."""
        from ragdemo.vector_store import document_exists

        mock_store = MagicMock()
        mock_store.similarity_search.return_value = [Document(page_content="found")]

        result = document_exists(mock_store, "test.pdf")

        assert result is True
        mock_store.similarity_search.assert_called_once()

    def test_document_exists_returns_false_when_not_found(self):
        """Test document_exists returns False when document not found."""
        from ragdemo.vector_store import document_exists

        mock_store = MagicMock()
        mock_store.similarity_search.return_value = []

        result = document_exists(mock_store, "missing.pdf")

        assert result is False

    @pytest.mark.skipif(
        not os.getenv("SUPABASE_PASSWORD"),
        reason="Supabase credentials not configured"
    )
    def test_create_vector_store_connects_to_supabase(self, mock_embeddings):
        """Integration test: verify connection to Supabase (requires credentials)."""
        from ragdemo.vector_store import create_vector_store

        chunks = [
            Document(page_content="Test document", metadata={"source": "test.pdf"}),
        ]

        with patch('ragdemo.vector_store.OpenAIEmbeddings', return_value=mock_embeddings):
            vector_store = create_vector_store(chunks)

        assert vector_store is not None
