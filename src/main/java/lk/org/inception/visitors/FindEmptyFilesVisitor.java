package lk.org.inception.visitors;

import lk.org.inception.ArchiveNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A visitor that finds all empty files (size 0) in an archive tree.
 */
public class FindEmptyFilesVisitor implements TreeVisitor<List<String>> {
    private final List<String> results = new ArrayList<>();

    @Override
    public void visit(ArchiveNode node, String path) {
        // An empty file is not a directory, has a valid entry, and its size is 0.
        if (!node.isDirectory() && node.getEntry() != null && node.getEntry().getSize() == 0) {
            results.add(path);
        }
    }

    @Override
    public List<String> getResult() {
        return results;
    }
}