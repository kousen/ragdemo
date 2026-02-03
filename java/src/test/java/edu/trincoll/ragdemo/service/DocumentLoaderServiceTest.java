package edu.trincoll.ragdemo.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocumentLoaderServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Captor
    private ArgumentCaptor<List<Document>> documentsCaptor;

    private DocumentLoaderService documentLoaderService;

    @BeforeEach
    void setUp() {
        documentLoaderService = new DocumentLoaderService(vectorStore);
    }

    @Test
    void loadPdf_shouldExtractAndChunkDocument() {
        // Given
        var pdfResource = new ClassPathResource("documents/sample.pdf");

        // When
        int chunkCount = documentLoaderService.loadPdf(pdfResource);

        // Then
        assertThat(chunkCount).isGreaterThan(0);
        verify(vectorStore).add(documentsCaptor.capture());

        List<Document> addedDocuments = documentsCaptor.getValue();
        assertThat(addedDocuments).isNotEmpty();
        assertThat(addedDocuments.size()).isEqualTo(chunkCount);
    }

    @Test
    void loadPdf_chunksContainContent() {
        // Given
        var pdfResource = new ClassPathResource("documents/sample.pdf");

        // When
        documentLoaderService.loadPdf(pdfResource);

        // Then
        verify(vectorStore).add(documentsCaptor.capture());
        List<Document> chunks = documentsCaptor.getValue();

        // Verify chunks have content
        assertThat(chunks)
                .allSatisfy(doc -> assertThat(doc.getText()).isNotBlank());
    }

    @Test
    void loadPdf_chunksContainTransformerContent() {
        // Given - the sample PDF is "Attention Is All You Need"
        var pdfResource = new ClassPathResource("documents/sample.pdf");

        // When
        documentLoaderService.loadPdf(pdfResource);

        // Then
        verify(vectorStore).add(documentsCaptor.capture());
        List<Document> chunks = documentsCaptor.getValue();

        // At least one chunk should mention "Transformer" or "attention"
        boolean hasRelevantContent = chunks.stream()
                .anyMatch(doc -> {
                    Assertions.assertNotNull(doc.getText());
                    return doc.getText().toLowerCase().contains("transformer")
                               || doc.getText().toLowerCase().contains("attention");
                });
        assertThat(hasRelevantContent)
                .as("PDF chunks should contain transformer/attention content")
                .isTrue();
    }

    @Test
    void loadPdf_chunksContainSourceMetadata() {
        // Given
        var pdfResource = new ClassPathResource("documents/sample.pdf");

        // When
        documentLoaderService.loadPdf(pdfResource);

        // Then
        verify(vectorStore).add(documentsCaptor.capture());
        List<Document> chunks = documentsCaptor.getValue();

        // All chunks should have source metadata
        assertThat(chunks)
                .allSatisfy(doc -> {
                    assertThat(doc.getMetadata()).containsKey("source");
                    assertThat(doc.getMetadata().get("source")).isEqualTo("sample.pdf");
                });
    }
}
