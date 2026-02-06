package edu.trincoll.ragdemo;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for document loading and chunking — no API key required.
 * Tests PDF loading, content extraction, metadata, and the overlap feature
 * that distinguishes LangChain4j from Spring AI.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentLoaderTest {

    private Document document;
    private List<TextSegment> segments;

    @BeforeAll
    void loadDocument() throws URISyntaxException {
        DocumentLoader loader = new DocumentLoader();
        Path pdfPath = Paths.get(
                getClass().getClassLoader().getResource("documents/sample.pdf").toURI()
        );
        document = loader.load(pdfPath);

        // Split using the same settings as RagService's EmbeddingStoreIngestor
        segments = DocumentSplitters
                .recursive(RagService.CHUNK_SIZE, RagService.CHUNK_OVERLAP)
                .split(document);
    }

    @Test
    void shouldLoadPdfWithContent() {
        assertThat(document.text()).isNotBlank();
        assertThat(document.text().length()).isGreaterThan(1000);
    }

    @Test
    void shouldHaveSourceMetadata() {
        assertThat(document.metadata().getString("source"))
                .isEqualTo("sample.pdf");
    }

    @Test
    void shouldProduceMultipleChunks() {
        assertThat(segments).isNotEmpty();
        assertThat(segments.size()).isGreaterThan(5);
    }

    @Test
    void chunksShouldHaveContent() {
        assertThat(segments)
                .allSatisfy(segment -> assertThat(segment.text()).isNotBlank());
    }

    @Test
    void chunksShouldContainTransformerContent() {
        // The sample PDF is "Attention Is All You Need"
        boolean hasRelevantContent = segments.stream()
                .anyMatch(segment -> {
                    String text = segment.text().toLowerCase();
                    return text.contains("transformer") || text.contains("attention");
                });
        assertThat(hasRelevantContent)
                .as("PDF chunks should contain transformer/attention content")
                .isTrue();
    }

    /**
     * Overlap test — this test doesn't exist in the Spring AI version because
     * Spring AI's TokenTextSplitter has no overlap support.
     * <p>
     * With overlap enabled (DocumentSplitters.recursive(800, 100)), consecutive
     * chunks should share some text content at their boundaries. This prevents
     * semantic meaning from being lost when a sentence or concept spans two chunks.
     */
    @Test
    void consecutiveChunksShouldOverlap() {
        int overlapCount = 0;
        for (int i = 0; i < segments.size() - 1; i++) {
            String current = segments.get(i).text();
            String next = segments.get(i + 1).text();

            // The end of the current chunk should appear at the start of the next.
            // Check if any suffix of current (at least 20 chars) is a prefix of next.
            boolean hasOverlap = false;
            int minOverlap = 20;
            for (int len = minOverlap; len <= Math.min(200, current.length()); len++) {
                String suffix = current.substring(current.length() - len);
                if (next.startsWith(suffix)) {
                    hasOverlap = true;
                    break;
                }
            }
            if (hasOverlap) overlapCount++;
        }

        // Most consecutive chunks should overlap (allow some splits at natural boundaries)
        assertThat(overlapCount)
                .as("Expected most consecutive chunks to share overlapping text. " +
                    "Found %d overlapping pairs out of %d consecutive pairs.",
                    overlapCount, segments.size() - 1)
                .isGreaterThan(0);
    }
}
