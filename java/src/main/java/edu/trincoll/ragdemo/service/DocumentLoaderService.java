package edu.trincoll.ragdemo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Service for loading documents into the vector store.
 * <p>
 * RAG Flow Step 1-3: Load → Chunk → Store
 * - PagePdfDocumentReader: Extracts text from PDF (one Document per page)
 * - TokenTextSplitter: Splits documents into smaller chunks for better retrieval
 * - VectorStore: Embeds chunks and stores them for similarity search
 */
@Service
public class DocumentLoaderService {

    private static final Logger log = LoggerFactory.getLogger(DocumentLoaderService.class);

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    @Value("classpath:documents/*.pdf")
    private Resource[] pdfResources;

    public DocumentLoaderService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        // TokenTextSplitter splits by token count (~800 tokens per chunk).
        // NOTE: Unlike LangChain, Spring AI's TokenTextSplitter has NO overlap between chunks.
        // This may cause context loss at chunk boundaries.
        //
        // Alternative: Python's RecursiveCharacterTextSplitter splits on semantic boundaries
        // (paragraphs, sentences) and supports overlap for better context preservation.
        this.textSplitter = new TokenTextSplitter();
    }

    /**
     * Load all PDF documents from the documents directory into the vector store.
     *
     * @return total number of chunks created and stored
     */
    public int loadAllPdfs() {
        if (pdfResources == null || pdfResources.length == 0) {
            log.warn("No PDF files found in documents directory");
            return 0;
        }

        log.info("Found {} PDF files to load", pdfResources.length);
        return Arrays.stream(pdfResources)
                .mapToInt(this::loadPdf)
                .sum();
    }

    /**
     * Load a PDF document into the vector store.
     * Adds source metadata to each chunk for traceability.
     *
     * @param pdfResource the PDF file to load
     * @return number of chunks created and stored
     */
    public int loadPdf(Resource pdfResource) {
        String filename = pdfResource.getFilename();
        log.info("Loading PDF: {}", filename);

        // Step 1: Load PDF - extracts text page by page
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource);
        List<Document> documents = pdfReader.get();
        log.info("Extracted {} pages from {}", documents.size(), filename);

        // Step 2: Chunk - split into smaller pieces for better retrieval
        List<Document> chunks = textSplitter.apply(documents);
        log.info("Split {} into {} chunks", filename, chunks.size());

        // Add source metadata to help identify which document content came from
        chunks.forEach(chunk -> chunk.getMetadata().put("source", filename));

        // Step 3: Store - embed and add to vector store
        // The VectorStore automatically embeds each chunk using the EmbeddingModel
        vectorStore.add(chunks);
        log.info("Added {} chunks from {} to vector store", chunks.size(), filename);

        return chunks.size();
    }
}
