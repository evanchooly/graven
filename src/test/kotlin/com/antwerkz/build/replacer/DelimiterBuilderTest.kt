package com.antwerkz.build.replacer

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.testng.annotations.Test

class DelimiterBuilderTest {

    companion object {
        private const val VALUE_WITHOUT_MIDDLE = "@"
        private const val VALUE_WITH_MIDDLE_START = "\${"
        private const val VALUE_WITH_MIDDLE_END = "}"
        private const val TOKEN = "token"
    }

    @Test
    fun shouldReturnUnchangedTokenWhenNoValueGiven() {
        assertThat(DelimiterBuilder("").apply(TOKEN), equalTo(TOKEN))
    }

    @Test
    fun shouldReturnTokenWithValueAtStartAndEndWhenNoMiddle() {
        val result = DelimiterBuilder(VALUE_WITHOUT_MIDDLE).apply(TOKEN)
        assertThat(result, equalTo(VALUE_WITHOUT_MIDDLE + TOKEN + VALUE_WITHOUT_MIDDLE))
    }

    @Test
    fun shouldReturnTokenWithSplitValueAtStartAndEndWhenHasMiddleAsterisk() {
        val result =
            DelimiterBuilder("$VALUE_WITH_MIDDLE_START*$VALUE_WITH_MIDDLE_END").apply(TOKEN)
        assertThat(result, equalTo(VALUE_WITH_MIDDLE_START + TOKEN + VALUE_WITH_MIDDLE_END))
    }

    @Test
    fun shouldReturnEmptyOrNullIfTokenEmptyOrNull() {
        assertThat(DelimiterBuilder().apply(""), equalTo(""))
    }
}
