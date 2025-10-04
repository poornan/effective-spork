package lk.org.inception;

import org.junit.jupiter.api.Test;
import java.util.zip.ZipEntry;

import static org.assertj.core.api.Assertions.assertThat;

class ArchiveNodeTest {

    @Test
    void getTreeAsString_generatesCorrectStringRepresentation_forComplexTree() {
        // Arrange: Manually build a complex tree structure.
        ArchiveNode root = new ArchiveNode("/", null);

        // -- file in root --
        ZipEntry fileEntry = new ZipEntry("file1.txt");
        fileEntry.setSize(123);
        root.getChildren().put("file1.txt", new ArchiveNode("file1.txt", fileEntry));

        // -- empty dir in root --
        ZipEntry emptyDirEntry = new ZipEntry("empty_dir/");
        root.getChildren().put("empty_dir", new ArchiveNode("empty_dir", emptyDirEntry));

        // -- nested archive --
        ZipEntry nestedZipEntry = new ZipEntry("nested.zip");
        nestedZipEntry.setSize(456);
        ArchiveNode nestedZipNode = new ArchiveNode("nested.zip", nestedZipEntry);

        // -- nested archive's content --
        ArchiveNode nestedRoot = new ArchiveNode("/", null);
        ZipEntry innerFileEntry = new ZipEntry("inner.txt");
        innerFileEntry.setSize(78);
        nestedRoot.getChildren().put("inner.txt", new ArchiveNode("inner.txt", innerFileEntry));
        nestedZipNode.setNestedArchiveRoot(nestedRoot);

        root.getChildren().put("nested.zip", nestedZipNode);

        // Act: Call the new, testable method (which doesn't exist yet).
        String actualOutput = root.getTreeAsString();
        System.out.println(actualOutput);

        // Assert: Check against a precisely formatted expected string.
        String expectedOutput = String.join(System.lineSeparator(),
                "📁 /",
                "   📄 file1.txt (size: 123)",
                "   📁 empty_dir",
                "   📄 nested.zip (size: 456)",
                "     +-- [Nested Archive: nested.zip]",
                "     |  📁 /",
                "     |     📄 inner.txt (size: 78)"
        );

        assertThat(actualOutput).isEqualTo(expectedOutput);
    }
}