package edu.trincoll.ragdemo;

import edu.trincoll.ragdemo.service.DocumentLoaderService;
import edu.trincoll.ragdemo.service.RagService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * Interactive CLI runner for the RAG demo.
 * Loads the sample PDF on startup and then enters an interactive Q&A loop.
 * Disabled during tests via @Profile("!test").
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
        System.out.println("Ask questions about the document (type 'quit' to exit):");
        System.out.println("Sample questions:");
        System.out.println("  - What is the Transformer architecture?");
        System.out.println("  - How does self-attention work?");
        System.out.println("  - What are the key contributions of this paper?\n");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("\nYou: ");
                String question = scanner.nextLine().trim();

                if (question.equalsIgnoreCase("quit") || question.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                if (question.isEmpty()) {
                    continue;
                }

                System.out.println("\nAssistant: Thinking...");
                String answer = ragService.ask(question);
                System.out.println("\nAssistant: " + answer);
            }
        }

        // Exit the application
        System.exit(0);
    }
}
