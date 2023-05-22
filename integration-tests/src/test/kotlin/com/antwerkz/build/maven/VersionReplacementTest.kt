package com.antwerkz.build.maven

import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.util.Properties
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.InvocationRequest
import org.apache.maven.shared.invoker.InvocationResult
import org.apache.maven.shared.invoker.InvokerLogger
import org.apache.maven.shared.invoker.PrintStreamLogger
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class VersionReplacementTest : MavenTester() {
    @Test
    fun doubleQuoteUpdates() {
        val testDir = initProject("projects/doubleQuotes")

        val result = setupAndInvoke(testDir)

        assertEquals(result.exitCode, 0)
        val lines = File(testDir, "build.gradle.kts").readLines(Charset.forName("UTF-8"))

        assertTrue(
            lines.any { it.contains("implementation(\"org.apache.maven:maven-model:2.3.1\")") }
        )
        assertTrue(
            lines.any {
                it.contains("classpath(\"com.fasterxml.jackson.core:jackson-databind:2.14.1\")")
            }
        )
        assertTrue(lines.any { it.contains("kotlin(\"jvm\") version \"1.8.0\"") })
    }

    @Test
    fun noRegex() {
        val testDir = initProject("projects/noRegex")

        val result = setupAndInvoke(testDir)

        assertEquals(result.exitCode, 0)
        val lines = File(testDir, "build.gradle.kts").readLines(Charset.forName("UTF-8"))

        assertTrue(
            lines.any { it.contains("implementation(\"org.apache.maven:maven-model:2.3.1\")") }
        )
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

        val result = setupAndInvoke(testDir)

        assertEquals(result.exitCode, 0)
        val lines = File(testDir, "gradle.properties").readLines(Charset.forName("UTF-8"))

        assertTrue(lines.any { it.contains("guava.version=31.1-jre") })
        assertTrue(lines.any { it.contains("testng.version=7.8.0") })
    }

    @Test
    fun singleQuoteUpdates() {
        val testDir = initProject("projects/singleQuotes")

        val result = setupAndInvoke(testDir)

        assertEquals(result.exitCode, 0)
        val lines = File(testDir, "build.gradle").readLines(Charset.forName("UTF-8"))

        assertTrue(
            lines.any { it.contains("implementation('org.apache.maven:maven-model:2.3.1')") }
        )
        assertTrue(
            lines.any {
                it.contains("classpath('com.fasterxml.jackson.core:jackson-databind:2.14.1')")
            }
        )
        assertTrue(lines.any { it.contains("kotlin('jvm') version '1.8.0'") })
    }

    private fun setupAndInvoke(testDir: File, params: Properties = Properties()): InvocationResult {
        val invoker = initInvoker(testDir)

        val request: InvocationRequest = DefaultInvocationRequest()
        request.isBatchMode = true
        request.isDebug = false
        request.isShowErrors = true
        request.properties = params
        request.goals = listOf("test-compile")
        val logger =
            PrintStreamLogger(
                PrintStream(
                    FileOutputStream(File(testDir, "maven-${testDir.name}.log")),
                    true,
                    "UTF-8"
                ),
                InvokerLogger.DEBUG
            )
        invoker.logger = logger
        return invoker.execute(request)
    }
}
