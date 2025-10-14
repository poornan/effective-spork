package lk.org.inception.visitors;

import lk.org.inception.ArchiveNode;

/**
 * Defines the contract for a visitor that can traverse an ArchiveNode tree.
 * @param <R> The type of the result this visitor produces.
 */
public interface TreeVisitor<R> {
    /**
     * Called for each node in the tree during traversal.
     * @param node The current ArchiveNode being visited.
     * @param path The full, constructed path to the current node.
     */
    void visit(ArchiveNode node, String path);

    /**
     * @return The final result of the traversal.
     */
    R getResult();
}