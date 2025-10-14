package lk.org.inception.visitors;

import lk.org.inception.ArchiveNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A visitor that finds all empty directories in an archive tree.
 */
public class FindEmptyDirectoriesVisitor implements TreeVisitor<List<String>> {
    private final List<String> results = new ArrayList<>();

    @Override
    public void visit(ArchiveNode node, String path) {
        if (node.isDirectory() && node.getChildren().isEmpty()) {
            results.add(path);
        }
    }

    @Override
    public List<String> getResult() {
        return results;
    }
}
