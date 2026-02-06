package edu.trincoll.ragdemo;

import dev.langchain4j.data.document.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests that require a valid OPENAI_API_KEY.
 * These tests verify the full RAG pipeline works end-to-end,
 * including EmbeddingStoreIngestor ingestion and AiServices Q&A.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class RagServiceTest {

    private RagService ragService;

    @BeforeAll
    void setUp() throws URISyntaxException {
        // Load document
        DocumentLoader loader = new DocumentLoader();
        Path pdfPath = Paths.get(
                Objects.requireNonNull(getClass().getClassLoader()
                                .getResource("documents/sample.pdf"))
                        .toURI()
        );
        Document document = loader.load(pdfPath);

        // Ingest via EmbeddingStoreIngestor pipeline (split → embed → store)
        ragService = new RagService(System.getenv("OPENAI_API_KEY"));
        ragService.ingest(document);
    }

    @Test
    void shouldAnswerQuestionAboutTransformerArchitecture() {
        String answer = ragService.ask("What is the Transformer architecture?");

        assertThat(answer).isNotBlank();
        assertThat(answer.toLowerCase())
                .containsAnyOf("attention", "encoder", "decoder", "self-attention");
    }

    @Test
    void shouldAnswerQuestionAboutSelfAttention() {
        String answer = ragService.ask("How does self-attention work in the Transformer?");

        assertThat(answer).isNotBlank();
        assertThat(answer.toLowerCase())
                .containsAnyOf("query", "key", "value", "attention", "weight");
    }

    @Test
    void shouldHandleQuestionNotInDocument() {
        String answer = ragService.ask("What is the recipe for chocolate cake?");

        // Should still return a response (likely saying it doesn't know)
        assertThat(answer).isNotBlank();
    }

    @Test
    void debugModeShouldReturnRelevantDocuments() {
        RagService.DebugResult result = ragService.askWithContext("What is multi-head attention?");

        assertThat(result.answer()).isNotBlank();
        assertThat(result.matches()).isNotEmpty();

        // At least one retrieved segment should mention attention
        boolean hasRelevantContent = result.matches().stream()
                .anyMatch(match -> match.embedded().text().toLowerCase().contains("attention"));
        assertThat(hasRelevantContent)
                .as("Retrieved documents should contain attention-related content")
                .isTrue();
    }

    @Test
    void retrievedDocumentsShouldHaveSourceMetadata() {
        RagService.DebugResult result = ragService.askWithContext("Explain the encoder architecture");

        assertThat(result.matches()).isNotEmpty();
        assertThat(result.matches().getFirst().embedded().metadata().getString("source"))
                .isEqualTo("sample.pdf");
    }

    @Test
    void retrievedDocumentsShouldHaveSimilarityScores() {
        RagService.DebugResult result = ragService.askWithContext("What is multi-head attention?");

        assertThat(result.matches()).isNotEmpty();
        // EmbeddingMatch always provides a score
        assertThat(result.matches().getFirst().score())
                .as("Retrieved documents should have similarity scores")
                .isGreaterThan(0.0);
    }
}
