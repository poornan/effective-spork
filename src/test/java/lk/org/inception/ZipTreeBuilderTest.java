package lk.org.inception;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ZipTreeBuilderTest {

    private Path tempZipFile;
    private ZipTreeBuilder builder;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary file for our test zip
        tempZipFile = Files.createTempFile("test-", ".zip");
        builder = new ZipTreeBuilder();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempZipFile);
    }

    @Test
    void buildTree_withSingleFile_createsCorrectNode() throws IOException {
        // Arrange: Create a zip with one file
        try (OutputStream os = Files.newOutputStream(tempZipFile);
             ZipOutputStream zos = new ZipOutputStream(os)) {
            zos.putNextEntry(new ZipEntry("hello.txt"));
            zos.write("content".getBytes());
            zos.closeEntry();
        }

        // Act: This method doesn't exist yet, so it won't compile.
        ArchiveNode root = builder.buildTree(tempZipFile);

        // Assert
        assertThat(root).isNotNull();
        assertThat(root.getChildren()).hasSize(1);

        ArchiveNode fileNode = root.getChildren().get("hello.txt");
        assertThat(fileNode.getName()).isEqualTo("hello.txt");
        assertThat(fileNode.isDirectory()).isFalse();
        assertThat(fileNode.getEntry().getSize()).isEqualTo(7);
    }

    @Test
    void buildTree_withFileInSubdirectory_createsParentNode() throws IOException {
        // Arrange
        try (OutputStream os = Files.newOutputStream(tempZipFile);
             ZipOutputStream zos = new ZipOutputStream(os)) {
            zos.putNextEntry(new ZipEntry("dir1/hello.txt"));
            zos.write("content".getBytes());
            zos.closeEntry();
        }

        // Act
        ArchiveNode root = builder.buildTree(tempZipFile);

        // Assert
        assertThat(root.getChildren()).hasSize(1);

        ArchiveNode dirNode = root.getChildren().get("dir1");
        assertThat(dirNode).isNotNull();
        assertThat(dirNode.getName()).isEqualTo("dir1");
        // This will fail: Our old code doesn't create implicit directories.
        assertThat(dirNode.isDirectory()).isTrue();

        assertThat(dirNode.getChildren()).hasSize(1);
        ArchiveNode fileNode = dirNode.getChildren().get("hello.txt");
        assertThat(fileNode).isNotNull();
        assertThat(fileNode.getName()).isEqualTo("hello.txt");
    }
}
