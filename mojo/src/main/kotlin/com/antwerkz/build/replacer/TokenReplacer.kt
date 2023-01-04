package com.antwerkz.build.replacer

import java.util.regex.Pattern

object TokenReplacer : Replacer {
    override fun replace(
        content: String,
        replacement: Replacement,
        regex: Boolean,
        regexFlags: Int
    ): String {
        return if (regex) {
            replaceRegex(content, replacement.token, replacement.value, regexFlags)
        } else replaceNonRegex(content, replacement.token, replacement.value)
    }

    private fun replaceRegex(content: String, token: String, value: String, flags: Int): String {
        val compiledPattern: Pattern =
            if (flags == PatternFlagsFactory.NO_FLAGS) {
                Pattern.compile(token)
            } else {
                Pattern.compile(token, flags)
            }
        return compiledPattern.matcher(content).replaceAll(value)
    }

    private fun replaceNonRegex(content: String, token: String, value: String): String {
        return if (content.isEmpty()) {
            content
        } else content.replace(token, value)
    }
}
