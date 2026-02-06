package edu.trincoll.ragdemo;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;

import java.nio.file.Path;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

/**
 * Loads PDF documents for ingestion into the RAG pipeline.
 * <p>
 * RAG Flow Step 1: Load
 * <p>
 * Uses Apache Tika for document parsing, which handles PDF, DOCX, HTML, and more.
 * The loaded Document is passed to RagService, where EmbeddingStoreIngestor handles
 * the remaining steps (chunk → embed → store) as a single idiomatic pipeline.
 */
public class DocumentLoader {

    /**
     * Load a PDF file and add source metadata for traceability.
     *
     * @param pdfPath path to the PDF file
     * @return a Document with text content and source metadata
     */
    public Document load(Path pdfPath) {
        Document document = loadDocument(pdfPath, new ApacheTikaDocumentParser());
        document.metadata().put("source", pdfPath.getFileName().toString());
        return document;
    }
}
