package lk.org.inception;

import lk.org.inception.visitors.FindFilesLargerThanVisitor;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class EffectiveSporkTest {

    @Test
    void load_andQueryComplexZip_providesCorrectResults() throws IOException {
        // Arrange: Create a complex zip file for the test
        Path testZip = createComplexTestZip();

        // Act: Use our new, ideal public API
        EffectiveSpork spork = EffectiveSpork.load(testZip);

        // Assert: Verify all functionalities through the single facade object
        assertThat(spork).isNotNull();
        assertThat(spork.getRootNode()).isNotNull();

        // Test the query methods
        assertThat(spork.hasEmptyDirectory()).isTrue();
        assertThat(spork.findEmptyDirectories()).containsExactlyInAnyOrder(
                "//empty_outer",
                "//nested.zip/inner_empty",
                "//nested.zip/deep.zip/deep_empty"
        );

        // Test the string representation
        String treeString = spork.getTreeAsString();
        assertThat(treeString).contains("📁 empty_outer");
        assertThat(treeString).contains("📄 deep.zip");

        // Cleanup
        Files.delete(testZip);
    }

    // Helper method to create a zip file, copied from ArchiveQueryEngineTest
    private Path createComplexTestZip() throws IOException {
        Path tempFile = Files.createTempFile("test-spork-", ".zip");
        ByteArrayOutputStream deepBaos = new ByteArrayOutputStream();
        try (ZipOutputStream deepZos = new ZipOutputStream(deepBaos)) {
            deepZos.putNextEntry(new ZipEntry("deep_empty/"));
            deepZos.closeEntry();
        }
        ByteArrayOutputStream nestedBaos = new ByteArrayOutputStream();
        try (ZipOutputStream nestedZos = new ZipOutputStream(nestedBaos)) {
            nestedZos.putNextEntry(new ZipEntry("inner_empty/"));
            nestedZos.closeEntry();
            nestedZos.putNextEntry(new ZipEntry("deep.zip"));
            nestedZos.write(deepBaos.toByteArray());
            nestedZos.closeEntry();
        }
        try (OutputStream os = Files.newOutputStream(tempFile);
             ZipOutputStream zos = new ZipOutputStream(os)) {
            zos.putNextEntry(new ZipEntry("empty_outer/"));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("nested.zip"));
            zos.write(nestedBaos.toByteArray());
            zos.closeEntry();
        }
        return tempFile;
    }

    @Test
    void load_whenEmptyFilesExist_queriesReturnCorrectResults() throws IOException {
        // Arrange: Create a zip with empty files at various levels
        Path testZip = createComplexTestZipWithEmptyFiles();

        // Act
        EffectiveSpork spork = EffectiveSpork.load(testZip);

        // Assert
        assertThat(spork.hasEmptyFile()).isTrue();
        assertThat(spork.findEmptyFiles()).containsExactlyInAnyOrder(
                "//empty.txt",
                "//nested.zip/inner_empty.txt"
        );

        // Cleanup
        Files.delete(testZip);
    }

    private Path createComplexTestZipWithEmptyFiles() throws IOException {
        Path tempFile = Files.createTempFile("test-empty-files-", ".zip");

        // Create inner zip (nested.zip) with an empty file
        ByteArrayOutputStream nestedBaos = new ByteArrayOutputStream();
        try (ZipOutputStream nestedZos = new ZipOutputStream(nestedBaos)) {
            nestedZos.putNextEntry(new ZipEntry("inner_empty.txt"));
            nestedZos.closeEntry(); // 0 bytes
        }

        // Create outer zip with an empty file and the nested zip
        try (OutputStream os = Files.newOutputStream(tempFile);
             ZipOutputStream zos = new ZipOutputStream(os)) {
            zos.putNextEntry(new ZipEntry("empty.txt"));
            zos.closeEntry(); // 0 bytes
            zos.putNextEntry(new ZipEntry("nested.zip"));
            zos.write(nestedBaos.toByteArray());
            zos.closeEntry();
        }

        return tempFile;
    }

    @Test
    void givenArchiveWithVariousFileSizes_whenProcessingWithLargeFileVisitor_thenOnlyLargeFileIsFound() throws IOException {
        // Given: A ZIP archive exists with a mix of small, medium, and large files.
        Path testZip = createZipWithVariousFileSizes();
        EffectiveSpork spork = EffectiveSpork.load(testZip);

        // When: The user processes the archive with a visitor designed to find large files.
        List<String> largeFiles = spork.findFilesLargerThan(100);

        // Then: The result from the visitor should contain only the path to the large file.
        assertThat(largeFiles)
                .isNotNull()
                .hasSize(1)
                .containsExactly("//large.txt");

        // Cleanup
        Files.delete(testZip);
    }

    /**
     * Helper method to create a zip with specific file sizes for testing.
     */
    private Path createZipWithVariousFileSizes() throws IOException {
        Path tempFile = Files.createTempFile("test-sizes-", ".zip");
        try (OutputStream os = Files.newOutputStream(tempFile);
             ZipOutputStream zos = new ZipOutputStream(os)) {

            zos.putNextEntry(new ZipEntry("small.txt"));
            zos.write(new byte[50]); // Smaller than threshold
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("medium.txt"));
            zos.write(new byte[100]); // Exactly at threshold
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("large.txt"));
            zos.write(new byte[150]); // Larger than threshold
            zos.closeEntry();
        }
        return tempFile;
    }
}