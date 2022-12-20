package com.antwerkz.build.replacer.file

import java.io.File
import java.nio.charset.Charset

object FileUtils {
    fun fileNotExists(filename: String): Boolean {
        return filename.isBlank() || !File(filename).exists()
    }

    fun ensureFolderStructureExists(outputFile: File) {
        if (outputFile.parent == null) {
            return
        }
        if (!outputFile.isDirectory) {
            val parentPath = File(outputFile.parent)
            check(!(!parentPath.exists() && !parentPath.mkdirs())) {
                "Error creating directory: $parentPath"
            }
        } else {
            throw IllegalArgumentException("outputFile cannot be a directory: $outputFile")
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
        return elements.filterNotNull().joinToString(File.separator)
    }

    fun isAbsolutePath(file: String): Boolean {
        return File(file).isAbsolute
    }
}
