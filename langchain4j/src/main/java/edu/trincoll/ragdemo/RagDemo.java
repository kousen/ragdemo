package edu.trincoll.ragdemo;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive RAG demo using LangChain4j — no Spring Boot required.
 * <p>
 * This entire RAG pipeline (load → chunk → embed → store → retrieve → generate)
 * runs as a plain Java application with a public static void main().
 * No Spring DI, no @Autowired, no application.properties.
 * <p>
 * LangChain4j CAN integrate with Spring Boot via langchain4j-spring-boot-starter,
 * but this version deliberately avoids Spring to show the contrast with java/.
 */
public class RagDemo {

    public static void main(String[] args) {
        System.out.println("\n=== RAG Demo (Java/LangChain4j) ===\n");

        // Check for API key
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Error: OPENAI_API_KEY environment variable is not set.");
            System.err.println("Set it with: export OPENAI_API_KEY=sk-...");
            System.exit(1);
        }

        // Step 1: Load the PDF
        System.out.println("Loading documents...");
        DocumentLoader loader = new DocumentLoader();
        Path pdfPath = getResourcePath("documents/sample.pdf");
        Document document = loader.load(pdfPath);

        // Steps 2-4: Chunk (with overlap) → Embed → Store
        // The EmbeddingStoreIngestor handles all three steps as one pipeline
        System.out.println("Ingesting (splitting with overlap, embedding, storing)...");
        RagService ragService = new RagService(apiKey);
        ragService.ingest(document);
        System.out.println("Ready!\n");

        // Interactive Q&A loop
        System.out.println("""
                Ask questions about the document (type 'quit' to exit):
                Tip: Prefix with 'debug:' to see which documents were retrieved

                Sample questions:
                  - What is the Transformer architecture?
                  - How does self-attention work?
                  - What are the key contributions of this paper?
                """);

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("\nYou: ");
                if (!scanner.hasNextLine()) break;
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                if (input.isEmpty()) continue;

                boolean debugMode = input.toLowerCase().startsWith("debug:");
                String question = debugMode ? input.substring(6).trim() : input;

                if (question.isEmpty()) {
                    System.out.println("Please provide a question after 'debug:'");
                    continue;
                }

                System.out.println("\nAssistant: Thinking...");

                if (debugMode) {
                    RagService.DebugResult result = ragService.askWithContext(question);
                    System.out.println("\nAssistant: " + result.answer());
                    printRetrievedDocuments(result.matches());
                } else {
                    String answer = ragService.ask(question);
                    System.out.println("\nAssistant: " + answer);
                }
            }
        }
    }

    private static void printRetrievedDocuments(List<EmbeddingMatch<TextSegment>> matches) {
        System.out.printf("%n--- Retrieved %d documents ---%n", matches.size());
        int maxChars = 200;
        for (int i = 0; i < matches.size(); i++) {
            EmbeddingMatch<TextSegment> match = matches.get(i);
            TextSegment segment = match.embedded();
            String source = segment.metadata().getString("source");
            if (source == null) source = "unknown";

            String content = segment.text();
            String preview = content.length() > maxChars
                    ? content.substring(0, maxChars).replace("\n", " ") + "..."
                    : content.replace("\n", " ");
            System.out.printf("%n[%d] Source: %s, Score: %.3f%n", i + 1, source, match.score());
            System.out.printf("    %s%n", preview);
        }
        System.out.println("-----------------------------------\n");
    }

    private static Path getResourcePath(String resource) {
        URL url = RagDemo.class.getClassLoader().getResource(resource);
        if (url == null) {
            throw new RuntimeException("Resource not found: " + resource);
        }
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid resource path: " + resource, e);
        }
    }
}
