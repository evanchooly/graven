package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.Tests.FILE
import com.antwerkz.build.replacer.Tests.TOKEN
import com.antwerkz.build.replacer.Tests.VALUE
import com.antwerkz.build.replacer.Tests.generateFile
import java.nio.charset.Charset
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.testng.annotations.Test

class ReplacementTest {
    @Test
    private fun shouldReturnConstructorParameters() {
        val replacement = Replacement(TOKEN, VALUE)
        assertThat(replacement.token, equalTo(TOKEN))
        assertThat(replacement.value, equalTo(VALUE))
    }

    @Test
    fun shouldUseEscapedTokensAndValues() {
        val replacement = Replacement(UNESCAPED, UNESCAPED, unescape = true)
        assertThat(replacement.token, equalTo(ESCAPED))
        assertThat(replacement.value, equalTo(ESCAPED))
    }

    @Test
    fun shouldUseEscapedTokensAndValuesFromFiles() {
        generateFile(FILE, UNESCAPED)
        val replacement = Replacement(tokenFile = FILE, valueFile = FILE, unescape = true)
        assertThat(replacement.token, equalTo(ESCAPED))
        assertThat(replacement.value, equalTo(ESCAPED))
    }

    @Test
    fun shouldUseTokenFromFileUtilsIfGiven() {
        generateFile(FILE, TOKEN)
        val replacement = Replacement(tokenFile = FILE, value = VALUE)
        assertThat(replacement.token, equalTo(TOKEN))
        assertThat(replacement.value, equalTo(VALUE))
    }

    @Test
    fun shouldUseValueFromFileUtilsIfGiven() {
        generateFile(FILE, VALUE)
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
    }
}
