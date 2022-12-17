package com.antwerkz.build.replacer

import java.io.FileReader
import java.nio.charset.Charset
import org.apache.commons.text.StringEscapeUtils

class Replacement {
    private var delimiter: DelimiterBuilder? = null
    var isUnescape = false
    private var token: String
    private var value: String
    var encoding: Charset

    constructor(token: String, value: String, unescape: Boolean, encoding: Charset) {
        isUnescape = unescape
        this.token = token
        this.value = value
        this.encoding = encoding
    }

    fun setTokenFile(tokenFile: String) {
        token = FileReader(tokenFile, encoding).readText()
    }

    fun setValueFile(valueFile: String?) {
        if (valueFile != null) {
            value = FileReader(valueFile, encoding).readText()
        }
    }

    fun getToken(): String {
        val newToken = if (isUnescape) unescape(token) else token
        return delimiter?.apply(newToken) ?: newToken
    }

    fun getValue(): String {
        return if (isUnescape) unescape(value) else value
    }

    private fun unescape(text: String): String {
        return StringEscapeUtils.unescapeJava(text)
    }

    fun withDelimiter(delimiter: DelimiterBuilder): Replacement {
        this.delimiter = delimiter
        return this
    }

    companion object {
        fun from(replacement: Replacement): Replacement {
            return Replacement(
                replacement.token,
                replacement.value,
                replacement.isUnescape,
                replacement.encoding
            )
        }
    }
}
