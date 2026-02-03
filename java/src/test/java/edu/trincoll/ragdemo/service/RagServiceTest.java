package edu.trincoll.ragdemo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;

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
}
