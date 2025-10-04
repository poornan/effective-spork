package lk.org.inception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

public class ArchiveNode {
    private final String name;
    private final ZipEntry entry;
    private final Map<String, ArchiveNode> children = new LinkedHashMap<>();
    private ArchiveNode nestedArchiveRoot;

    public ArchiveNode(String name, ZipEntry entry) {
        this.name = name;
        this.entry = entry;
    }

    public String getName() {
        return name;
    }

    public ZipEntry getEntry() {
        return entry;
    }

    public Map<String, ArchiveNode> getChildren() {
        return children;
    }

    public boolean isDirectory() {
        return (entry != null && entry.isDirectory()) || !children.isEmpty();
    }

    public ArchiveNode getNestedArchiveRoot() {
        return nestedArchiveRoot;
    }

    public void setNestedArchiveRoot(ArchiveNode nestedArchiveRoot) {
        this.nestedArchiveRoot = nestedArchiveRoot;
    }/**
     * A convenient utility method to print the tree structure to the console.
     * It is a wrapper around the testable getTreeAsString() method.
     */
    public void printTree() {
        System.out.println(getTreeAsString());
    }

    /**
     * Generates a formatted string representation of the archive tree.
     * This method is pure and testable, with no side effects.
     * @return A multi-line string visualizing the tree.
     */
    public String getTreeAsString() {
        StringBuilder builder = new StringBuilder();
        buildTreeString(builder, "");
        return builder.toString().trim(); // Trim trailing newline
    }

    /**
     * Recursive helper to build the tree string with proper indentation.
     */
    private void buildTreeString(StringBuilder builder, String indent) {
        String meta = "";
        if (entry != null && !entry.isDirectory()) {
            meta = String.format(" (size: %d)", entry.getSize());
        }
        builder.append(indent)
                .append(isDirectory() ? "📁 " : "📄 ")
                .append(name)
                .append(meta)
                .append(System.lineSeparator());

        if (nestedArchiveRoot != null) {
            builder.append(indent)
                    .append("  +-- [Nested Archive: ")
                    .append(name)
                    .append("]")
                    .append(System.lineSeparator());
            nestedArchiveRoot.buildTreeString(builder, indent + "  |  ");
        }

        for (ArchiveNode child : children.values()) {
            child.buildTreeString(builder, indent + "   ");
        }
    }

}