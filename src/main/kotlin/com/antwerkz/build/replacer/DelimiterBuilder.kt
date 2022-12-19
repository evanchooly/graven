package com.antwerkz.build.replacer

class DelimiterBuilder(delimiter: String = "") {
    private val start: String
    private var end: String? = null

    init {
        val startBuilder = StringBuilder()
        val endBuilder = StringBuilder()
        var buildingStart = true
        var hasMiddle = false
        for (c in delimiter) {
            if (c == '*') {
                buildingStart = false
                hasMiddle = true
                continue
            }
            if (buildingStart) {
                startBuilder.append(c)
            } else {
                endBuilder.append(c)
            }
        }
        start = startBuilder.toString()
        if (hasMiddle) {
            end = endBuilder.toString()
        } else {
            end = start
        }
    }

    fun apply(token: String): String {
        return if (token.isEmpty()) {
            token
        } else String.format(FORMAT, start, token, end)
    }

    companion object {
        private const val FORMAT = "%s%s%s"
    }
}
