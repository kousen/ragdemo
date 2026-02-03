"""Tests for document loading and chunking."""

import pytest
from pathlib import Path

from ragdemo.document_loader import load_and_chunk_pdf


class TestDocumentLoader:
    """Tests for the document loader module."""

    def test_load_pdf_returns_chunks(self, sample_pdf_path: Path):
        """Test that loading a PDF returns a non-empty list of chunks."""
        chunks = load_and_chunk_pdf(sample_pdf_path)

        assert len(chunks) > 0
        assert all(hasattr(chunk, 'page_content') for chunk in chunks)

    def test_chunks_have_content(self, sample_pdf_path: Path):
        """Test that all chunks have non-empty content."""
        chunks = load_and_chunk_pdf(sample_pdf_path)

        for chunk in chunks:
            assert chunk.page_content.strip(), "Chunk should have non-empty content"

    def test_chunks_contain_transformer_content(self, sample_pdf_path: Path):
        """Test that chunks contain expected content from the Transformer paper."""
        chunks = load_and_chunk_pdf(sample_pdf_path)

        all_content = " ".join(chunk.page_content.lower() for chunk in chunks)

        # The paper should mention transformer or attention
        assert "transformer" in all_content or "attention" in all_content, \
            "PDF should contain transformer/attention content"

    def test_chunk_size_parameter(self, sample_pdf_path: Path):
        """Test that chunk_size parameter affects the output."""
        small_chunks = load_and_chunk_pdf(sample_pdf_path, chunk_size=500)
        large_chunks = load_and_chunk_pdf(sample_pdf_path, chunk_size=2000)

        # Smaller chunk size should result in more chunks
        assert len(small_chunks) > len(large_chunks)

    def test_chunks_have_metadata(self, sample_pdf_path: Path):
        """Test that chunks include metadata."""
        chunks = load_and_chunk_pdf(sample_pdf_path)

        # PyPDFLoader adds page metadata
        for chunk in chunks:
            assert hasattr(chunk, 'metadata')
            assert isinstance(chunk.metadata, dict)

    def test_invalid_pdf_path_raises_error(self):
        """Test that an invalid path raises an appropriate error."""
        with pytest.raises(Exception):
            load_and_chunk_pdf(Path("/nonexistent/file.pdf"))
