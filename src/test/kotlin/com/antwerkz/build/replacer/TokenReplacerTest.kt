package com.antwerkz.build.replacer

import java.util.regex.Pattern
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class TokenReplacerTest {
    lateinit var replacement: Replacement

    @BeforeMethod
    fun setUp() {
        replacement = mock(Replacement::class.java)
        `when`(replacement.token).thenReturn("t.k.n")
        `when`(replacement.value).thenReturn("value")
    }

    @Test
    fun shouldReplaceNonRegexTokenWithValue() {
        `when`(replacement.token).thenReturn("\$token$")
        val results = TokenReplacer.replace("some \$token$", replacement, false, NO_FLAGS)
        assertThat(results, equalTo("some value"))
    }

    @Test
    fun shouldReplaceRegexTokenWithValue() {
        val results = TokenReplacer.replace("some token", replacement, true, NO_FLAGS)
        assertThat(results, equalTo("some value"))
    }

    @Test
    fun shouldReplaceTokenWithEmptyValue() {
        `when`(replacement.value).thenReturn(null)
        val results = TokenReplacer.replace("some token", replacement, true, NO_FLAGS)
        assertThat(results, equalTo("some "))
    }

    @Test
    fun shouldReplaceTokenInMultipleLines() {
        `when`(replacement.value).thenReturn(null)
        val results = TokenReplacer.replace("some\ntoken", replacement, true, NO_FLAGS)
        assertThat(results, equalTo("some\n"))
    }

    @Test
    fun shouldReplaceTokenOnCompleteLine() {
        `when`(replacement.token).thenReturn("^replace=.*$")
        `when`(replacement.value).thenReturn("replace=value")
        val results =
            TokenReplacer.replace(
                "some\nreplace=token\nnext line",
                replacement,
                true,
                Pattern.MULTILINE
            )
        assertThat(results, equalTo("some\nreplace=value\nnext line"))
    }

    @Test
    fun shouldReplaceTokenWithCaseInsensitivity() {
        `when`(replacement.token).thenReturn("TEST")
        val results = TokenReplacer.replace("test", replacement, true, Pattern.CASE_INSENSITIVE)
        assertThat(results, equalTo("value"))
    }

    @Test
    fun shouldHandleEmptyContentsGracefully() {
        var results = TokenReplacer.replace("", replacement, true, NO_FLAGS)
        assertThat(results, equalTo(""))
        results = TokenReplacer.replace("", replacement, false, NO_FLAGS)
        assertThat(results, equalTo(""))
    }

    @Test
    fun shouldHandleEmptyValueForNonRegex() {
        `when`(replacement.token).thenReturn("token")
        `when`(replacement.value).thenReturn(null)
        val results = TokenReplacer.replace("some token", replacement, false, NO_FLAGS)
        assertThat(results, equalTo("some "))
    }

    @Test
    fun shouldReplaceWithGroups() {
        `when`(replacement.token).thenReturn("test (.*) number")
        `when`(replacement.value).thenReturn("group $1 replaced")
        val results = TokenReplacer.replace("test 123 number", replacement, true, NO_FLAGS)
        assertThat(results, equalTo("group 123 replaced"))
    }

    companion object {
        private const val NO_FLAGS = -1
    }
}
