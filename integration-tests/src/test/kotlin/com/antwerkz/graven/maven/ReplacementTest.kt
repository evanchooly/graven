package com.antwerkz.graven.maven

import java.io.File
import java.nio.charset.Charset
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class ReplacementTest : MavenTester() {
    @Test
    fun doubleQuoteUpdates() {
        val testDir = initProject("doubleQuotes")

        setupAndInvoke(testDir, listOf("process-sources"))

        val lines = File(testDir, "build.gradle.kts").readLines(Charset.forName("UTF-8"))

        find(lines, "classpath(\"org.apache.maven:maven-model:3.9.1\")")
        find(lines, "classpath(\"com.fasterxml.jackson.core:jackson-databind:2.14.1\")")
        find(lines, "kotlin(\"jvm\") version \"1.8.10\"")
    }

    private fun find(lines: List<String>, target: String) {
        assertTrue(
            lines.any { it.contains(target) },
            "Can't find:\n$target \nin \n\n" + lines.toLogFormat()
        )
    }

    @Test
    fun noRegex() {
        val testDir = initProject("noRegex")

        setupAndInvoke(testDir)

        val lines = File(testDir, "build.gradle.kts").readLines(Charset.forName("UTF-8"))

        find(lines, "classpath(\"org.apache.maven:maven-model:2.3.1\")")
        find(lines, "classpath(\"com.fasterxml.jackson.core:jackson-databind:2.14.1\")")
        find(lines, "kotlin(\"jvm\") version \"1.6.0\"")
    }

    @Test
    fun properties() {
        val testDir = initProject("properties")

        setupAndInvoke(testDir)

        val lines = File(testDir, "gradle.properties").readLines(Charset.forName("UTF-8"))

        find(lines, "guava.version = 31.1-jre")
        find(lines, "testng.version = 7.8.0")
    }

    @Test
    fun singleQuoteUpdates() {
        val testDir = initProject("singleQuotes")

        setupAndInvoke(testDir)

        val lines = File(testDir, "build.gradle").readLines(Charset.forName("UTF-8"))

        find(lines, "implementation('org.apache.maven:maven-model:2.3.1')")
        find(lines, "classpath('com.fasterxml.jackson.core:jackson-databind:2.14.1')")
        find(lines, "kotlin('jvm') version '1.8.10'")
    }
}
