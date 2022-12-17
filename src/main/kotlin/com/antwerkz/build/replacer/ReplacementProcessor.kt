package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.file.FileUtils
import java.nio.charset.Charset

class ReplacementProcessor {
    fun replace(
        replacements: List<Replacement>,
        regex: Boolean,
        file: String,
        outputFile: String,
        regexFlags: Int,
        encoding: Charset
    ) {
        var content: String = FileUtils.readFile(file, encoding)
        for (replacement in replacements) {
            content = replaceContent(regex, regexFlags, content, replacement)
        }
        FileUtils.writeToFile(outputFile, content, encoding)
    }

    private fun replaceContent(
        regex: Boolean,
        regexFlags: Int,
        content: String,
        replacement: Replacement
    ): String {
        require(replacement.getToken().isNotEmpty()) { "Token or token file required" }
        val replacer = TokenReplacer()
        return replacer.replace(content, replacement, regex, regexFlags)
    }
}
