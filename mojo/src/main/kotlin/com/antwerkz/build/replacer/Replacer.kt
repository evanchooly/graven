package com.antwerkz.build.replacer

interface Replacer {
    fun replace(content: String, replacement: Replacement, regex: Boolean, regexFlags: Int): String
}
