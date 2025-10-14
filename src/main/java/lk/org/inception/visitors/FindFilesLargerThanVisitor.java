package lk.org.inception.visitors;

import lk.org.inception.ArchiveNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A visitor that finds all files larger than a specified size threshold.
 */
public class FindFilesLargerThanVisitor implements TreeVisitor<List<String>> {
    private final long sizeThreshold;
    private final List<String> results = new ArrayList<>();

    /**
     * Constructs a visitor to find files larger than the given size.
     * @param sizeInBytes The size threshold in bytes. Files with a size
     * strictly greater than this value will be found.
     */
    public FindFilesLargerThanVisitor(long sizeInBytes) {
        this.sizeThreshold = sizeInBytes;
    }

    @Override
    public void visit(ArchiveNode node, String path) {
        // The logic to make our test pass:
        // 1. Is it a file (not a directory)?
        // 2. Does it have a valid ZipEntry?
        // 3. Is its size greater than our threshold?
        if (!node.isDirectory() && node.getEntry() != null && node.getEntry().getSize() > this.sizeThreshold) {
            results.add(path);
        }
    }

    @Override
    public List<String> getResult() {
        return results;
    }
}