package edu.trincoll.ragdemo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClientRequestSpec requestSpec;

    @Mock
    private CallResponseSpec responseSpec;

    private RagService ragService;

    @BeforeEach
    void setUp() {
        ragService = new RagService(chatClient);
    }

    @Test
    void ask_shouldReturnResponse() {
        // Given
        String question = "What is the Transformer architecture?";
        String expectedAnswer = "The Transformer is a neural network architecture...";

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(expectedAnswer);

        // When
        String answer = ragService.ask(question);

        // Then
        assertThat(answer).isEqualTo(expectedAnswer);
        verify(requestSpec).user(question);
    }

    @Test
    void ask_shouldPassQuestionToPrompt() {
        // Given
        String question = "How does self-attention work?";

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("Self-attention allows...");

        // When
        ragService.ask(question);

        // Then
        verify(chatClient).prompt();
        verify(requestSpec).user(question);
        verify(requestSpec).call();
    }

    @Test
    void askWithContext_shouldReturnChatResponse() {
        // Given
        String question = "What is attention?";
        ChatResponse mockResponse = new ChatResponse(List.of());

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.chatResponse()).thenReturn(mockResponse);

        // When
        ChatResponse response = ragService.askWithContext(question);

        // Then
        assertThat(response).isEqualTo(mockResponse);
        verify(requestSpec).user(question);
    }

    @Test
    void getRetrievedDocuments_shouldReturnDocumentsFromMetadata() {
        // Given
        List<Document> expectedDocs = List.of(
                new Document("chunk 1"),
                new Document("chunk 2")
        );
        Map<String, Object> metadataMap = Map.of(
                QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS, expectedDocs
        );
        ChatResponseMetadata metadata = ChatResponseMetadata.builder()
                .metadata(metadataMap)
                .build();
        ChatResponse response = new ChatResponse(List.of(), metadata);

        // When
        List<Document> docs = ragService.getRetrievedDocuments(response);

        // Then
        assertThat(docs).hasSize(2);
        assertThat(docs.get(0).getText()).isEqualTo("chunk 1");
    }

    @Test
    void getRetrievedDocuments_shouldReturnEmptyListWhenNoDocuments() {
        // Given
        ChatResponse response = new ChatResponse(List.of());

        // When
        List<Document> docs = ragService.getRetrievedDocuments(response);

        // Then
        assertThat(docs).isEmpty();
    }
}
