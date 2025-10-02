package lk.org.inception;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipTreeBuilder {
    public ArchiveNode buildTree(Path zipPath) throws IOException {
        try (InputStream fis = Files.newInputStream(zipPath)) {
            return buildTreeFromStream(fis);
        }
    }

    private ArchiveNode buildTreeFromStream(InputStream is) throws IOException {
        ArchiveNode root = new ArchiveNode("/", null);
        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // For this simple test, we don't need path splitting yet.
                ArchiveNode newNode = new ArchiveNode(entry.getName(), entry);
                root.getChildren().put(entry.getName(), newNode);
                zis.closeEntry();
            }
        }
        return root;
    }
}
