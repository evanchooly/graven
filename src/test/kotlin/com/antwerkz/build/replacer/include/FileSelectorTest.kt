package com.antwerkz.build.replacer.include

import java.io.File
import java.util.Arrays
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.testng.Assert.assertTrue
import org.testng.annotations.Ignore
import org.testng.annotations.Test

class FileSelectorTest {
    companion object {
        private const val BASE_DIR = "src/test/resources/files"
        private const val TEST_FILE = "maven-replacer-plugin-test-file"
        private const val BACK_DIR_SYMBOL = ".."
    }

    @Test
    fun shouldReturnMultipleFilesToInclude() {
        val files =
            FileSelector.listIncludes(
                BASE_DIR,
                mutableListOf("include1", "file*"),
                mutableListOf("file3")
            )
        assertThat(files.size, `is`(3))
        assertThat(files, equalTo(mutableListOf("file1", "file2", "include1")))
    }

    @Test
    fun shouldSupportNoExcludes() {
        val files =
            FileSelector.listIncludes(BASE_DIR, mutableListOf("include1", "file*"), emptyList())
        assertThat(files, equalTo(mutableListOf("file1", "file2", "file3", "include1")))
    }

    @Test
    fun shouldReturnEmptyListWhenEmptyIncludes() {
        assertTrue(
            FileSelector.listIncludes(BASE_DIR, emptyList(), mutableListOf("file3")).isEmpty()
        )
        assertTrue(
            FileSelector.listIncludes(BASE_DIR, emptyList(), mutableListOf("file3")).isEmpty()
        )
    }

    @Test
    fun shouldSelectFilesInBackDirectories() {
        val file = File(BACK_DIR_SYMBOL + File.separator + TEST_FILE)
        file.deleteOnExit()
        file.writeText(BASE_DIR)
        val files =
            FileSelector.listIncludes(BACK_DIR_SYMBOL, Arrays.asList(TEST_FILE), emptyList())
        assertThat(files, equalTo(Arrays.asList(TEST_FILE)))
    }

    @Test
    @Ignore("doesn't work on windows")
    fun shouldSelectFilesFromAbsolutePaths() {
        val file = File("src/test/resources/files/file1")
        val include: String = file.parentFile.absolutePath + "/**/*"
        val selected = FileSelector.listIncludes(includes = listOf(include))
        assertThat(selected, hasItem(file.absolutePath))
    }
}
