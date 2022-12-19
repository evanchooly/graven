package com.antwerkz.build.replacer

import java.util.regex.Pattern
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class TokenReplacerTest {
    lateinit var replacement: Replacement

    @BeforeMethod
    fun setUp() {
        replacement = Replacement("t.k.n", "value")
    }

    @Test
    fun shouldReplaceNonRegexTokenWithValue() {
        replacement.token = "\$token$"
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
        replacement.value = ""
        val results = TokenReplacer.replace("some token", replacement, true, NO_FLAGS)
        assertThat(results, equalTo("some "))
    }

    @Test
    fun shouldReplaceTokenInMultipleLines() {
        replacement.value = ""
        val results = TokenReplacer.replace("some\ntoken", replacement, true, NO_FLAGS)
        assertThat(results, equalTo("some\n"))
    }

    @Test
    fun shouldReplaceTokenOnCompleteLine() {
        replacement.token = "^replace=.*$"
        replacement.value = "replace=value"
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
        replacement.token = "TEST"
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
        replacement.token = "token"
        replacement.value = ""
        val results = TokenReplacer.replace("some token", replacement, false, NO_FLAGS)
        assertThat(results, equalTo("some "))
    }

    @Test
    fun shouldReplaceWithGroups() {
        replacement.token = "test (.*) number"
        replacement.value = "group $1 replaced"
        val results = TokenReplacer.replace("test 123 number", replacement, true, NO_FLAGS)
        assertThat(results, equalTo("group 123 replaced"))
    }

    companion object {
        private const val NO_FLAGS = -1
    }
}
