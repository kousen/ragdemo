package edu.trincoll.ragdemo;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.DefaultMetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageMode;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

import java.util.Collections;
import java.util.List;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * RAG service using LangChain4j with Supabase/PgVector for persistent storage.
 * <p>
 * RAG Flow Steps 2-6: Chunk → Embed → Store → Retrieve → Generate
 * <p>
 * Uses two key LangChain4j patterns:
 * <ul>
 *   <li><b>EmbeddingStoreIngestor</b> — bundles document splitting, embedding, and storage
 *       into a single pipeline. The splitter is configured with overlap here:
 *       {@code DocumentSplitters.recursive(800, 100)} — the second parameter (100) is the
 *       overlap in characters. Spring AI's TokenTextSplitter has NO overlap support
 *       (expected in 2.x, see GitHub issue #2123).</li>
 *   <li><b>AiServices</b> — define a simple Java interface (Assistant), and AiServices
 *       creates a dynamic proxy that wires together the content retriever and chat model.
 *       Conceptually similar to Spring AI's ChatClient with QuestionAnswerAdvisor,
 *       but without Spring's DI container.</li>
 * </ul>
 */
public class RagService {

    // Chunk overlap prevents semantic meaning from being split across chunk boundaries.
    // For example, if a sentence spans two chunks, the overlap ensures both chunks
    // contain the full sentence, improving retrieval quality.
    static final int CHUNK_SIZE = 800;
    static final int CHUNK_OVERLAP = 100;

    /**
     * AiServices interface — LangChain4j generates a dynamic proxy implementation.
     * The proxy automatically: embeds the question, retrieves relevant content,
     * augments the prompt with context, and sends it to the chat model.
     */
    interface Assistant {
        String answer(String question);
    }

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChatModel chatModel;
    private final EmbeddingStoreIngestor ingestor;
    private final Assistant assistant;

    public RagService(String apiKey) {
        // Same models as the Spring AI version
        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-3-small")
                .build();

        this.chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-5-mini")
                .build();

        // PgVectorEmbeddingStore — connects to Supabase-hosted PostgreSQL.
        // Table "vector_store" matches Spring AI and Python for cross-language interop.
        String host = System.getenv("SUPABASE_HOST");
        int port = Integer.parseInt(
                System.getenv("SUPABASE_PORT") != null ? System.getenv("SUPABASE_PORT") : "6543");
        String database = System.getenv("SUPABASE_DATABASE") != null
                ? System.getenv("SUPABASE_DATABASE") : "postgres";
        String user = System.getenv("SUPABASE_USER");
        String password = System.getenv("SUPABASE_PASSWORD");

        this.embeddingStore = PgVectorEmbeddingStore.builder()
                .host(host)
                .port(port)
                .database(database)
                .user(user)
                .password(password)
                .table("vector_store")
                .dimension(1536)
                .createTable(true)
                .dropTableFirst(true)  // Drop and recreate to fix schema mismatch
                .useIndex(false)  // Disable index creation for Supabase (memory limits)
                .metadataStorageConfig(DefaultMetadataStorageConfig.builder()
                        .columnDefinitions(Collections.singletonList("metadata JSONB"))
                        .storageMode(MetadataStorageMode.COMBINED_JSONB)
                        .build())
                .build();

        // EmbeddingStoreIngestor is the idiomatic LC4j pipeline for ingestion:
        // split → embed → store, all configured in one builder.
        // Compare with Spring AI: new TokenTextSplitter() — NO overlap parameter exists.
        // Compare with Python LangChain: RecursiveCharacterTextSplitter(chunk_overlap=200)
        this.ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(CHUNK_SIZE, CHUNK_OVERLAP))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        // EmbeddingStoreContentRetriever handles the Retrieve step:
        // embed the question → search the store → return matching segments
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(4)
                .minScore(0.7)
                .build();

        // AiServices builds a proxy that chains: retrieve → augment → generate.
        // No Spring annotations, no DI container — just plain Java.
        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .contentRetriever(contentRetriever)
                .build();
    }

    /**
     * Check if a document with the given source already exists in the vector store.
     * Uses a metadata filter query — same pattern as Spring AI's DocumentLoaderService.
     *
     * @param source the source filename to check
     * @return true if documents from this source already exist
     */
    public boolean documentExists(String source) {
        Embedding queryEmbedding = embeddingModel.embed("test").content();
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(1)
                        .minScore(0.0)
                        .filter(metadataKey("source").isEqualTo(source))
                        .build()
        );
        return !result.matches().isEmpty();
    }

    /**
     * Ingest documents through the full pipeline: split → embed → store.
     * Skips documents that already exist in the store (by source metadata).
     *
     * @param documents documents to ingest
     */
    public void ingest(Document... documents) {
        for (Document doc : documents) {
            String source = doc.metadata().getString("source");
            if (source != null && documentExists(source)) {
                System.out.println("Document '" + source + "' already exists in vector store, skipping.");
                continue;
            }
            ingestor.ingest(doc);
        }
    }

    /**
     * Ask a question — the Assistant proxy handles retrieval + generation.
     */
    public String ask(String question) {
        return assistant.answer(question);
    }

    /**
     * Debug mode: search the embedding store directly, then construct
     * a manual prompt so the displayed documents are exactly the ones
     * used for the answer (not a second retrieval via AiServices).
     *
     * @return result containing the answer and retrieved matches
     */
    public DebugResult askWithContext(String question) {
        // Step 1: Embed the question
        Embedding queryEmbedding = embeddingModel.embed(question).content();

        // Step 2: Search the store directly
        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(4)
                        .minScore(0.7)
                        .build()
        );

        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

        // Step 3: Build a manual prompt with the retrieved context
        StringBuilder context = new StringBuilder();
        for (EmbeddingMatch<TextSegment> match : matches) {
            context.append(match.embedded().text()).append("\n\n");
        }

        String prompt = """
                Context information is below.
                ---------------------
                %s
                ---------------------
                Given the context information and no prior knowledge, answer the query.
                Query: %s
                Answer:""".formatted(context.toString().trim(), question);

        // Step 4: Send to chat model directly (bypassing AiServices)
        String answer = chatModel.chat(prompt);

        return new DebugResult(answer, matches);
    }

    /**
     * Result from debug mode, containing both the answer and retrieved segments.
     */
    public record DebugResult(
            String answer,
            List<EmbeddingMatch<TextSegment>> matches
    ) {}
}
