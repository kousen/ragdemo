"""
Document loading and chunking module.

RAG Flow Step 1-2: Load → Chunk
- PyPDFLoader: Extracts text from PDF (one Document per page)
- RecursiveCharacterTextSplitter: Splits documents into smaller chunks
"""

from pathlib import Path

from langchain_community.document_loaders import PyPDFLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter


def load_and_chunk_pdf(
    pdf_path: str | Path,
    chunk_size: int = 1000,
    chunk_overlap: int = 200,
) -> list:
    """
    Load a PDF and split it into chunks suitable for embedding.

    Args:
        pdf_path: Path to the PDF file
        chunk_size: Maximum characters per chunk
        chunk_overlap: Number of characters to overlap between chunks

    Returns:
        List of Document objects (chunks)
    """
    # Step 1: Load PDF - extracts text page by page
    loader = PyPDFLoader(str(pdf_path))
    documents = loader.load()
    print(f"Loaded {len(documents)} pages from PDF")

    # Step 2: Chunk - split into smaller pieces for better retrieval
    # RecursiveCharacterTextSplitter tries to split on natural boundaries
    # (paragraphs, sentences, words) before falling back to characters
    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=chunk_size,
        chunk_overlap=chunk_overlap,
        length_function=len,
        separators=["\n\n", "\n", " ", ""],
    )
    chunks = text_splitter.split_documents(documents)
    print(f"Split into {len(chunks)} chunks")

    return chunks
