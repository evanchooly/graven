package com.antwerkz.build

import com.antwerkz.build.UpdateValues.matcher
import com.antwerkz.build.UpdateValues.propertyMatcher
import com.antwerkz.expression.RegularExpression.Companion.capture
import com.antwerkz.expression.RegularExpression.Companion.oneOrMore
import com.antwerkz.expression.RegularExpression.Companion.oneOrMoreLazy
import com.antwerkz.expression.toRegex
import org.apache.maven.model.Dependency

enum class Updaters {
    DOUBLE_QUOTED_VERSIONS {
        override fun create(input: String): DepReplacer {
            return object : DepReplacer(matcher("\""), input) {
                override fun replace(dependency: Dependency): String {
                    return input.replace(regex, "$1(\"$2:$3:${dependency.version}\")")
                }
            }
        }
    },
    SINGLE_QUOTED_VERSIONS {
        override fun create(input: String): DepReplacer {
            return object : DepReplacer(matcher("'"), input) {
                override fun replace(dependency: Dependency): String {
                    return input.replace(regex, "$1('$2:$3:${dependency.version}')")
                }
            }
        }
    },
    PROPERTY_DEFINITIONS {
        override fun create(input: String): DepReplacer {
            return object : DepReplacer(propertyMatcher("\""), input) {
                override fun replace(dependency: Dependency): String {
                    return input.replace(regex, "$1 version \"$2\"")
                }
            }
        }
    };
    //    GROOVY_PLUGINS,
    //    KOTLIN_PLUGINS

    abstract fun create(input: String): DepReplacer
    abstract class DepReplacer(val regex: Regex, input: String) {
        val result = regex.matchEntire(input)
        val groupId: String? by lazy {
            try {
                result!!.groups["groupId"]?.value
            } catch (_: IllegalArgumentException) {
                null
            }
        }
        val artifactId: String? by lazy {
            try {
                result!!.groups["artifactId"]?.value
            } catch (_: IllegalArgumentException) {
                null
            }
        }

        abstract fun replace(dependency: Dependency): String

        fun matches() = result != null
    }
}

object UpdateValues {
    private val gavCharacters = oneOrMore {
        anyOf { range('a', 'z').range('A', 'Z').char('-').char('.') }
    }
    private val versionCharacters =
        oneOrMore { digit() }
            .char('.')
            .oneOrMore { digit() }
            .char('.')
            .oneOrMore { digit() }
            .zeroOrMoreLazy { anyChar() }
    fun matcher(quote: String): Regex {
        return capture { oneOrMore { anyOf { range('a', 'z').range('A', 'Z').whitespaceChar() } } }
            .string("($quote")
            .namedCapture("groupId") { subexpression(gavCharacters) }
            .char(':')
            .namedCapture("artifactId") { subexpression(gavCharacters) }
            .char(':')
            .capture { subexpression(versionCharacters) }
            .string("$quote)")
            .toRegex()
    }
    fun propertyMatcher(quote: String) =
        oneOrMoreLazy { anyChar() }
            .atLeast(1) { whitespaceChar() }
            .char('=')
            .atLeast(1) { whitespaceChar() }
            .subexpression(versionCharacters)
            .toRegex()
}
