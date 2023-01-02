package com.antwerkz.build.maven

import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.util.Properties
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.InvocationRequest
import org.apache.maven.shared.invoker.InvocationResult
import org.apache.maven.shared.invoker.Invoker
import org.apache.maven.shared.invoker.InvokerLogger
import org.apache.maven.shared.invoker.PrintStreamLogger
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class VersionReplacementTest : MavenTester() {
    private val running: RunningInvoker? = null

    @Test
    fun testProjectGenerationFromScratch() {
        var testDir = initProject("projects/versionReplacements")
        val invoker = initInvoker(testDir)

        val result: InvocationResult = setupAndInvoke(invoker, testDir)

        assertEquals(result.exitCode, 0)

        testDir = File(testDir, "acme")
    }

    private fun setupAndInvoke(
        invoker: Invoker,
        testDir: File,
        params: Properties = Properties()
    ): InvocationResult {
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
