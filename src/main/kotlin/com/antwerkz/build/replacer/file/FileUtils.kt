package com.antwerkz.build.replacer.file

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.charset.Charset

object FileUtils {
    fun fileNotExists(filename: String): Boolean {
        return filename.isBlank() || !File(filename).exists()
    }

    private fun ensureFolderStructureExists(file: String) {
        val outputFile = File(file)
        if (outputFile.parent == null) {
            return
        }
        if (!outputFile.isDirectory) {
            val parentPath = File(outputFile.parent)
            check(!(!parentPath.exists() && !parentPath.mkdirs())) {
                "Error creating directory: $parentPath"
            }
        } else {
            throw IllegalArgumentException("outputFile cannot be a directory: $file")
        }
    }

    fun readFile(file: String, encoding: Charset): String {
        return FileReader(file, encoding).readText()
    }

    fun writeToFile(outputFile: String, content: String, encoding: Charset) {
        ensureFolderStructureExists(outputFile)
        FileWriter(outputFile, encoding).write(content)
    }

    fun createFullPath(vararg dirsAndFilename: String?): String {
        val fullPath = StringBuilder()
        for (i in 0 until dirsAndFilename.size - 1) {
            if (dirsAndFilename[i]?.isNotBlank() == true) {
                fullPath.append(dirsAndFilename[i])
                fullPath.append(File.separator)
            }
        }
        val last = dirsAndFilename[dirsAndFilename.size - 1]
        if (last != null) {
            fullPath.append(last)
        }
        return fullPath.toString()
    }

    fun isAbsolutePath(file: String): Boolean {
        return File(file).isAbsolute
    }
}
