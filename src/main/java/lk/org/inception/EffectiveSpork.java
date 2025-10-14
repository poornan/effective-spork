package lk.org.inception;

import lk.org.inception.visitors.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
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
        FindEmptyDirectoriesVisitor visitor = new FindEmptyDirectoriesVisitor();
        this.process(visitor);
        return visitor.getResult();
    }

    /**
     * Quickly checks if the archive contains at least one empty directory.
     * @return true if an empty directory is found, false otherwise.
     */
    public boolean hasEmptyDirectory() {
        HasEmptyDirectoryVisitor visitor = new HasEmptyDirectoryVisitor();
        this.process(visitor);
        return visitor.getResult();
    }

    /**
     * Finds all empty files (size 0) within the archive.
     * @return A list of full paths to each empty file.
     */
    public List<String> findEmptyFiles() {
        FindEmptyFilesVisitor visitor = new FindEmptyFilesVisitor();
        this.process(visitor);
        return visitor.getResult();

    }

    /**
     * Quickly checks if the archive contains at least one empty file.
     * @return true if an empty file is found, false otherwise.
     */
    public boolean hasEmptyFile() {
        HasEmptyFileVisitor visitor = new HasEmptyFileVisitor();
        this.process(visitor);
        return visitor.getResult();
    }

    /**
     * Processes the loaded archive tree with one or more visitors.
     * This is the primary method for running custom analysis.
     * @param visitors A list of visitors to run over the tree.
     */
    public void process(TreeVisitor<?>... visitors) {
        traverse(this.rootNode, "", Arrays.asList(visitors));
    }

    /**
     * The recursive engine that walks the tree and applies visitors.
     */
    private void traverse(ArchiveNode currentNode, String currentPath, List<TreeVisitor<?>> visitors) {
        String nodePath = currentPath.isEmpty() ?
                currentNode.getName() :
                currentPath + "/" + currentNode.getName();

        // Apply all visitors to the current node
        for (TreeVisitor<?> visitor : visitors) {
            visitor.visit(currentNode, nodePath);
        }

        // Recurse into the nested archive's children
        if (currentNode.getNestedArchiveRoot() != null) {
            for (ArchiveNode child : currentNode.getNestedArchiveRoot().getChildren().values()) {
                traverse(child, nodePath, visitors);
            }
        }

        // Recurse into the current node's children
        for (ArchiveNode child : currentNode.getChildren().values()) {
            traverse(child, nodePath, visitors);
        }
    }

    /**
     * Finds all files in the archive larger than a specified size.
     * This is the new, simple API method.
     * @param sizeInBytes The size threshold.
     * @return A list of paths to files larger than the threshold.
     */
    public List<String> findFilesLargerThan(long sizeInBytes) {
        // Internally, it uses our powerful visitor pattern...
        FindFilesLargerThanVisitor visitor = new FindFilesLargerThanVisitor(sizeInBytes);
        this.process(visitor);
        // ...but the user doesn't need to know that.
        return visitor.getResult();
    }
}