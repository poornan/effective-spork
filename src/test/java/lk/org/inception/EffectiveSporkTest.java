package lk.org.inception;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
}