package lk.org.inception;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                placeEntryInTree(root, entry, zis);
                zis.closeEntry();
            }
        }
        return root;
    }

    private void placeEntryInTree(ArchiveNode root, ZipEntry entry, ZipInputStream zis) throws IOException {
        Path path = Paths.get(entry.getName());
        ArchiveNode currentNode = root;

        // Traverse or create parent directories
        for (int i = 0; i < path.getNameCount() - 1; i++) {
            String part = path.getName(i).toString();
            currentNode = currentNode.getChildren().computeIfAbsent(part, name -> new ArchiveNode(name, null));
        }

        // Create and place the final node
        String finalName = path.getFileName().toString();
        ArchiveNode newNode = new ArchiveNode(finalName, entry);
        currentNode.getChildren().put(finalName, newNode);
    }
}
