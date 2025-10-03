# effective-spork

[](https://www.google.com/search?q=https://github.com/user/effective-spork)
[](https://opensource.org/licenses/MIT)
[](https://www.google.com/search?q=https://search.maven.org/artifact/com.example/zip-tree-builder)

`effective-spork` is a lightweight, zero-dependency Java library for deep analysis of ZIP archives. Its primary function is to build a complete, in-memory tree representation of a ZIP file's structure, with first-class support for nested archives.

-----

## 🎯 Core Utility

The main utility of `effective-spork` is to solve the common but surprisingly difficult problem of analyzing the contents of ZIP files that contain other ZIP files. It does this by parsing the entire archive structure into an `ArchiveNode` tree.

Once the tree is built, you can:

  * **Visualize** the complete, nested hierarchy of the archive.
  * **Traverse** the structure programmatically to perform complex queries.
  * **Access** all `ZipEntry` metadata for every file and folder.
  * **Analyze** archive contents without writing any temporary files to disk.

-----

## 💥 The Pain Point

This library was created to address significant limitations and complexities in Java's standard libraries when dealing with nested archives.

1.  **`java.nio.file.FileSystem` (ZipFileSystem):**

      * **The Problem:** The modern `ZipFileSystem` provider **cannot handle nested archives**. It can only mount a ZIP file that exists on the primary disk file system. Attempting to mount a virtual `Path` to a ZIP file inside another `ZipFileSystem` fails.
      * **The Consequence:** The standard workaround is to extract the inner ZIP to a temporary file on disk and then mount it. This introduces slow disk I/O, is inefficient, and requires careful cleanup of temporary files.

2.  **`java.util.zip.ZipInputStream`:**

      * **The Problem:** While this stream-based API is performant and avoids temporary files, it presents the archive as a **flat, sequential list of entries**. Reconstructing the intuitive parent-child hierarchy from this flat list is a non-trivial task.
      * **The Consequence:** Developers have to write complex, stateful, and often buggy logic to handle path splitting, create implicit parent directories, and manage nested streams. As we discovered, this can easily lead to subtle bugs like `IOException: Stream closed` or incorrect path parsing if not handled with extreme care.

`effective-spork` solves these pain points by providing a robust, TDD-validated implementation that does the hard work for you.

-----

## Alternatives Considered

Before creating `effective-spork`, several alternatives were considered. Understanding these helps clarify the project's niche.

  * **Apache Commons Compress:** A powerful and mature library for handling many archive formats. It provides excellent low-level tools that can be used to build a solution like `effective-spork`. It is a fantastic choice but requires the developer to assemble the stream-handling and tree-building logic themselves.
  * **TrueVFS (formerly TrueZIP):** A heavyweight framework that provides a true virtual file system, transparently handling nested archives through the standard `java.nio.file.Path` API. It is an extremely powerful solution but can be overkill for projects that only need to inspect archives, introducing significant complexity and dependencies.
  * **Manual Implementation:** As demonstrated in our TDD process, building this from scratch is possible but fraught with subtle challenges related to stream management and recursion.

`effective-spork` aims to be the middle ground: a lightweight, focused utility that provides the high-level tree abstraction without the overhead of a full VFS framework or requiring the user to write complex parsing logic.

-----

## 🚀 Getting Started

The library is designed to be simple to use. The workflow involves two main classes: `ZipTreeBuilder` to parse the file, and `ArchiveQueryEngine` to analyze the resulting tree.

```java
import com.example.ziptree.ArchiveNode;
import com.example.ziptree.ArchiveQueryEngine;
import com.example.ziptree.ZipTreeBuilder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SporkExample {
    public static void main(String[] args) throws Exception {
        // 1. Point to your zip file
        Path myArchive = Paths.get("path/to/your/complex-archive.zip");

        // 2. Build the in-memory tree from the archive
        ZipTreeBuilder builder = new ZipTreeBuilder();
        ArchiveNode root = builder.buildTree(myArchive);
        
        // 3. (Optional) Print the tree to visualize its structure
        System.out.println("--- Archive Structure ---");
        root.printTree("");

        // 4. Query the tree to find all empty directories
        ArchiveQueryEngine queryEngine = new ArchiveQueryEngine();
        System.out.println("\n--- Finding Empty Directories ---");
        List<String> emptyDirs = queryEngine.findEmptyDirectories(root);
        
        if (emptyDirs.isEmpty()) {
            System.out.println("No empty directories found.");
        } else {
            emptyDirs.forEach(System.out::println);
        }
    }
}
```

-----

## 🛠️ Development

This project was built with a strict Test-Driven Development (TDD) approach to ensure robustness and correctness.

To run the full test suite, execute the following Maven command:

```bash
mvn clean test
```
