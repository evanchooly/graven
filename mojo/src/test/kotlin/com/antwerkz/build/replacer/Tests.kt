package com.antwerkz.build.replacer

import java.io.File
import java.nio.charset.Charset
import org.testng.Assert

object Tests {
    val FILE = File("target/inputFile")
    val OUTPUT_FILE = File("target/outputFile")
    const val NEW_CONTENT = "new content"
    const val REGEX_FLAGS = 0
    const val CONTENT = "content"
    const val TOKEN = "token"
    const val VALUE = "value"
    val ascii: Charset = Charset.forName("US-ASCII")
    val utf8: Charset = Charset.forName("UTF-8")

    fun generateFile(file: File, content: String, charset: Charset = Charset.forName("UTF-8")) {
        file.writeText(content, charset)
    }

    fun assertFile(file: File, content: String, encoding: Charset = Charset.forName("UTF-8")) {
        Assert.assertEquals(file.readText(encoding), content)
    }
}
