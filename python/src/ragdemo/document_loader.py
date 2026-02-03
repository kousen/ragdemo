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
    pdf_path = Path(pdf_path)
    filename = pdf_path.name

    # Step 1: Load PDF - extracts text page by page
    loader = PyPDFLoader(str(pdf_path))
    documents = loader.load()
    print(f"Loaded {len(documents)} pages from {filename}")

    # Step 2: Chunk - split into smaller pieces for better retrieval
    # RecursiveCharacterTextSplitter tries to split on natural boundaries
    # (paragraphs, sentences, words) before falling back to characters.
    # This preserves semantic context better than fixed-size splits.
    #
    # Alternative: TokenTextSplitter (used in Java version) splits by token count,
    # which is useful for staying within LLM context limits but may split
    # mid-sentence.
    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=chunk_size,
        chunk_overlap=chunk_overlap,
        length_function=len,
        separators=["\n\n", "\n", " ", ""],
    )
    chunks = text_splitter.split_documents(documents)

    # Add source metadata to help identify which document content came from
    for chunk in chunks:
        chunk.metadata["source"] = filename

    print(f"Split {filename} into {len(chunks)} chunks")
    return chunks


def load_all_pdfs(
    directory: str | Path,
    chunk_size: int = 1000,
    chunk_overlap: int = 200,
) -> list:
    """
    Load all PDF files from a directory and split them into chunks.

    Args:
        directory: Path to directory containing PDF files
        chunk_size: Maximum characters per chunk
        chunk_overlap: Number of characters to overlap between chunks

    Returns:
        List of Document objects (chunks) from all PDFs
    """
    directory = Path(directory)
    pdf_files = sorted(directory.glob("*.pdf"))

    if not pdf_files:
        print(f"No PDF files found in {directory}")
        return []

    print(f"Found {len(pdf_files)} PDF files to load")

    all_chunks = []
    for pdf_path in pdf_files:
        chunks = load_and_chunk_pdf(pdf_path, chunk_size, chunk_overlap)
        all_chunks.extend(chunks)

    print(f"Total: {len(all_chunks)} chunks from {len(pdf_files)} PDFs")
    return all_chunks
