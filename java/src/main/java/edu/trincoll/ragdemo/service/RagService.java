package edu.trincoll.ragdemo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Service for RAG-based question answering.
 * <p>
 * RAG Flow Step 4-5: Retrieve → Generate
 * The QuestionAnswerAdvisor (configured in RagConfig) handles these steps:
 * - Retrieve: Searches vector store for relevant chunks
 * - Augment: Adds retrieved context to the prompt
 * - Generate: Sends augmented prompt to LLM
 */
@Service
public class RagService {

    private final ChatClient chatClient;

    public RagService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Ask a question and get an answer based on the loaded documents.
     * <p>
     * The ChatClient's QuestionAnswerAdvisor automatically:
     * 1. Embeds the question
     * 2. Searches the vector store for similar chunks
     * 3. Adds retrieved chunks to the system prompt
     * 4. Sends the augmented prompt to the LLM
     * 5. Returns the generated response
     *
     * @param question the user's question
     * @return the LLM's response based on retrieved context
     */
    public String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
