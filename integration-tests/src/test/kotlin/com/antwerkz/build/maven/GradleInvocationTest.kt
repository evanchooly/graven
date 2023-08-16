package com.antwerkz.build.maven

import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class GradleInvocationTest : MavenTester() {
    @Test
    fun invokeGradle() {
        val testDir = initProject("projects/gradleInvoke")
        val (_, welcome) = setupAndInvoke(testDir)
        var string = welcome.toLogFormat()
        assertTrue(welcome.contains("[INFO] > hello, graven!"), string)

        val (_, packaging) = setupAndInvoke(testDir, goals = listOf("package"))
        string = packaging.toLogFormat()
        assertTrue(packaging.contains("[INFO] > hello, graven!"), string)
        assertTrue(packaging.contains("[INFO] > working hard"), string)
    }
}
