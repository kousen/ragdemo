"""
RAG Demo - Main entry point.

Interactive CLI for question-answering over the sample PDF.
"""

import os
from pathlib import Path

from dotenv import load_dotenv

from ragdemo.document_loader import load_and_chunk_pdf
from ragdemo.rag_chain import create_rag_chain
from ragdemo.vector_store import create_vector_store


def main():
    """Run the RAG demo."""
    # Load environment variables from .env file
    load_dotenv()

    # Verify API key is set
    if not os.getenv("OPENAI_API_KEY"):
        print("Error: OPENAI_API_KEY environment variable not set")
        print("Set it via: export OPENAI_API_KEY=sk-...")
        return

    print("\n=== RAG Demo (Python/LangChain) ===\n")

    # Find the sample PDF
    script_dir = Path(__file__).parent.parent.parent
    pdf_path = script_dir / "documents" / "sample.pdf"

    if not pdf_path.exists():
        print(f"Error: Sample PDF not found at {pdf_path}")
        return

    # RAG Flow: Load → Chunk → Embed → Store
    print("Loading and processing PDF...")
    chunks = load_and_chunk_pdf(pdf_path)
    vector_store = create_vector_store(chunks)

    # Create RAG chain
    rag_chain = create_rag_chain(vector_store)
    print("\nRAG chain ready!\n")

    # Interactive Q&A loop
    print("Ask questions about the document (type 'quit' to exit):")
    print("Sample questions:")
    print("  - What is the Transformer architecture?")
    print("  - How does self-attention work?")
    print("  - What are the key contributions of this paper?\n")

    while True:
        try:
            question = input("\nYou: ").strip()
        except (EOFError, KeyboardInterrupt):
            print("\nGoodbye!")
            break

        if question.lower() in ("quit", "exit"):
            print("Goodbye!")
            break

        if not question:
            continue

        print("\nAssistant: Thinking...")
        answer = rag_chain.invoke(question)
        print(f"\nAssistant: {answer}")


if __name__ == "__main__":
    main()
