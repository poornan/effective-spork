# effective-spork

[](https://www.google.com/search?q=https://github.com/user/effective-spork)
[](https://opensource.org/licenses/MIT)
[](https://www.google.com/search?q=https://search.maven.org/artifact/com.example/zip-tree-builder)

[![Java CI with Maven](https://github.com/poornan/effective-spork/actions/workflows/maven.yml/badge.svg)](https://github.com/poornan/effective-spork/actions/workflows/maven.yml)

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

Using effective-spork` is designed to be simple and intuitive through its main `EffectiveSpork` class. This class acts as a single entry point (a facade) for all parsing and querying operations.

```java
import lk.org.inception.EffectiveSpork;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SporkExample {
    public static void main(String[] args) throws Exception {
        // 1. Point to your zip file
        Path myArchive = Paths.get("path/to/your/complex-archive.zip");

        // 2. Load the archive using the simple facade. This does all the parsing.
        EffectiveSpork spork = EffectiveSpork.load(myArchive);

        // 3. (Optional) Print the entire tree structure to the console
        System.out.println("--- Archive Structure ---");
        System.out.println(spork.getTreeAsString());

        // 4. Efficiently check if an empty directory exists
        System.out.println("\n--- Checking for Empty Directories ---");
        if (spork.hasEmptyDirectory()) {
            System.out.println("✅ An empty directory was found! Here is the full list:");
            List<String> emptyDirs = spork.findEmptyDirectories();
            emptyDirs.forEach(System.out::println);
        } else {
            System.out.println("❌ No empty directories were found in the archive.");
        }

        // 5. Efficiently check if an empty file exists
        System.out.println("\n--- Checking for Empty Files ---");
        if (spork.hasEmptyFile()) {
            System.out.println("✅ An empty file was found! Here is the full list:");
            List<String> emptyFiles = spork.findEmptyFiles();
            emptyFiles.forEach(System.out::println);
        } else {
            System.out.println("❌ No empty files were found in the archive.");
        }

        // 6. Find files larger than a specific size
        System.out.println("\n--- Checking for Files Larger Than 1 KB ---");
        long oneKilobyte = 1024;
        List<String> largeFiles = spork.findFilesLargerThan(oneKilobyte);

        if (!largeFiles.isEmpty()) {
            System.out.println("✅ Found " + largeFiles.size() + " file(s) larger than 1 KB:");
            largeFiles.forEach(System.out::println);
        } else {
            System.out.println("❌ No files larger than 1 KB were found.");
        }
    }
}
```
-----

## 📦 Using as a Dependency

`effective-spork` is hosted on GitHub Packages. To use it in your own Maven project, you need to configure your `pom.xml` and `settings.xml` files.

### Step 1: Add the Repository to your `pom.xml`

Add this repository configuration to your `pom.xml`. This tells Maven where to find the `effective-spork` package. **Remember to replace `OWNER` and `REPOSITORY`** with the appropriate GitHub username/organization and repo name.

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub OWNER Apache Maven Packages</name>
        <url>https://maven.pkg.github.com/poornan/effective-spork</url>
    </repository>
</repositories>
```

### Step 2: Add the Dependency to your `pom.xml`

Add the `effective-spork` dependency to your `pom.xml`.

```xml
<dependencies>
    <dependency>
        <groupId>lk.org.inception</groupId>
        <artifactId>effective-spork</artifactId>
        <version>2.0.0.2</version>
    </dependency>
</dependencies>
```

### Step 3: Authenticate with GitHub Packages

You need to authenticate by adding your GitHub credentials to your `settings.xml` file, which is typically located at `~/.m2/settings.xml`.

1.  **Create a Personal Access Token (PAT)** on GitHub with the `read:packages` scope. You can find instructions here: [Creating a personal access token](https://www.google.com/search?q=https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens%23creating-a-personal-access-token-classic).
2.  **Add a server entry** to your `settings.xml` file. The `<id>` must match the repository ID from your `pom.xml` (in this case, `github`).

<!-- end list -->

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_PAT</password>
    </server>
  </servers>
</settings>
```

After these steps, running `mvn install` in your project will successfully download the `effective-spork` dependency from GitHub Packages.
-----

## 🛠️ Development

This project was built with a strict Test-Driven Development (TDD) approach to ensure robustness and correctness.

To run the full test suite, execute the following Maven command:

```bash
mvn clean test
```
