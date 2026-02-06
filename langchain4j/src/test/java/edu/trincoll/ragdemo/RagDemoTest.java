package edu.trincoll.ragdemo;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagDemoTest {

    @Test
    void shouldListAllPdfFilesInMainDocumentsDirectory() {
        Path docsPath = Path.of("src/main/resources/documents");

        List<Path> pdfPaths = RagDemo.listPdfPaths(docsPath);

        assertThat(pdfPaths)
                .extracting(path -> path.getFileName().toString())
                .contains("sample.pdf", "asthma_emergency_department_algorithm_-_9.8.23.pdf");
    }

    @Test
    void listedFilesShouldOnlyContainPdfExtensions() {
        Path docsPath = Path.of("src/main/resources/documents");

        List<Path> pdfPaths = RagDemo.listPdfPaths(docsPath);

        assertThat(pdfPaths)
                .allSatisfy(path -> assertThat(path.getFileName().toString().toLowerCase())
                        .endsWith(".pdf"));
    }
}
