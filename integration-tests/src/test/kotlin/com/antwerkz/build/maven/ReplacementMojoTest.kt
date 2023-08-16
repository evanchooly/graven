package com.antwerkz.build.maven

import java.io.File
import java.nio.charset.Charset
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class ReplacementMojoTest : MavenTester() {
    @Test
    fun doubleQuoteUpdates() {
        val testDir = initProject("projects/doubleQuotes")

        val (result, output) = setupAndInvoke(testDir)

        assertEquals(result.exitCode, 0, output.toLogFormat())
        val lines = File(testDir, "build.gradle.kts").readLines(Charset.forName("UTF-8"))

        assertTrue(lines.any { it.contains("classpath(\"org.apache.maven:maven-model:3.9.1\")") })
        assertTrue(
            lines.any {
                it.contains("classpath(\"com.fasterxml.jackson.core:jackson-databind:2.14.1\")")
            }
        )
        assertTrue(lines.any { it.contains("kotlin(\"jvm\") version \"1.8.10\"") })
    }

    @Test
    fun noRegex() {
        val testDir = initProject("projects/noRegex")

        val (result, output) = setupAndInvoke(testDir)

        assertEquals(result.exitCode, 0, output.toLogFormat())
        val lines = File(testDir, "build.gradle.kts").readLines(Charset.forName("UTF-8"))

        assertTrue(lines.any { it.contains("classpath(\"org.apache.maven:maven-model:2.3.1\")") })
        assertTrue(
            lines.any {
                it.contains("classpath(\"com.fasterxml.jackson.core:jackson-databind:2.14.1\")")
            }
        )
        assertTrue(lines.any { it.contains("kotlin(\"jvm\") version \"1.6.0\"") })
    }

    @Test
    fun properties() {
        val testDir = initProject("projects/properties")

        val (result, output) = setupAndInvoke(testDir)

        assertEquals(result.exitCode, 0)
        val lines = File(testDir, "gradle.properties").readLines(Charset.forName("UTF-8"))

        assertTrue(lines.any { it.contains("guava.version=31.1-jre") })
        assertTrue(lines.any { it.contains("testng.version=7.8.0") })
    }

    @Test
    fun singleQuoteUpdates() {
        val testDir = initProject("projects/singleQuotes")

        val (result, output) = setupAndInvoke(testDir)

        assertEquals(result.exitCode, 0, output.toLogFormat())
        val lines = File(testDir, "build.gradle").readLines(Charset.forName("UTF-8"))

        assertTrue(
            lines.any { it.contains("implementation('org.apache.maven:maven-model:2.3.1')") }
        )
        assertTrue(
            lines.any {
                it.contains("classpath('com.fasterxml.jackson.core:jackson-databind:2.14.1')")
            }
        )
        assertTrue(lines.any { it.contains("kotlin('jvm') version '1.8.10'") })
    }
}
