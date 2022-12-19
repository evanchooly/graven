package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.file.FileUtils
import com.antwerkz.build.replacer.file.FileUtilsTest.Companion.utf8
import java.io.File
import java.nio.charset.Charset
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.Mockito.`when`
import org.testng.Assert.assertThrows
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test

class ReplacementProcessorTest {

    @Mock lateinit var replacer: Replacer
    @Mock lateinit var replacement: Replacement

    @BeforeTest
    fun setUp() {
        `when`(FileUtils.readFile(File(FILE), Charset.defaultCharset())).thenReturn(CONTENT)
        `when`(replacement.token).thenReturn(TOKEN)
        `when`(replacement.value).thenReturn(VALUE)
    }

    @Test
    fun shouldWriteReplacedRegexTextToFile() {
        `when`(replacer.replace(CONTENT, replacement, true, REGEX_FLAGS)).thenReturn(NEW_CONTENT)
        ReplacementProcessor.replace(
            listOf(replacement),
            USE_REGEX,
            File(FILE),
            File(OUTPUT_FILE),
            REGEX_FLAGS,
            utf8
        )
        verify(FileUtils).writeToFile(File(OUTPUT_FILE), NEW_CONTENT, utf8)
    }

    @Test
    fun shouldWriteReplacedNonRegexTextToFile() {
        `when`(replacer.replace(CONTENT, replacement, false, REGEX_FLAGS)).thenReturn(NEW_CONTENT)
        ReplacementProcessor.replace(
            listOf(replacement),
            NO_REGEX,
            File(FILE),
            File(OUTPUT_FILE),
            REGEX_FLAGS,
            utf8
        )
        verify(FileUtils).writeToFile(File(OUTPUT_FILE), NEW_CONTENT, utf8)
    }

    @Test
    fun shouldThrowExceptionIfNoToken() {
        assertThrows(IllegalArgumentException::class.java) {
            `when`(replacement.token).thenReturn(null)
            ReplacementProcessor.replace(
                listOf(replacement),
                USE_REGEX,
                File(FILE),
                File(OUTPUT_FILE),
                REGEX_FLAGS,
                utf8
            )
            verifyZeroInteractions(FileUtils)
        }
    }

    @Test
    fun shouldThrowExceptionIfEmptyToken() {
        assertThrows(IllegalArgumentException::class.java) {
            `when`(replacement.token).thenReturn("")
            ReplacementProcessor.replace(
                listOf(replacement),
                USE_REGEX,
                File(FILE),
                File(OUTPUT_FILE),
                REGEX_FLAGS,
                utf8
            )
            verifyZeroInteractions(FileUtils)
        }
    }

    companion object {
        private const val FILE = "file"
        private const val OUTPUT_FILE = "outputFile"
        private const val NEW_CONTENT = "new content"
        private const val REGEX_FLAGS = 0
        private const val USE_REGEX = true
        private const val NO_REGEX = false
        private const val TOKEN = "token"
        private const val CONTENT = "content"
        private const val VALUE = "value"
    }
}
