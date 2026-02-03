package edu.trincoll.ragdemo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * Configuration for RAG components.
 * <p>
 * The key abstraction here is the QuestionAnswerAdvisor, which intercepts
 * ChatClient calls to automatically retrieve relevant documents and augment
 * the user's prompt with context before sending to the LLM.
 * <p>
 * The prompt template is loaded from {@code prompts/rag-prompt.st} and can be
 * customized. It uses two placeholders:
 * <ul>
 *   <li>{@code {question_answer_context}} - Retrieved document chunks</li>
 *   <li>{@code {query}} - The user's question</li>
 * </ul>
 */
@Configuration
public class RagConfig {

    @Value("classpath:prompts/rag-prompt.st")
    private Resource ragPromptResource;

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
     * <p>
     * The prompt template is customizable via prompts/rag-prompt.st
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, VectorStore vectorStore) {
        PromptTemplate promptTemplate = new PromptTemplate(ragPromptResource);

        return builder
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .promptTemplate(promptTemplate)
                        .build())
                .build();
    }
}
