package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.Tests.FILE
import com.antwerkz.build.replacer.Tests.generateFile
import com.antwerkz.build.replacer.Tests.utf8
import java.io.File
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.testng.Assert.assertThrows
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class TokenValueMapFactoryTest {
    @Test
    fun shouldReturnReplacementsFromFile() {
        generateFile(FILE, "token=value")
        val replacements =
            TokenValueMapFactory.replacementsForFile(FILE, COMMENTS_DISABLED, false, utf8)
        assertThat(replacements, notNullValue())
        assertThat(replacements.size, `is`(1))
        assertThat(replacements[0].token, equalTo("token"))
        assertThat(replacements[0].value, equalTo("value"))
    }

    @Test
    fun shouldReturnReplacementsFromFileAndIgnoreBlankLinesAndComments() {
        val file = File(FILENAME)
        generateFile(file, "\n  \ntoken1=value1\ntoken2 = value2\n#some comment\n")
        val replacements =
            TokenValueMapFactory.replacementsForFile(file, COMMENTS_ENABLED, false, utf8)
        assertThat(replacements, notNullValue())
        assertThat(replacements.size, `is`(2))
        assertThat(replacements[0].token, equalTo("token1"))
        assertThat(replacements[0].value, equalTo("value1"))
        assertThat(replacements[1].token, equalTo("token2"))
        assertThat(replacements[1].value, equalTo("value2"))
    }

    @Test
    fun shouldReturnReplacementsFromFileAndIgnoreBlankLinesUsingCommentLinesIfCommentsDisabled() {
        val file = File(FILENAME)
        generateFile(file, "\n  \ntoken1=value1\ntoken2=value2\n#some=#comment\n")
        val replacements =
            TokenValueMapFactory.replacementsForFile(file, COMMENTS_DISABLED, false, utf8)
        assertThat(replacements, notNullValue())
        assertThat(replacements.size, `is`(3))
        assertThat(replacements[0].token, equalTo("token1"))
        assertThat(replacements[0].value, equalTo("value1"))
        assertThat(replacements[1].token, equalTo("token2"))
        assertThat(replacements[1].value, equalTo("value2"))
        assertThat(replacements[2].token, equalTo("#some"))
        assertThat(replacements[2].value, equalTo("#comment"))
    }

    @Test
    fun shouldIgnoreTokensWithNoSeparatedValue() {
        val file = File(FILENAME)
        generateFile(file, "#comment\ntoken2")
        val replacements =
            TokenValueMapFactory.replacementsForFile(file, COMMENTS_DISABLED, false, utf8)
        assertThat(replacements, notNullValue())
        assertTrue(replacements.isEmpty())
    }

    @Test
    fun shouldReturnRegexReplacementsFromFile() {
        val file = File(FILENAME)
        generateFile(file, "\\=tok\\=en1=val\\=ue1\nto\$ke..n2=value2")
        val replacements =
            TokenValueMapFactory.replacementsForFile(file, COMMENTS_ENABLED, false, utf8)
        assertThat(replacements, notNullValue())
        assertThat(replacements.size, `is`(2))
        assertThat(replacements[0].token, equalTo("\\=tok\\=en1"))
        assertThat(replacements[0].value, equalTo("val\\=ue1"))
        assertThat(replacements[1].token, equalTo("to\$ke..n2"))
        assertThat(replacements[1].value, equalTo("value2"))
    }

    @Test
    fun shouldReturnRegexReplacementsFromFileUnescaping() {
        val file = File(FILENAME)
        generateFile(file, "\\\\=tok\\\\=en1=val\\\\=ue1\nto\$ke..n2=value2")
        val replacements =
            TokenValueMapFactory.replacementsForFile(file, COMMENTS_ENABLED, true, utf8)
        assertThat(replacements, notNullValue())
        assertThat(replacements.size, `is`(2))
        assertThat(replacements[0].token, equalTo("\\=tok\\=en1"))
        assertThat(replacements[0].value, equalTo("val\\=ue1"))
        assertThat(replacements[1].token, equalTo("to\$ke..n2"))
        assertThat(replacements[1].value, equalTo("value2"))
    }

    @Test
    fun shouldThrowExceptionIfNoTokenForValue() {
        assertThrows(IllegalArgumentException::class.java) {
            generateFile(FILE, "=value")
            TokenValueMapFactory.replacementsForFile(FILE, COMMENTS_DISABLED, false, utf8)
        }
    }

    @Test
    fun shouldSupportEmptyFileAndReturnNoReplacements() {
        generateFile(FILE, "")
        val replacements =
            TokenValueMapFactory.replacementsForFile(FILE, COMMENTS_DISABLED, false, utf8)
        assertThat(replacements, notNullValue())
        assertTrue(replacements.isEmpty())
    }

    @Test
    fun shouldReturnListOfReplacementsFromVariable() {
        val replacements =
            TokenValueMapFactory.replacementsForVariable(
                "#comment,token1=value1,token2=value2",
                commentsEnabled = true,
                unescape = false,
                encoding = utf8
            )
        assertThat(replacements, notNullValue())
        assertThat(replacements.size, `is`(2))
        assertThat(replacements, hasItem(replacementWith("token1", "value1")))
        assertThat(replacements, hasItem(replacementWith("token2", "value2")))
    }

    @Test
    fun shouldReturnListOfReplacementsFromSingleVariable() {
        val replacements =
            TokenValueMapFactory.replacementsForVariable(
                "token1=value1",
                commentsEnabled = true,
                unescape = false,
                encoding = utf8
            )
        assertThat(replacements, notNullValue())
        assertThat(replacements.size, `is`(1))
        assertThat(replacements, hasItem(replacementWith("token1", "value1")))
    }

    private fun replacementWith(token: String, value: String): Matcher<Replacement> {
        return object : BaseMatcher<Replacement>() {
            override fun matches(o: Any): Boolean {
                val replacement = o as Replacement
                return token == replacement.token && value == replacement.value
            }

            override fun describeTo(desc: Description) {
                desc.appendText("token=$token, value=$value")
            }
        }
    }

    companion object {
        private const val FILENAME = "target/some file"
        private const val COMMENTS_ENABLED = true
        private const val COMMENTS_DISABLED = false
    }
}
