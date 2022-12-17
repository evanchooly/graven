package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.file.FileUtils
import java.io.File

object OutputFilenameBuilder {
    fun buildFrom(inputFilename: String, mojo: ReplacerMojo): String {
        var based = buildOutputFile(inputFilename, mojo)
        val inputFilePattern = mojo.inputFilePattern
        val outputFilePattern = mojo.outputFilePattern
        if (inputFilePattern != null && outputFilePattern != null) {
            based = based.replace(inputFilePattern.toRegex(), outputFilePattern)
        }
        return based
    }

    private fun buildOutputFile(inputFilename: String, mojo: ReplacerMojo): String {
        val basedir = if (FileUtils.isAbsolutePath(inputFilename)) "" else mojo.basedir
        val outputFile = mojo.outputFile
        if (mojo.outputDir != null && outputFile != null) {
            val cleanResult = if (mojo.isPreserveDir) outputFile else stripPath(outputFile)
            return if (mojo.outputBasedir != null) {
                FileUtils.createFullPath(mojo.outputBasedir, mojo.outputDir, cleanResult)
            } else FileUtils.createFullPath(basedir, mojo.outputDir, cleanResult)
        }
        if (mojo.outputDir != null) {
            val cleanResult = if (mojo.isPreserveDir) inputFilename else stripPath(inputFilename)
            return if (mojo.outputBasedir != null) {
                FileUtils.createFullPath(mojo.outputBasedir, mojo.outputDir, cleanResult)
            } else FileUtils.createFullPath(basedir, mojo.outputDir, cleanResult)
        }
        if (outputFile != null) {
            val outFile = File(outputFile)
            return if (outFile.isAbsolute) {
                FileUtils.createFullPath(outputFile)
            } else FileUtils.createFullPath(basedir, outputFile)
        }
        return FileUtils.createFullPath(basedir, inputFilename)
    }

    private fun stripPath(inputFilename: String): String = File(inputFilename).name
}
