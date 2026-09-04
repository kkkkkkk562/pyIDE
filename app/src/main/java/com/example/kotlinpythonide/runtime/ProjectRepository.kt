package com.example.kotlinpythonide.runtime

import android.content.Context
import java.io.File

class ProjectRepository(context: Context) {
    val root: File = File(context.filesDir, "projects").apply { mkdirs() }

    init {
        if (listProjects().isEmpty()) {
            createProject("hello-world")
        }
    }

    fun listProjects(): List<File> =
        root.listFiles()
            ?.filter { it.isDirectory && !it.name.startsWith(".") }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()

    fun projectFiles(project: File): List<File> =
        project.listFiles()
            ?.filter { it.isFile && !it.name.startsWith(".") }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()

    fun readFile(project: File, name: String): String {
        val file = safeChild(project, name)
        return if (file.exists()) file.readText() else ""
    }

    fun writeFile(project: File, name: String, contents: String) {
        safeChild(project, name).apply {
            parentFile?.mkdirs()
            writeText(contents)
        }
    }

    fun createProject(rawName: String): File {
        val name = rawName
            .trim()
            .replace(Regex("[^A-Za-z0-9_-]"), "-")
            .trim('-')
            .ifBlank { "untitled-project" }
        val project = File(root, uniqueName(name))
        project.mkdirs()
        File(project, "main.py").writeText(
            "def main():\n" +
                "    print(\"Hello from Kotlin Python IDE\")\n\n\n" +
                "if __name__ == \"__main__\":\n" +
                "    main()\n",
        )
        File(project, "requirements.txt").writeText("# Add pure-Python packages from the Packages tab.\n")
        File(project, "README.md").writeText("# ${project.name}\n\nA Python project running on Android.\n")
        File(project, ".packages").mkdirs()
        return project
    }

    private fun uniqueName(base: String): String {
        var candidate = base
        var index = 2
        while (File(root, candidate).exists()) {
            candidate = "$base-$index"
            index += 1
        }
        return candidate
    }

    private fun safeChild(parent: File, name: String): File {
        val child = File(parent, name).canonicalFile
        require(child.path.startsWith(parent.canonicalFile.path + File.separator)) {
            "File path escapes project directory"
        }
        return child
    }
}