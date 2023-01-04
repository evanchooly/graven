package com.antwerkz.build.replacer

import java.io.File
import java.nio.charset.Charset

object ReplacementProcessor {
    fun replace(
        replacements: List<Replacement>,
        regex: Boolean,
        file: File,
        outputFile: File,
        regexFlags: Int,
        encoding: Charset
    ) {
        var content: String = file.readText(encoding)
        for (replacement in replacements) {
            content = replaceContent(regex, regexFlags, content, replacement)
        }
        outputFile.writeText(content, encoding)
    }

    private fun replaceContent(
        regex: Boolean,
        regexFlags: Int,
        content: String,
        replacement: Replacement
    ): String {
        require(replacement.token.isNotEmpty()) { "Token or token file required" }
        return TokenReplacer.replace(content, replacement, regex, regexFlags)
    }
}
