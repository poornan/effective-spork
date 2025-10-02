package lk.org.inception;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipTreeBuilder {

    /**
     * A helper class that wraps an InputStream and prevents it from being closed.
     * This is crucial for handling nested streams.
     */
    private static class NonClosingInputStream extends FilterInputStream {
        public NonClosingInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() {
            // Do nothing, shielding the underlying stream from being closed.
        }
    }

    /**
     * Public entry point. It is responsible for creating and closing the initial stream.
     */
    public ArchiveNode buildTree(Path zipPath) throws IOException {
        try (InputStream fis = Files.newInputStream(zipPath)) {
            return buildTreeFromStream(fis);
        }
    }

    /**
     * This method correctly creates a ZipInputStream for each archive level
     * and uses the NonClosingInputStream wrapper for recursion.
     */
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

        for (int i = 0; i < path.getNameCount() - 1; i++) {
            String part = path.getName(i).toString();
            currentNode = currentNode.getChildren().computeIfAbsent(part, name -> new ArchiveNode(name, null));
        }

        String finalName = path.getFileName().toString();
        ArchiveNode newNode = new ArchiveNode(finalName, entry);
        currentNode.getChildren().put(finalName, newNode);

        if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".zip")) {
            // THE FIX:
            // We are positioned to read the nested zip's data from 'zis'.
            // We pass 'zis' to the recursive call, but shield it from being closed.
            ArchiveNode nestedTree = buildTreeFromStream(new NonClosingInputStream(zis));
            newNode.setNestedArchiveRoot(nestedTree);
        }
    }
}