"""
RAG chain module using LangChain Expression Language (LCEL).

RAG Flow Step 4-5: Retrieve → Generate
- Retriever: Searches vector store for relevant chunks
- Prompt: Combines context with user question
- LLM: Generates answer based on augmented prompt

The prompt template used is:
    Answer the question based only on the following context.
    If the context doesn't contain enough information to answer, say so.

    Context:
    {context}

    Question: {question}

    Answer:
"""

from langchain_core.documents import Document
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import RunnablePassthrough
from langchain_core.vectorstores import VectorStore
from langchain_openai import ChatOpenAI


# RAG prompt template - instructs the model to answer based on context
RAG_TEMPLATE = """Answer the question based only on the following context.
If the context doesn't contain enough information to answer, say so.

Context:
{context}

Question: {question}

Answer:"""


def format_docs(docs: list) -> str:
    """Format retrieved documents into a single string for the prompt."""
    return "\n\n".join(doc.page_content for doc in docs)


def create_rag_chain(
    vector_store: VectorStore,
    model: str = "gpt-5-mini",
    k: int = 4,
):
    """
    Create a RAG chain using LCEL (LangChain Expression Language).

    The chain follows this flow:
    1. question → retriever → relevant documents
    2. documents → format_docs → context string
    3. {context, question} → prompt → augmented prompt
    4. augmented prompt → LLM → response
    5. response → parser → final answer string

    Args:
        vector_store: The vector store to retrieve from
        model: OpenAI chat model to use
        k: Number of documents to retrieve

    Returns:
        LCEL chain that can be invoked with a question string
    """
    # Create retriever from vector store
    retriever = vector_store.as_retriever(search_kwargs={"k": k})

    # Create prompt template
    prompt = ChatPromptTemplate.from_template(RAG_TEMPLATE)

    # Create LLM (gpt-5 models only support temperature=1)
    llm = ChatOpenAI(model=model, temperature=1)

    # Build the RAG chain using LCEL
    # The | operator chains components together
    rag_chain = (
        {
            "context": retriever | format_docs,
            "question": RunnablePassthrough(),
        }
        | prompt
        | llm
        | StrOutputParser()
    )

    return rag_chain


def create_rag_chain_with_sources(
    vector_store: VectorStore,
    model: str = "gpt-5-mini",
    k: int = 4,
):
    """
    Create a RAG chain that returns both the answer and retrieved documents.

    Useful for debugging and understanding which documents contributed to the answer.
    Documents include similarity scores in their metadata when available.

    Args:
        vector_store: The vector store to retrieve from
        model: OpenAI chat model to use
        k: Number of documents to retrieve

    Returns:
        Callable that returns {"answer": str, "source_documents": list[Document]}
    """
    prompt = ChatPromptTemplate.from_template(RAG_TEMPLATE)
    llm = ChatOpenAI(model=model, temperature=1)

    def retrieve_with_scores(question: str) -> list[Document]:
        """Retrieve documents with similarity scores attached to metadata."""
        try:
            # Use similarity_search_with_score to get (doc, score) tuples
            results = vector_store.similarity_search_with_score(question, k=k)
            docs_with_scores = []
            for doc, score in results:
                doc.metadata["score"] = score
                docs_with_scores.append(doc)
            return docs_with_scores
        except Exception:
            # Fallback if similarity_search_with_score not available
            return vector_store.similarity_search(question, k=k)

    def invoke(question: str) -> dict:
        """Run the RAG chain and return answer with sources."""
        docs = retrieve_with_scores(question)
        context = format_docs(docs)

        # Build the prompt and get answer
        chain = prompt | llm | StrOutputParser()
        answer = chain.invoke({"context": context, "question": question})

        return {"answer": answer, "source_documents": docs}

    # Return a simple callable wrapper with invoke method
    class ChainWrapper:
        def invoke(self, question: str) -> dict:
            return invoke(question)

    return ChainWrapper()


def print_source_documents(docs: list[Document], max_chars: int = 200) -> None:
    """
    Print retrieved source documents for debugging.

    Args:
        docs: List of retrieved Document objects (may include similarity scores in metadata)
        max_chars: Maximum characters to show from each document
    """
    print(f"\n--- Retrieved {len(docs)} documents ---")
    for i, doc in enumerate(docs, 1):
        source = doc.metadata.get("source", "unknown")
        page = doc.metadata.get("page", "?")
        score = doc.metadata.get("score")
        content_preview = doc.page_content[:max_chars].replace("\n", " ")
        if len(doc.page_content) > max_chars:
            content_preview += "..."

        # Show similarity score if available
        score_str = f", Score: {score:.3f}" if score is not None else ""
        print(f"\n[{i}] Source: {source}, Page: {page}{score_str}")
        print(f"    {content_preview}")
    print("-----------------------------------\n")
