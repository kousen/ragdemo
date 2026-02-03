"""
RAG Demo - Main entry point.

Interactive CLI for question-answering over PDF documents.
"""

import os
from pathlib import Path

from dotenv import load_dotenv

from ragdemo.document_loader import load_all_pdfs
from ragdemo.rag_chain import create_rag_chain, create_rag_chain_with_sources, print_source_documents
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

    # Find the documents directory
    script_dir = Path(__file__).parent.parent.parent
    docs_dir = script_dir / "documents"

    if not docs_dir.exists():
        print(f"Error: Documents directory not found at {docs_dir}")
        return

    # RAG Flow: Load → Chunk → Embed → Store
    print("Loading and processing PDFs...")
    chunks = load_all_pdfs(docs_dir)

    if not chunks:
        print("No documents loaded. Add PDF files to the documents/ directory.")
        return

    vector_store = create_vector_store(chunks)

    # Create RAG chains - one simple, one with source tracking
    rag_chain = create_rag_chain(vector_store)
    rag_chain_with_sources = create_rag_chain_with_sources(vector_store)
    print("\nRAG chain ready!\n")

    # Interactive Q&A loop
    print("Ask questions about the documents (type 'quit' to exit):")
    print("Tip: Prefix with 'debug:' to see which documents were retrieved")
    print("\nSample questions:")
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

        # Check for debug mode
        debug_mode = question.lower().startswith("debug:")
        if debug_mode:
            question = question[6:].strip()
            if not question:
                print("Please provide a question after 'debug:'")
                continue

        print("\nAssistant: Thinking...")

        try:
            if debug_mode:
                # Use chain that returns source documents
                result = rag_chain_with_sources.invoke(question)
                print(f"\nAssistant: {result['answer']}")
                print_source_documents(result["source_documents"])
            else:
                answer = rag_chain.invoke(question)
                print(f"\nAssistant: {answer}")
        except Exception as e:
            print(f"\nError: {e}")


if __name__ == "__main__":
    main()
