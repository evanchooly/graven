package com.antwerkz.build.replacer

import java.util.regex.Pattern

object PatternFlagsFactory {
    fun buildFlags(flags: List<String>): Int {
        if (flags.isEmpty()) {
            return NO_FLAGS
        }
        var value = 0
        for (flag in flags) {
            value = value or getStaticFieldValueOf(flag)
        }
        return value
    }

    private fun getStaticFieldValueOf(fieldName: String): Int {
        for (f in Pattern::class.java.fields) {
            if (f.name.equals(fieldName, ignoreCase = true)) {
                return try {
                    f[null] as Int
                } catch (e: Exception) {
                    throw IllegalStateException(
                        "Could not access Pattern field: " +
                            f.name +
                            " - is this an unsupported JVM?"
                    )
                }
            }
        }
        throw IllegalArgumentException("Unknown regex flag: $fieldName")
    }

    const val NO_FLAGS = -1
}
