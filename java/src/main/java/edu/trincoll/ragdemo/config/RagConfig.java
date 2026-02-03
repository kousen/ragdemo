package edu.trincoll.ragdemo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for RAG components.
 * <p>
 * The key abstraction here is the QuestionAnswerAdvisor, which intercepts
 * ChatClient calls to automatically retrieve relevant documents and augment
 * the user's prompt with context before sending to the LLM.
 */
@Configuration
public class RagConfig {

    /**
     * SimpleVectorStore is an in-memory vector store suitable for demos.
     * For production, consider PgVectorStore, ChromaVectorStore, or RedisVectorStore.
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * ChatClient configured with QuestionAnswerAdvisor for RAG.
     * The advisor automatically:
     * 1. Searches the vector store for relevant documents
     * 2. Adds retrieved context to the system prompt
     * 3. Sends the augmented prompt to the LLM
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, VectorStore vectorStore) {
        return builder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
    }
}
