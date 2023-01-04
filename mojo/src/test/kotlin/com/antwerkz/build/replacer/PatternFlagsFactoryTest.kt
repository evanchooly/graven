package com.antwerkz.build.replacer

import java.util.regex.Pattern
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.testng.Assert.assertThrows
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class PatternFlagsFactoryTest {

    @Test(dataProvider = "params")
    fun shouldReturnBitValueForFlags(inputFlags: List<String>, expectedFlags: Int) {
        assertThat(PatternFlagsFactory.buildFlags(inputFlags), `is`(expectedFlags))
    }

    @Test(dataProvider = "params")
    fun shouldThrowIllegalArgumentExceptionWhenFlagIsInvalid(
        inputFlags: List<String>,
        expectedFlags: Int
    ) {
        assertThrows(IllegalArgumentException::class.java) {
            PatternFlagsFactory.buildFlags(mutableListOf("invalid"))
        }
    }

    @DataProvider(name = "params")
    fun params(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(mutableListOf<Any>(), -1),
            arrayOf(mutableListOf<Any>(), -1),
            arrayOf(mutableListOf("CANON_EQ"), Pattern.CANON_EQ),
            arrayOf(mutableListOf("CASE_INSENSITIVE"), Pattern.CASE_INSENSITIVE),
            arrayOf(mutableListOf("COMMENTS"), Pattern.COMMENTS),
            arrayOf(mutableListOf("DOTALL"), Pattern.DOTALL),
            arrayOf(mutableListOf("LITERAL"), Pattern.LITERAL),
            arrayOf(mutableListOf("MULTILINE"), Pattern.MULTILINE),
            arrayOf(mutableListOf("UNICODE_CASE"), Pattern.UNICODE_CASE),
            arrayOf(mutableListOf("UNIX_LINES"), Pattern.UNIX_LINES),
            arrayOf(
                mutableListOf("CANON_EQ", "CASE_INSENSITIVE"),
                Pattern.CANON_EQ or Pattern.CASE_INSENSITIVE
            )
        )
    }
}
