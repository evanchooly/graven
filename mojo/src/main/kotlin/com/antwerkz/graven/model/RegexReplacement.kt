package com.antwerkz.graven.model

import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("replacement")
class RegexReplacement() {
    lateinit var pattern: String
    lateinit var value: String
    val regex by lazy { Regex(pattern) }

    constructor(pattern: String, value: String) : this() {
        this.pattern = pattern
        this.value = value
    }

    fun replace(input: String): String {
        return input.replace(regex, value)
    }

    override fun toString(): String {
        return "Replacement(pattern='$pattern', value='$value')"
    }
}
