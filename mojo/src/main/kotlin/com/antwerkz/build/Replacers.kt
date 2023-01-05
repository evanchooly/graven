package com.antwerkz.build

import com.antwerkz.expression.RegularExpression.Companion.anyChar
import com.antwerkz.expression.RegularExpression.Companion.atLeast
import com.antwerkz.expression.RegularExpression.Companion.capture
import com.antwerkz.expression.RegularExpression.Companion.oneOrMore
import com.antwerkz.expression.toRegex
import org.apache.maven.model.Dependency

val gavCharacters = oneOrMore {
        anyOf {
            range('a', 'z')
                .range('A', 'Z')
                .char('-')
                .char('.')
        }
    }
val versionCharacters =
    oneOrMore { digit() }
        .char('.')
        .oneOrMore { digit() }
        .char('.')
    .oneOrMore { digit() }
    .zeroOrMoreLazy { anyChar() }

private fun matcher(quote: String): Regex {
    return capture {
        oneOrMore {
            anyOf {
                range('a', 'z')
                    .range('A', 'Z')
                    .whitespaceChar()
            }
        }
    }
        .string("($quote")
        .namedCapture("groupId") { subexpression(gavCharacters) }
        .char(':')
        .namedCapture("artifactId") { subexpression(gavCharacters) }
        .char(':')
        .capture { subexpression(versionCharacters) }
        .string("$quote)")
        .toRegex()
}

enum class Replacers {
    DOUBLE_QUOTED_VERSIONS {
        override fun create(input: String): Replacer {
            return object: Replacer(matcher("\""), input) {
                override fun replace(dependency: Dependency): String {
                    return input.replace(regex, "$1(\"$2:$3:${dependency.version}\")")
                }
            }
        }

    },
    SINGLE_QUOTED_VERSIONS {
        override fun create(input: String): Replacer {
            return object: Replacer(matcher("'"), input) {
                override fun replace(dependency: Dependency): String {
                    return input.replace(regex, "$1('$2:$3:${dependency.version}')")
                }
            }
        }

    };
//    GROOVY_PLUGINS,
//    KOTLIN_PLUGINS

    abstract fun create(input: String): Replacer
}

abstract class Replacer(val regex: Regex, input: String ) {
    private val result = regex.matchEntire(input)
    val groupId: String by lazy {
        result!!.groups["groupId"]!!.value
    }
    val artifactId: String by lazy {
        result!!.groups["artifactId"]!!.value
    }

    abstract fun replace(dependency: Dependency): String

    fun matches() = result != null
}