package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.Tests.CONTENT
import com.antwerkz.build.replacer.Tests.FILE
import com.antwerkz.build.replacer.Tests.NEW_CONTENT
import com.antwerkz.build.replacer.Tests.OUTPUT_FILE
import com.antwerkz.build.replacer.Tests.REGEX_FLAGS
import com.antwerkz.build.replacer.Tests.assertFile
import com.antwerkz.build.replacer.Tests.generateFile
import com.antwerkz.build.replacer.Tests.utf8
import org.testng.Assert.assertThrows
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class ReplacementProcessorTest {
    lateinit var replacement: Replacement

    @BeforeMethod
    fun setUp() {
        replacement = Replacement("token", "value")
        generateFile(FILE, CONTENT)
    }

    @Test
    fun shouldWriteReplacedRegexTextToFile() {
        //                `when`(TokenReplacer.replace(CONTENT, replacement, true,
        // REGEX_FLAGS)).thenReturn(NEW_CONTENT)
        ReplacementProcessor.replace(
            listOf(Replacement("^", "new ")),
            true,
            FILE,
            OUTPUT_FILE,
            REGEX_FLAGS,
            utf8
        )
        assertFile(OUTPUT_FILE, NEW_CONTENT)
    }

    @Test
    fun shouldWriteReplacedNonRegexTextToFile() {
        ReplacementProcessor.replace(
            listOf(Replacement("content", "new content")),
            false,
            FILE,
            OUTPUT_FILE,
            REGEX_FLAGS,
            utf8
        )
        assertFile(OUTPUT_FILE, NEW_CONTENT)
    }

    @Test
    fun shouldThrowExceptionIfNoToken() {
        assertThrows(IllegalArgumentException::class.java) {
            replacement.token = ""
            ReplacementProcessor.replace(
                listOf(replacement),
                true,
                FILE,
                OUTPUT_FILE,
                REGEX_FLAGS,
                utf8
            )
        }
    }

    @Test
    fun shouldThrowExceptionIfEmptyToken() {
        assertThrows(IllegalArgumentException::class.java) {
            replacement.token = ""
            ReplacementProcessor.replace(
                listOf(replacement),
                true,
                FILE,
                OUTPUT_FILE,
                REGEX_FLAGS,
                utf8
            )
        }
    }
}
