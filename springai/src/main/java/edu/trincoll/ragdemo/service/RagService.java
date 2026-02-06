package edu.trincoll.ragdemo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for RAG-based question answering.
 * <p>
 * RAG Flow Step 4-5: Retrieve → Generate
 * The QuestionAnswerAdvisor (configured in RagConfig) handles these steps:
 * - Retrieve: Searches vector store for relevant chunks
 * - Augment: Adds retrieved context to the prompt
 * - Generate: Sends augmented prompt to LLM
 * <p>
 * The advisor uses a default prompt template similar to:
 * <pre>
 * Context information is below.
 * ---------------------
 * {question_answer_context}
 * ---------------------
 * Given the context information and no prior knowledge, answer the query.
 * Query: {query}
 * Answer:
 * </pre>
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

    /**
     * Ask a question and get the full response including metadata.
     * Use this to inspect which documents were retrieved for the answer.
     *
     * @param question the user's question
     * @return the full ChatResponse with metadata
     */
    public ChatResponse askWithContext(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .chatResponse();
    }

    /**
     * Get the documents that were retrieved for the last question.
     * Useful for debugging and understanding RAG behavior.
     *
     * @param response the ChatResponse from askWithContext
     * @return list of retrieved documents, or empty list if none found
     */
    @SuppressWarnings("unchecked")
    public List<Document> getRetrievedDocuments(ChatResponse response) {
        Object docs = response.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        if (docs instanceof List<?>) {
            return (List<Document>) docs;
        }
        return List.of();
    }
}
