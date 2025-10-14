// src/test/java/lk/org/inception/JolMemoryTest.java
package lk.org.inception;

import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.GraphLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class JolMemoryTest {

    @Test
    void calculateMemoryFootprintWithJol() throws IOException {
        // Arrange
        Path testZip = createTestZip();
        EffectiveSpork spork = EffectiveSpork.load(testZip);

        // Act: Use JOL to parse the object graph and get its total size
        long totalSizeInBytes = GraphLayout.parseInstance(spork).totalSize();
        double sizeInKb = totalSizeInBytes / 1024.0;

        System.out.printf("[JOL] Memory footprint of effective-spork: %d bytes (%.2f KB)%n",
                totalSizeInBytes, sizeInKb);

        // Assert: Check that the size is a plausible, non-zero number
        assertThat(totalSizeInBytes).isGreaterThan(1000L);

        Files.delete(testZip);
    }

    private Path createTestZip() throws IOException {
        Path tempFile = Files.createTempFile("test-mem-", ".zip");
        try (OutputStream os = Files.newOutputStream(tempFile);
             ZipOutputStream zos = new ZipOutputStream(os)) {
            zos.putNextEntry(new ZipEntry("dir1/file1.txt"));
            zos.write(new byte[10*1024]);
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("dir2/file2.txt"));
            zos.write(new byte[20*2048]);
            zos.closeEntry();
        }
        return tempFile;
    }
}