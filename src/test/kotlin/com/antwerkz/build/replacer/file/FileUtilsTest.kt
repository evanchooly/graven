package com.antwerkz.build.replacer.file

import com.antwerkz.build.replacer.Tests.ascii
import com.antwerkz.build.replacer.Tests.utf8
import java.io.File
import java.io.FileWriter
import java.util.UUID
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat
import org.testng.Assert.assertFalse
import org.testng.Assert.assertThrows
import org.testng.Assert.assertTrue
import org.testng.Assert.fail
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class FileUtilsTest {
    companion object {
        const val NON_ASCII_CONTENT = "한국어/조선말"
        const val CONTENT = "content"
    }

    private lateinit var folder: File

    @BeforeMethod
    fun tempDir() {
        folder = File("target/tempfolders", UUID.randomUUID().toString())
        folder.mkdirs()
        folder.deleteOnExit()
    }

    @Test
    fun shouldEnsureFileFolderExists() {
        val tempFile = getTempFile()
        FileUtils.ensureFolderStructureExists(tempFile)
        tempFile.createNewFile()
        assertTrue(tempFile.exists())
    }

    @Test
    fun shouldNotDoAnythingIfRootDirectory() {
        FileUtils.ensureFolderStructureExists(File("/"))
    }

    @Test
    fun shouldThrowIllegalArgumentExceptionIfFileIsDirectory() {
        assertThrows(IllegalArgumentException::class.java) {
            FileUtils.ensureFolderStructureExists(File(System.getProperty("java.io.tmpdir")))
        }
    }

    @Test
    fun shouldWriteToFileEnsuringFolderStructureExists() {
        val tempFile = getTempFile()
        FileUtils.writeToFile(tempFile, CONTENT, utf8)
        assertThat(tempFile.readText(), equalTo(CONTENT))
    }

    @Test
    fun shouldWriteFileWithoutSpecifiedEncoding() {
        val tempFile = getTempFile()
        FileUtils.writeToFile(tempFile, NON_ASCII_CONTENT, utf8)
        assertThat(FileUtils.readFile(tempFile, utf8), equalTo(NON_ASCII_CONTENT))
    }

    @Test
    fun shouldWriteFileWithSpecifiedEncoding() {
        val tempFile = getTempFile()
        FileUtils.writeToFile(tempFile, NON_ASCII_CONTENT, utf8)
        assertThat(FileUtils.readFile(tempFile, utf8), equalTo(NON_ASCII_CONTENT))
        assertThat(FileUtils.readFile(tempFile, ascii), not(equalTo(NON_ASCII_CONTENT)))
    }

    private fun getTempFile() =
        File(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID() + "/tempfile")

    @Test
    fun shouldReturnFileText() {
        val file: File = folder.newFile("tempfile")
        val writer = FileWriter(file)
        writer.write("test\n123\\t456")
        writer.close()
        val data = FileUtils.readFile(file, utf8)
        assertThat(data, equalTo("test\n123\\t456"))
    }

    @Test
    fun shouldReturnFilenameWhenJustFilenameParam() {
        val result = FileUtils.createFullPath("tempFile")
        assertThat(result, equalTo("tempFile"))
    }

    @Test
    fun shouldBuildFullPathFromDirsAndFilename() {
        val result = FileUtils.createFullPath("1", "2", "3", "tempFile")
        assertThat(
            result,
            equalTo(mutableListOf("1", "2", "3", "tempFile").joinToString(File.separator))
        )
    }

    @Test
    fun shouldThrowExceptionWhenCannotCreateDir() {
        try {
            FileUtils.ensureFolderStructureExists(File(".../f*\"%e\$d/a%*bc$:\\te\"st"))
            fail("Should have thrown Error")
        } catch (e: IllegalStateException) {
            assertThat(e.message, startsWith("Error creating directory"))
        }
    }

    @Test
    fun shouldReturnTrueWhenAbsolutePathFilename() {
        assertFalse(FileUtils.isAbsolutePath("target/somedir/somepath"))
        assertTrue(FileUtils.isAbsolutePath(File("target/somefile").absolutePath))
    }
}

private fun File.newFile(newFile: String): File {
    return File(this, newFile).also { it.parentFile.mkdirs() }
}
