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

    private ZipTreeBuilder treeBuilder;

    @BeforeEach
    void setUp() {
        treeBuilder = new ZipTreeBuilder();
    }

    @Test
    void findEmptyDirectories_findsAllEmptyDirsInComplexZip() throws IOException {
        // Arrange: Create a complex zip file with multiple empty dirs
        Path testZip = createComplexTestZip();
        ArchiveNode root = treeBuilder.buildTree(testZip);

        // Act
        List<String> emptyDirs = ArchiveQueryEngine.findEmptyDirectories(root);

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
        assertThat(ArchiveQueryEngine.hasEmptyDirectory(root)).isTrue();

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
        assertThat(ArchiveQueryEngine.hasEmptyDirectory(root)).isFalse();

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

    @Test
    void findEmptyFiles_findsAllEmptyFilesInComplexZip() throws IOException {
        // Arrange: Create a zip with empty files at various levels
        Path testZip = createComplexTestZipWithEmptyFiles();
        ArchiveNode root = treeBuilder.buildTree(testZip);

        // Act
        List<String> emptyFiles = ArchiveQueryEngine.findEmptyFiles(root);

        // Assert
        assertThat(emptyFiles).containsExactlyInAnyOrder(
                "//empty.txt",
                "//nested.zip/inner_empty.txt"
        );

        // Cleanup
        Files.delete(testZip);
    }

    @Test
    void hasEmptyFile_returnsTrue_whenEmptyFileExists() throws IOException {
        // Arrange
        Path testZip = createComplexTestZipWithEmptyFiles();
        ArchiveNode root = treeBuilder.buildTree(testZip);

        // Act & Assert
        assertThat(ArchiveQueryEngine.hasEmptyFile(root)).isTrue();

        // Cleanup
        Files.delete(testZip);
    }

    @Test
    void hasEmptyFile_returnsFalse_whenNoEmptyFileExists() throws IOException {
        // Arrange: Use the existing complex zip helper which has no empty files
        Path testZip = createComplexTestZip();
        ArchiveNode root = treeBuilder.buildTree(testZip);

        // Act & Assert
        assertThat(ArchiveQueryEngine.hasEmptyFile(root)).isFalse();

        // Cleanup
        Files.delete(testZip);
    }

    /**
     * Helper method to create a zip file with empty files for testing.
     */
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
    void findEmptyFiles_findsFileInDoublyNestedZip() throws IOException {
        // Arrange: Create the deeply nested zip file structure
        Path testZip = createDoublyNestedTestZip();
        ArchiveNode root = treeBuilder.buildTree(testZip);

        // Act
        List<String> emptyFiles = ArchiveQueryEngine.findEmptyFiles(root);

        // Assert
        assertThat(emptyFiles)
                .isNotNull()
                .hasSize(1)
                .containsExactly("//dir1/dir2/innerZip.zip/dir3/innnerinnerzip.zip/dir4/empty.txt");

        // Cleanup
        Files.delete(testZip);
    }

    /**
     * Helper to create the structure:
     * test.zip/dir1/dir2/innerZip.zip/dir3/innnerinnerzip.zip/dir4/empty.txt
     */
    private Path createDoublyNestedTestZip() throws IOException {
        Path finalZipFile = Files.createTempFile("test-doubly-nested-", ".zip");

        // 1. Create the innermost zip (innnerinnerzip.zip) in memory
        ByteArrayOutputStream innerInnerBaos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(innerInnerBaos)) {
            zos.putNextEntry(new ZipEntry("dir4/"));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("dir4/empty.txt"));
            zos.closeEntry(); // 0 bytes
        }
        byte[] innerInnerZipBytes = innerInnerBaos.toByteArray();

        // 2. Create the middle zip (innerZip.zip) in memory
        ByteArrayOutputStream innerBaos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(innerBaos)) {
            zos.putNextEntry(new ZipEntry("dir3/"));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("dir3/innnerinnerzip.zip"));
            zos.write(innerInnerZipBytes);
            zos.closeEntry();
        }
        byte[] innerZipBytes = innerBaos.toByteArray();

        // 3. Create the final, outer zip file
        try (OutputStream os = Files.newOutputStream(finalZipFile);
             ZipOutputStream zos = new ZipOutputStream(os)) {
            zos.putNextEntry(new ZipEntry("dir1/"));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("dir1/dir2/"));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("dir1/dir2/innerZip.zip"));
            zos.write(innerZipBytes);
            zos.closeEntry();
        }

        return finalZipFile;
    }
}