package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.file.FileUtils
import com.antwerkz.build.replacer.file.FileUtilsTest.Companion.utf8
import java.io.File
import java.nio.charset.Charset
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.mockito.Mock
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.Mockito.`when`
import org.testng.annotations.Test

class ReplacementTest {
    @Mock lateinit var delimiter: DelimiterBuilder
    @Test
    private fun shouldReturnConstructorParameters() {
        val replacement = Replacement(TOKEN, VALUE)
        assertThat(replacement.token, equalTo(TOKEN))
        assertThat(replacement.value, equalTo(VALUE))
        verifyZeroInteractions(FileUtils)
    }

    @Test
    fun shouldApplyToTokenDelimiterIfExists() {
        `when`(delimiter.apply(TOKEN)).thenReturn("new token")
        val replacement = Replacement(TOKEN, VALUE).withDelimiter(delimiter)
        assertThat(replacement.token, equalTo("new token"))
        assertThat(replacement.value, equalTo(VALUE))
        verifyZeroInteractions(FileUtils)
    }

    @Test
    fun shouldUseEscapedTokensAndValues() {
        val replacement = Replacement(UNESCAPED, UNESCAPED, unescape = true)
        assertThat(replacement.token, equalTo(ESCAPED))
        assertThat(replacement.value, equalTo(ESCAPED))
        verifyZeroInteractions(FileUtils)
    }

    @Test
    fun shouldUseEscapedTokensAndValuesFromFiles() {
        `when`(FileUtils.readFile(FILE, utf8)).thenReturn(UNESCAPED)
        val replacement = Replacement(tokenFile = FILE, valueFile = FILE, unescape = true)
        assertThat(replacement.token, equalTo(ESCAPED))
        assertThat(replacement.value, equalTo(ESCAPED))
    }

    @Test
    fun shouldUseTokenFromFileUtilsIfGiven() {
        `when`(FileUtils.readFile(FILE, utf8)).thenReturn(TOKEN)
        val replacement = Replacement(tokenFile = FILE, value = VALUE)
        assertThat(replacement.token, equalTo(TOKEN))
        assertThat(replacement.value, equalTo(VALUE))
    }

    @Test
    fun shouldUseValueFromFileUtilsIfGiven() {
        `when`(FileUtils.readFile(FILE, utf8)).thenReturn(VALUE)
        val replacement = Replacement(TOKEN, valueFile = FILE)
        assertThat(replacement.token, equalTo(TOKEN))
        assertThat(replacement.value, equalTo(VALUE))
    }

    @Test
    fun shouldReturnCopyOfReplacementInFrom() {
        val replacement = Replacement(TOKEN, VALUE, unescape = true)
        val copy = Replacement.from(replacement)
        assertThat(copy.token, equalTo(TOKEN))
        assertThat(copy.value, equalTo(VALUE))
        assertThat(copy.isUnescape, equalTo(true))
        assertThat(copy.isUnescape, equalTo(true))
        assertThat(copy.encoding, equalTo(Charset.forName("UTF-8")))
    }

    companion object {
        private const val UNESCAPED = "test\\n123\\t456"
        private const val ESCAPED = "test\n123\t456"
        private val FILE = File("some file")
        private const val TOKEN = "token"
        private const val VALUE = "value"
    }
}
