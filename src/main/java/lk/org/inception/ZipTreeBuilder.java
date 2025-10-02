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

    /**
     * Public entry point. It is responsible for creating and closing the stream.
     */
    public ArchiveNode buildTree(Path zipPath) throws IOException {
        try (InputStream fis = Files.newInputStream(zipPath)) {
            // The one and only ZipInputStream is created here
            try (ZipInputStream zis = new ZipInputStream(fis)) {
                // We now pass the already-created stream to the recursive method
                return buildTreeFromStream(zis);
            }
        }
    }

    /**
     * This method now processes a ZipInputStream but does NOT close it.
     * It's designed to be called recursively on a stream that is already open.
     */
    private ArchiveNode buildTreeFromStream(ZipInputStream zis) throws IOException {
        ArchiveNode root = new ArchiveNode("/", null);

        // NO try-with-resources here. The stream is managed by the caller.
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            placeEntryInTree(root, entry, zis);
            zis.closeEntry();
        }
        return root;
    }

    private void placeEntryInTree(ArchiveNode root, ZipEntry entry, ZipInputStream zis) throws IOException {
        Path path = Paths.get(entry.getName());
        ArchiveNode currentNode = root;

        for (int i = 0; i < path.getNameCount() - 1; i++) {
            String part = path.getName(i).toString();
            currentNode = currentNode.getChildren().computeIfAbsent(part, name -> new ArchiveNode(name, null));
        }

        String finalName = path.getFileName().toString();
        ArchiveNode newNode = new ArchiveNode(finalName, entry);
        currentNode.getChildren().put(finalName, newNode);

        if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".zip")) {
            // The recursive call now works because it won't close the 'zis' stream.
            ArchiveNode nestedTree = buildTreeFromStream(zis);
            newNode.setNestedArchiveRoot(nestedTree);
        }
    }
}