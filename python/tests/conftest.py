"""Pytest fixtures for RAG demo tests."""

import os
from pathlib import Path
from unittest.mock import MagicMock

import pytest


@pytest.fixture(scope="class")
def sample_pdf_path() -> Path:
    """Return path to the sample PDF."""
    return Path(__file__).parent.parent / "documents" / "sample.pdf"


@pytest.fixture
def mock_embeddings():
    """Create a mock embeddings model."""
    mock = MagicMock()
    # Return a fixed-size embedding vector
    mock.embed_documents.return_value = [[0.1] * 1536 for _ in range(10)]
    mock.embed_query.return_value = [0.1] * 1536
    return mock


@pytest.fixture
def mock_llm():
    """Create a mock LLM."""
    mock = MagicMock()
    mock.invoke.return_value = MagicMock(content="This is a mock response about transformers.")
    return mock


@pytest.fixture
def has_openai_key() -> bool:
    """Check if OPENAI_API_KEY is available."""
    return bool(os.getenv("OPENAI_API_KEY"))
