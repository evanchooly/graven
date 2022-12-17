package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.file.FileUtils
import java.io.BufferedReader
import java.io.StringReader
import java.nio.charset.Charset
import java.util.StringTokenizer

class TokenValueMapFactory() {

    fun replacementsForVariable(
        variable: String,
        commentsEnabled: Boolean,
        unescape: Boolean,
        encoding: Charset
    ): List<Replacement> {
        val tokenizer = StringTokenizer(variable, ",")
        var fragment: String
        val replacements: MutableList<Replacement> = ArrayList()
        while (tokenizer.hasMoreTokens()) {
            fragment = tokenizer.nextToken()
            if (ignoreFragment(fragment, commentsEnabled)) {
                continue
            }
            appendReplacement(replacements, fragment, unescape, encoding)
        }
        return replacements
    }

    fun replacementsForFile(
        tokenValueMapFile: String,
        commentsEnabled: Boolean,
        unescape: Boolean,
        encoding: Charset
    ): List<Replacement> {
        val contents: String = FileUtils.readFile(tokenValueMapFile, encoding)
        val reader = BufferedReader(StringReader(contents))
        var fragment: String
        val replacements: MutableList<Replacement> = ArrayList()
        while (reader.readLine().also { fragment = it } != null) {
            fragment = fragment.trim { it <= ' ' }
            if (ignoreFragment(fragment, commentsEnabled)) {
                continue
            }
            appendReplacement(replacements, fragment, unescape, encoding)
        }
        return replacements
    }

    private fun appendReplacement(
        replacements: MutableList<Replacement>,
        fragment: String?,
        unescape: Boolean,
        encoding: Charset
    ) {
        val token = StringBuilder()
        var value = ""
        var settingToken = true
        for (i in 0 until fragment!!.length) {
            require(!(i == 0 && fragment[0] == SEPARATOR)) { getNoValueErrorMsgFor(fragment) }
            if (settingToken && !isSeparatorAt(i, fragment)) {
                token.append(fragment[i])
            } else if (isSeparatorAt(i, fragment)) {
                settingToken = false
                continue
            } else {
                value = fragment.substring(i)
                break
            }
        }
        if (settingToken) {
            return
        }
        val tokenVal = token.toString().trim { it <= ' ' }
        replacements.add(Replacement(tokenVal, value.trim { it <= ' ' }, unescape, encoding))
    }

    private fun isSeparatorAt(i: Int, line: String?): Boolean {
        return line!![i] == SEPARATOR && line[i - 1] != SEPARATOR_ESCAPER
    }

    private fun getNoValueErrorMsgFor(line: String?): String {
        return "No value for token: $line. Make sure that tokens have values in pairs in the format: token=value"
    }

    private fun ignoreFragment(line: String?, commentsEnabled: Boolean): Boolean {
        return line!!.length == 0 || commentsEnabled && line.startsWith(COMMENT_PREFIX)
    }

    companion object {
        private const val SEPARATOR_ESCAPER = '\\'
        private const val SEPARATOR = '='
        private const val COMMENT_PREFIX = "#"
    }
}
