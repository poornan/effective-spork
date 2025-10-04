package lk.org.inception;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * The main public API (Facade) for the effective-spork library.
 * This class provides a simple entry point for all functionality.
 */
public class EffectiveSpork {

    private final ArchiveNode rootNode;

    private EffectiveSpork(ArchiveNode rootNode) {
        this.rootNode = rootNode;
    }

    /**
     * Loads and parses a ZIP file, including any nested archives.
     * @param zipPath The path to the ZIP file.
     * @return An EffectiveSpork instance ready for querying.
     * @throws IOException If there is an error reading the file.
     */
    public static EffectiveSpork load(Path zipPath) throws IOException {
        ZipTreeBuilder builder = new ZipTreeBuilder();
        ArchiveNode root = builder.buildTree(zipPath);
        return new EffectiveSpork(root);
    }

    /**
     * @return The root ArchiveNode of the parsed tree.
     */
    public ArchiveNode getRootNode() {
        return this.rootNode;
    }

    /**
     * Generates a formatted string representation of the entire archive tree.
     * @return A multi-line string visualizing the tree.
     */
    public String getTreeAsString() {
        return this.rootNode.getTreeAsString();
    }

    /**
     * Finds all empty directories within the archive.
     * @return A list of full paths to each empty directory.
     */
    public List<String> findEmptyDirectories() {
        return ArchiveQueryEngine.findEmptyDirectories(this.rootNode);
    }

    /**
     * Quickly checks if the archive contains at least one empty directory.
     * @return true if an empty directory is found, false otherwise.
     */
    public boolean hasEmptyDirectory() {
        return ArchiveQueryEngine.hasEmptyDirectory(this.rootNode);
    }
}