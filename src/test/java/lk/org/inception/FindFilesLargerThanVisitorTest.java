package lk.org.inception;

import lk.org.inception.visitors.FindFilesLargerThanVisitor;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.zip.ZipEntry;
import static org.assertj.core.api.Assertions.assertThat;

class FindFilesLargerThanVisitorTest {

    @Test
    void visit_whenFileIsLargerThanThreshold_addsItToResults() {
        // Arrange: Create a visitor with a 100-byte threshold.
        // This line will fail to compile, putting us in the "Red" state.
        FindFilesLargerThanVisitor visitor = new FindFilesLargerThanVisitor(100);

        // Arrange: Create a set of nodes to test against.
        ArchiveNode largeFileNode = createTestNode("large.txt", 150, false);
        ArchiveNode exactFileNode = createTestNode("exact.txt", 100, false);
        ArchiveNode smallFileNode = createTestNode("small.txt", 50, false);
        ArchiveNode emptyDirNode = createTestNode("emptyDir/", 0, true);
        ArchiveNode implicitDirNode = new ArchiveNode("implicitDir", null); // Has no entry

        // Act: Manually "visit" each node as if a traversal were happening.
        visitor.visit(largeFileNode, "//large.txt");
        visitor.visit(exactFileNode, "//exact.txt");
        visitor.visit(smallFileNode, "//small.txt");
        visitor.visit(emptyDirNode, "//emptyDir");
        visitor.visit(implicitDirNode, "//implicitDir");

        // Assert: The result should contain ONLY the path to the large file.
        List<String> largeFiles = visitor.getResult();

        assertThat(largeFiles)
                .isNotNull()
                .hasSize(1)
                .containsExactly("//large.txt");
    }

    /** Helper method to create mock ArchiveNodes for this test. */
    private ArchiveNode createTestNode(String name, long size, boolean isDirectory) {
        ZipEntry entry = new ZipEntry(name);
        entry.setSize(size);
        if (isDirectory) {
            // A directory entry in a zip file has a size of 0.
            entry.setSize(0);
        }
        return new ArchiveNode(name, entry);
    }
}