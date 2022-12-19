package com.antwerkz.build.replacer.file

import java.io.File
import java.nio.charset.Charset

object FileUtils {
    fun fileNotExists(filename: String): Boolean {
        return filename.isBlank() || !File(filename).exists()
    }

    fun ensureFolderStructureExists(file: File) {
        if (file.parent == null) {
            return
        }
        if (file.isDirectory) {
            throw IllegalArgumentException("outputFile cannot be a directory: $file")
        }
        val parentPath = File(file.parent)
        if (!parentPath.exists() && !parentPath.mkdirs()) {
            throw IllegalArgumentException("Error creating directory: $parentPath")
        }
    }

    fun readFile(file: File, encoding: Charset): String {
        return file.readText(encoding)
    }

    fun writeToFile(outputFile: File, content: String, encoding: Charset) {
        ensureFolderStructureExists(outputFile)
        outputFile.writeText(content, encoding)
    }

    fun createFullPath(vararg elements: String?): String {
        return elements.filterNotNull().fold(File("")) { acc, it -> File(acc, it) }.absolutePath
        /*
                val fullPath = StringBuilder()
                for (i in 0 until dirsAndFilename.size - 1) {
                    if (dirsAndFilename[i]?.isNotBlank() == true) {
                        fullPath.append(dirsAndFilename[i])
                        fullPath.append(File.separator)
                    }
                }
                val last = dirsAndFilename[dirsAndFilename.size - 1]
                fullPath.append(last)
                return file
        */
    }

    fun isAbsolutePath(file: String): Boolean {
        return File(file).isAbsolute
    }
}
