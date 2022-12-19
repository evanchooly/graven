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

    @DataProvider
    fun params(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                arrayOf<Any?>(null, -1),
                arrayOf<Any?>(mutableListOf<Any>(), -1),
                arrayOf<Any?>(mutableListOf("CANON_EQ"), Pattern.CANON_EQ),
                arrayOf<Any?>(mutableListOf("CASE_INSENSITIVE"), Pattern.CASE_INSENSITIVE),
                arrayOf<Any?>(mutableListOf("COMMENTS"), Pattern.COMMENTS),
                arrayOf<Any?>(mutableListOf("DOTALL"), Pattern.DOTALL),
                arrayOf<Any?>(mutableListOf("LITERAL"), Pattern.LITERAL),
                arrayOf<Any?>(mutableListOf("MULTILINE"), Pattern.MULTILINE),
                arrayOf<Any?>(mutableListOf("UNICODE_CASE"), Pattern.UNICODE_CASE),
                arrayOf<Any?>(mutableListOf("UNIX_LINES"), Pattern.UNIX_LINES),
                arrayOf<Any?>(
                    mutableListOf("CANON_EQ", "CASE_INSENSITIVE"),
                    Pattern.CANON_EQ or Pattern.CASE_INSENSITIVE
                )
            )
        )
    }
}
