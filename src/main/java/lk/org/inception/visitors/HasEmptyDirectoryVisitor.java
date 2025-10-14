package lk.org.inception.visitors;

import lk.org.inception.ArchiveNode;

/**
 * A visitor that quickly checks for the existence of any empty directory.
 * It stops traversing once the first empty directory is found.
 */
public class HasEmptyDirectoryVisitor implements TreeVisitor<Boolean> {
    private boolean found = false;

    @Override
    public void visit(ArchiveNode node, String path) {
        // Stop processing if we've already found what we're looking for.
        if (found) {
            return;
        }
        if (node.isDirectory() && node.getChildren().isEmpty()) {
            found = true;
        }
    }

    @Override
    public Boolean getResult() {
        return found;
    }
}