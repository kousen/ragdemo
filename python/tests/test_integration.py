"""Integration tests requiring OPENAI_API_KEY."""

import os
from pathlib import Path

import pytest


# Skip all tests in this module if no API key
pytestmark = pytest.mark.skipif(
    not os.getenv("OPENAI_API_KEY"),
    reason="OPENAI_API_KEY not set"
)


class TestRagIntegration:
    """Integration tests for the full RAG pipeline."""

    @pytest.fixture(scope="class")
    def rag_chain(self, sample_pdf_path: Path):
        """Create a RAG chain with loaded documents."""
        from ragdemo.document_loader import load_and_chunk_pdf
        from ragdemo.vector_store import create_vector_store
        from ragdemo.rag_chain import create_rag_chain

        chunks = load_and_chunk_pdf(sample_pdf_path)
        vector_store = create_vector_store(chunks)
        return create_rag_chain(vector_store)

    def test_answer_question_about_transformers(self, rag_chain):
        """Test answering a question about the Transformer architecture."""
        answer = rag_chain.invoke("What is the Transformer architecture?")

        assert answer
        assert isinstance(answer, str)
        assert len(answer) > 50  # Should be a substantive answer

        # Should mention relevant concepts
        answer_lower = answer.lower()
        assert any(term in answer_lower for term in
                   ["attention", "encoder", "decoder", "self-attention", "neural"])

    def test_answer_question_about_attention(self, rag_chain):
        """Test answering a question about attention mechanisms."""
        answer = rag_chain.invoke("How does self-attention work?")

        assert answer
        answer_lower = answer.lower()
        assert any(term in answer_lower for term in
                   ["query", "key", "value", "attention", "weight", "score"])

    def test_answer_question_about_paper_contributions(self, rag_chain):
        """Test answering about paper contributions."""
        answer = rag_chain.invoke(
            "What are the key contributions of the Attention Is All You Need paper?"
        )

        assert answer
        answer_lower = answer.lower()
        assert any(term in answer_lower for term in
                   ["transformer", "attention", "sequence", "parallel", "recurrence"])

    def test_handles_unrelated_question(self, rag_chain):
        """Test handling a question not covered by the document."""
        answer = rag_chain.invoke("What is the recipe for chocolate cake?")

        # Should still return a response
        assert answer
        assert isinstance(answer, str)

    def test_multiple_questions_work(self, rag_chain):
        """Test that multiple sequential questions work."""
        q1 = rag_chain.invoke("What is attention?")
        q2 = rag_chain.invoke("What is a transformer?")

        assert q1
        assert q2
        # Answers should be different
        assert q1 != q2
