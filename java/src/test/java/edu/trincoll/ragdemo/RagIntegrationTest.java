package edu.trincoll.ragdemo;

import edu.trincoll.ragdemo.service.DocumentLoaderService;
import edu.trincoll.ragdemo.service.RagService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests that require a valid OPENAI_API_KEY.
 * These tests verify the full RAG pipeline works end-to-end.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class RagIntegrationTest {

    @Autowired
    private DocumentLoaderService documentLoaderService;

    @Autowired
    private RagService ragService;

    @BeforeAll
    void loadDocuments() {
        documentLoaderService.loadAllPdfs();
    }

    @Test
    void shouldAnswerQuestionAboutTransformerArchitecture() {
        // When
        String answer = ragService.ask("What is the Transformer architecture?");

        // Then
        assertThat(answer).isNotBlank();
        assertThat(answer.toLowerCase())
                .containsAnyOf("attention", "encoder", "decoder", "self-attention");
    }

    @Test
    void shouldAnswerQuestionAboutSelfAttention() {
        // When
        String answer = ragService.ask("How does self-attention work in the Transformer?");

        // Then
        assertThat(answer).isNotBlank();
        assertThat(answer.toLowerCase())
                .containsAnyOf("query", "key", "value", "attention", "weight");
    }

    @Test
    void shouldAnswerQuestionAboutPaperContributions() {
        // When
        String answer = ragService.ask("What are the key contributions of the Attention Is All You Need paper?");

        // Then
        assertThat(answer).isNotBlank();
        // Should mention something about eliminating recurrence or the transformer model
        assertThat(answer.toLowerCase())
                .containsAnyOf("transformer", "attention", "recurrence", "sequence", "parallel");
    }

    @Test
    void shouldHandleQuestionNotInDocument() {
        // When - asking about something not in the transformer paper
        String answer = ragService.ask("What is the recipe for chocolate cake?");

        // Then - should still return a response, likely saying it doesn't know
        assertThat(answer).isNotBlank();
    }
}
