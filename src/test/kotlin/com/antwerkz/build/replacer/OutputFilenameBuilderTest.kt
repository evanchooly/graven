package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.OutputFilenameBuilder.buildFrom
import com.antwerkz.build.replacer.file.FileUtils.createFullPath
import java.io.File
import java.util.Locale
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.testng.annotations.Test

class OutputFilenameBuilderTest {
    var mojo = ReplacerMojo()

    @Test
    fun shouldReturnFullPathWithAllOutputFileParams() {
        mojo.outputDir = OUTPUT_DIR
        mojo.outputBasedir = OUTPUT_BASE_DIR
        mojo.outputFile = OUTPUT_FILE_WITH_PARENT
        mojo.isPreserveDir = true
        assertThat(
            buildFrom(INPUT_FILE, mojo),
            equalTo(createFullPath(OUTPUT_BASE_DIR, OUTPUT_DIR, OUTPUT_FILE_WITH_PARENT))
        )
        mojo.isPreserveDir = false
        assertThat(
            buildFrom(INPUT_FILE, mojo),
            equalTo(createFullPath(OUTPUT_BASE_DIR, OUTPUT_DIR, OUTPUT_FILE))
        )
    }

    @Test
    fun shouldPrefixBasedirWhenNotPreservingPath() {
        mojo.isPreserveDir = false
        mojo.outputDir = OUTPUT_DIR
        val output = buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(createFullPath(BASE_DIR, OUTPUT_DIR, "input")))
    }

    @Test
    fun shouldPreservePathWhenPreserveIsEnabled() {
        mojo.isPreserveDir = true
        mojo.outputDir = OUTPUT_DIR
        val output = buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(createFullPath(BASE_DIR, OUTPUT_DIR, INPUT_FILE)))
    }

    @Test
    fun shouldPrefixOutputDirWhenUsingOutputDirAndOutputFile() {
        mojo.outputDir = OUTPUT_DIR
        mojo.outputFile = OUTPUT_FILE
        val output = buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(createFullPath(BASE_DIR, OUTPUT_DIR, OUTPUT_FILE)))
    }

    @Test
    fun shouldReturnReplacedOutputFilenameFromPatterns() {
        mojo.inputFilePattern = "(.+)"
        mojo.outputFilePattern = "$1"
        var output = buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(createFullPath(BASE_DIR, INPUT_FILE)))
        mojo.inputFilePattern = "(.+)"
        mojo.outputFilePattern = "$1-new"
        output = buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(createFullPath(BASE_DIR, INPUT_FILE + "-new")))
    }

    @Test
    fun shouldNotReturnReplacedOutputFilenameWhenMissingEitherInputOrOutputPattern() {
        mojo.inputFilePattern = null
        mojo.outputFilePattern = "$1-new"
        var output = buildFrom(INPUT_FILE, mojo)
        assertThat(output, not(endsWith("-new")))
        mojo.inputFilePattern = "(.+)"
        mojo.outputFilePattern = null
        output = buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(createFullPath(BASE_DIR, INPUT_FILE)))
    }

    @Test
    fun shouldPrefixBasedirWhenNotUsingOutputBasedir() {
        mojo.outputDir = OUTPUT_DIR
        val output = buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(createFullPath(BASE_DIR, OUTPUT_DIR, "input")))
    }

    @Test
    fun shouldPrefixWithOutputBasedirWhenUsingOutputBasedir() {
        mojo.outputBasedir = OUTPUT_BASE_DIR
        mojo.outputDir = OUTPUT_DIR
        val output = buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(createFullPath(OUTPUT_BASE_DIR, OUTPUT_DIR, "input")))
    }

    @Test
    fun shouldReturnInputFileWithBaseDirWhenNoOutputDirOrNoOutputFile() {
        val output = buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(createFullPath(BASE_DIR, INPUT_FILE)))
    }

    @Test
    fun shouldWriteToOutputFileWhenNotUsingOutputDirAndIsSet() {
        mojo.outputFile = OUTPUT_FILE
        val output = buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(createFullPath(BASE_DIR, OUTPUT_FILE)))
    }

    @Test
    fun shouldReturnIgnoreBaseDirForOutputFileWhenStartsWithAbsolutePath() {
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        if (os.indexOf("windows") < 0) {
            mojo.outputFile = File.separator + "output"
            val output = buildFrom(INPUT_FILE, mojo)
            assertThat(output, equalTo(File.separator + "output"))
        } else {
            mojo.outputFile = "C:" + File.separator + "output"
            val output = buildFrom(INPUT_FILE, mojo)
            assertThat(output, equalTo("C:" + File.separator + "output"))
        }
    }

    companion object {
        private val INPUT_FILE = "parent" + File.separator + "input"
        private const val BASE_DIR = "."
        private val OUTPUT_DIR = "target" + File.separator + "out"
        private const val OUTPUT_FILE = "outputFile"
        private val OUTPUT_FILE_WITH_PARENT = "parent" + File.separator + OUTPUT_FILE
        private const val OUTPUT_BASE_DIR = "outputBaseDir"
    }
}
