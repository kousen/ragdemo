package edu.trincoll.ragdemo;

import edu.trincoll.ragdemo.service.DocumentLoaderService;
import edu.trincoll.ragdemo.service.RagService;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

/**
 * Interactive CLI runner for the RAG demo.
 * Loads the sample PDF on startup and then enters an interactive Q&A loop.
 * Disabled during tests via @Profile("!test").
 * <p>
 * Supports debug mode: prefix your question with "debug:" to see which
 * documents were retrieved for the answer.
 */
@Component
@Profile("!test")
public class RagDemoRunner implements CommandLineRunner {

    private final DocumentLoaderService documentLoader;
    private final RagService ragService;

    public RagDemoRunner(DocumentLoaderService documentLoader, RagService ragService) {
        this.documentLoader = documentLoader;
        this.ragService = ragService;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n=== RAG Demo (Java/Spring AI) ===\n");

        // Load all PDFs from the documents directory
        int chunks = documentLoader.loadAllPdfs();
        System.out.printf("Loaded documents into %d chunks%n%n", chunks);

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
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                if (input.isEmpty()) {
                    continue;
                }

                // Check for debug mode
                boolean debugMode = input.toLowerCase().startsWith("debug:");
                String question = debugMode ? input.substring(6).trim() : input;

                if (question.isEmpty()) {
                    System.out.println("Please provide a question after 'debug:'");
                    continue;
                }

                System.out.println("\nAssistant: Thinking...");

                if (debugMode) {
                    ChatResponse response = ragService.askWithContext(question);
                    String answer = response.getResult().getOutput().getText();
                    System.out.println("\nAssistant: " + answer);
                    printRetrievedDocuments(ragService.getRetrievedDocuments(response));
                } else {
                    String answer = ragService.ask(question);
                    System.out.println("\nAssistant: " + answer);
                }
            }
        }

        // Exit the application
        System.exit(0);
    }

    /**
     * Print retrieved documents for debugging.
     * Shows which chunks from the vector store were used to answer the question.
     */
    private void printRetrievedDocuments(List<Document> docs) {
        System.out.printf("%n--- Retrieved %d documents ---%n", docs.size());
        int maxChars = 200;
        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            String source = (String) doc.getMetadata().getOrDefault("source", "unknown");
            String content = doc.getText();
            String preview = content.length() > maxChars
                    ? content.substring(0, maxChars).replace("\n", " ") + "..."
                    : content.replace("\n", " ");
            System.out.printf("%n[%d] Source: %s%n", i + 1, source);
            System.out.printf("    %s%n", preview);
        }
        System.out.println("-----------------------------------\n");
    }
}
