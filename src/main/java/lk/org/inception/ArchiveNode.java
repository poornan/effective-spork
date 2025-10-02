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
    }

}