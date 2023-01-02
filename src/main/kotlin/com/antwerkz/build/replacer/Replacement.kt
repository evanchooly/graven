package com.antwerkz.build.replacer

import java.io.File
import java.nio.charset.Charset
import org.apache.commons.text.StringEscapeUtils

class Replacement(
    token: String = "",
    value: String = "",
    tokenFile: File? = null,
    valueFile: File? = null,
    unescape: Boolean = false,
    var encoding: Charset = Charset.forName("UTF-8")
) {
    private var delimiter: DelimiterBuilder? = null
    var isUnescape = unescape
    var token: String
        get() {
            val newToken = if (isUnescape) unescape(field) else field
            return delimiter?.apply(newToken) ?: newToken
        }
    var value: String
        get() {
            return if (isUnescape) unescape(field) else field
        }

    init {
        this.token = tokenFile?.readText(encoding) ?: token
        this.value = valueFile?.readText(encoding) ?: value
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
                unescape = replacement.isUnescape,
                encoding = replacement.encoding
            )
        }
    }
}
