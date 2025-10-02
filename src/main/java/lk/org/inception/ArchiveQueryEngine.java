package lk.org.inception;

import java.util.ArrayList;
import java.util.List;

public class ArchiveQueryEngine {

    /**
     * Traverses the entire tree starting from a given node and finds all empty directories.
     * @param startNode The root node of the tree or sub-tree to search.
     * @return A list of full paths to each empty directory found.
     */
    public List<String> findEmptyDirectories(ArchiveNode startNode) {
        List<String> results = new ArrayList<>();
        findEmptyDirsRecursive(startNode, "", results);
        return results;
    }

    /**
     * Recursively traverses the tree to find and collect paths of empty directories.
     */
    private void findEmptyDirsRecursive(ArchiveNode currentNode, String currentPath, List<String> results) {
        // Construct the full path for the current node
        String nodePath = currentPath.isEmpty() ?
                currentNode.getName() :
                currentPath + "/" + currentNode.getName();

        // Check if the current node is an empty directory
        if (currentNode.isDirectory() && currentNode.getChildren().isEmpty()) {
            results.add(nodePath);
        }

        // If the node represents a nested archive, traverse its sub-tree
        if (currentNode.getNestedArchiveRoot() != null) {
            for (ArchiveNode child : currentNode.getNestedArchiveRoot().getChildren().values()) {
                findEmptyDirsRecursive(child, nodePath, results);
            }
        }

        // Traverse all children of the current node
        for (ArchiveNode child : currentNode.getChildren().values()) {
            findEmptyDirsRecursive(child, nodePath, results);
        }
    }

    /**
     * Quickly checks if a tree contains at least one empty directory.
     * This method is optimized to stop and return true as soon as the first one is found.
     * @param startNode The root node of the tree or sub-tree to check.
     * @return true if an empty directory is found, false otherwise.
     */
    public boolean hasEmptyDirectory(ArchiveNode startNode) {
        // Check if the current node itself is an empty directory
        if (startNode.isDirectory() && startNode.getChildren().isEmpty()) {
            return true;
        }

        // Check inside a nested archive if one exists
        if (startNode.getNestedArchiveRoot() != null) {
            if (hasEmptyDirectory(startNode.getNestedArchiveRoot())) {
                return true; // Found one, short-circuit
            }
        }

        // Check all children
        for (ArchiveNode child : startNode.getChildren().values()) {
            if (hasEmptyDirectory(child)) {
                return true; // Found one, short-circuit
            }
        }

        // Scanned the entire branch and found nothing
        return false;
    }
}