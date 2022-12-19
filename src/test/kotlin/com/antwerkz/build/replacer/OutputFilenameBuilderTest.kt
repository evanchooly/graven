package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.file.FileUtils
import java.io.File
import java.util.Locale
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class OutputFilenameBuilderTest {
    lateinit var mojo: ReplacerMojo
    @BeforeMethod
    fun setUp() {
        mojo = mock(ReplacerMojo::class.java)
        `when`(mojo.basedir).thenReturn(BASE_DIR)
    }

    @Test
    fun shouldReturnFullPathWithAllOutputFileParams() {
        `when`(mojo.outputDir).thenReturn(OUTPUT_DIR)
        `when`(mojo.outputBasedir).thenReturn(OUTPUT_BASE_DIR)
        `when`(mojo.outputFile).thenReturn(OUTPUT_FILE_WITH_PARENT)
        `when`(mojo.isPreserveDir).thenReturn(true)
        var output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
        assertThat(
            output,
            equalTo(FileUtils.createFullPath(OUTPUT_BASE_DIR, OUTPUT_DIR, OUTPUT_FILE_WITH_PARENT))
        )
        `when`(mojo.isPreserveDir).thenReturn(false)
        output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
        assertThat(
            output,
            equalTo(FileUtils.createFullPath(OUTPUT_BASE_DIR, OUTPUT_DIR, OUTPUT_FILE))
        )
    }

    @Test
    fun shouldPrefixBasedirWhenNotPreservingPath() {
        `when`(mojo.isPreserveDir).thenReturn(false)
        `when`(mojo.outputDir).thenReturn(OUTPUT_DIR)
        val output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(FileUtils.createFullPath(BASE_DIR, OUTPUT_DIR, "input")))
    }

    @Test
    fun shouldPreservePathWhenPreserveIsEnabled() {
        `when`(mojo.isPreserveDir).thenReturn(true)
        `when`(mojo.outputDir).thenReturn(OUTPUT_DIR)
        val output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(FileUtils.createFullPath(BASE_DIR, OUTPUT_DIR, INPUT_FILE)))
    }

    @Test
    fun shouldPrefixOutputDirWhenUsingOutputDirAndOutputFile() {
        `when`(mojo.outputDir).thenReturn(OUTPUT_DIR)
        `when`(mojo.outputFile).thenReturn(OUTPUT_FILE)
        val output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(FileUtils.createFullPath(BASE_DIR, OUTPUT_DIR, OUTPUT_FILE)))
    }

    @Test
    fun shouldReturnReplacedOutputFilenameFromPatterns() {
        `when`(mojo.inputFilePattern).thenReturn("(.+)")
        `when`(mojo.outputFilePattern).thenReturn("$1")
        var output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(FileUtils.createFullPath(BASE_DIR, INPUT_FILE)))
        `when`(mojo.inputFilePattern).thenReturn("(.+)")
        `when`(mojo.outputFilePattern).thenReturn("$1-new")
        output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(FileUtils.createFullPath(BASE_DIR, INPUT_FILE + "-new")))
    }

    @Test
    fun shouldNotReturnReplacedOutputFilenameWhenMissingEitherInputOrOutputPattern() {
        `when`(mojo.inputFilePattern).thenReturn(null)
        `when`(mojo.outputFilePattern).thenReturn("$1-new")
        var output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
        assertThat(output, not(endsWith("-new")))
        `when`(mojo.inputFilePattern).thenReturn("(.+)")
        `when`(mojo.outputFilePattern).thenReturn(null)
        output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(FileUtils.createFullPath(BASE_DIR, INPUT_FILE)))
    }

    @Test
    fun shouldPrefixBasedirWhenNotUsingOutputBasedir() {
        `when`(mojo.outputDir).thenReturn(OUTPUT_DIR)
        val output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(FileUtils.createFullPath(BASE_DIR, OUTPUT_DIR, "input")))
    }

    @Test
    fun shouldPrefixWithOutputBasedirWhenUsingOutputBasedir() {
        `when`(mojo.outputBasedir).thenReturn(OUTPUT_BASE_DIR)
        `when`(mojo.outputDir).thenReturn(OUTPUT_DIR)
        val output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(FileUtils.createFullPath(OUTPUT_BASE_DIR, OUTPUT_DIR, "input")))
    }

    @Test
    fun shouldReturnInputFileWithBaseDirWhenNoOutputDirOrNoOutputFile() {
        val output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(FileUtils.createFullPath(BASE_DIR, INPUT_FILE)))
    }

    @Test
    fun shouldWriteToOutputFileWhenNotUsingOutputDirAndIsSet() {
        `when`(mojo.outputFile).thenReturn(OUTPUT_FILE)
        val output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
        assertThat(output, equalTo(FileUtils.createFullPath(BASE_DIR, OUTPUT_FILE)))
    }

    @Test
    fun shouldReturnIgnoreBaseDirForOutputFileWhenStartsWithAbsolutePath() {
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        if (os.indexOf("windows") < 0) {
            `when`(mojo.outputFile).thenReturn(File.separator + "output")
            val output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
            assertThat(output, equalTo(File.separator + "output"))
        } else {
            `when`(mojo.outputFile).thenReturn("C:" + File.separator + "output")
            val output = OutputFilenameBuilder.buildFrom(INPUT_FILE, mojo)
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
