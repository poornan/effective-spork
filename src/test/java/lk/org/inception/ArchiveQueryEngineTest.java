package lk.org.inception;

import org.junit.jupiter.api.BeforeEach;
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

class ArchiveQueryEngineTest {

    private ArchiveQueryEngine queryEngine;
    private ZipTreeBuilder treeBuilder;

    @BeforeEach
    void setUp() {
        queryEngine = new ArchiveQueryEngine();
        treeBuilder = new ZipTreeBuilder();
    }

    @Test
    void findEmptyDirectories_findsAllEmptyDirsInComplexZip() throws IOException {
        // Arrange: Create a complex zip file with multiple empty dirs
        Path testZip = createComplexTestZip();
        ArchiveNode root = treeBuilder.buildTree(testZip);

        // Act
        List<String> emptyDirs = queryEngine.findEmptyDirectories(root);

        // Assert
        assertThat(emptyDirs).containsExactlyInAnyOrder(
                "//empty_outer",
                "//nested.zip/inner_empty",
                "//nested.zip/deep.zip/deep_empty"
        );

        // Cleanup
        Files.delete(testZip);
    }

    @Test
    void hasEmptyDirectory_returnsTrue_whenEmptyDirExists() throws IOException {
        // Arrange
        Path testZip = createComplexTestZip();
        ArchiveNode root = treeBuilder.buildTree(testZip);

        // Act & Assert
        assertThat(queryEngine.hasEmptyDirectory(root)).isTrue();

        // Cleanup
        Files.delete(testZip);
    }

    @Test
    void hasEmptyDirectory_returnsFalse_whenNoEmptyDirExists() throws IOException {
        // Arrange: Create a zip with no empty folders
        Path testZip = Files.createTempFile("test-no-empty-", ".zip");
        try (OutputStream os = Files.newOutputStream(testZip);
             ZipOutputStream zos = new ZipOutputStream(os)) {
            zos.putNextEntry(new ZipEntry("dir/file.txt"));
            zos.write("content".getBytes());
            zos.closeEntry();
        }
        ArchiveNode root = treeBuilder.buildTree(testZip);

        // Act & Assert
        assertThat(queryEngine.hasEmptyDirectory(root)).isFalse();

        // Cleanup
        Files.delete(testZip);
    }

    /**
     * Helper method to create a zip file with a known structure for testing.
     */
    private Path createComplexTestZip() throws IOException {
        Path tempFile = Files.createTempFile("test-complex-", ".zip");

        // Create inner-most zip (deep.zip)
        ByteArrayOutputStream deepBaos = new ByteArrayOutputStream();
        try (ZipOutputStream deepZos = new ZipOutputStream(deepBaos)) {
            deepZos.putNextEntry(new ZipEntry("deep_empty/"));
            deepZos.closeEntry();
        }

        // Create middle zip (nested.zip)
        ByteArrayOutputStream nestedBaos = new ByteArrayOutputStream();
        try (ZipOutputStream nestedZos = new ZipOutputStream(nestedBaos)) {
            nestedZos.putNextEntry(new ZipEntry("inner_empty/"));
            nestedZos.closeEntry();
            nestedZos.putNextEntry(new ZipEntry("deep.zip"));
            nestedZos.write(deepBaos.toByteArray());
            nestedZos.closeEntry();
        }

        // Create outer zip
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
}