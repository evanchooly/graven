package com.antwerkz.build.maven

import org.testng.annotations.Test

class GradleInvocationTest : MavenTester() {
    @Test
    fun invokeGradle() {
        val testDir = initProject("gradleInvoke")
        val (_, welcome) = setupAndInvoke(testDir)
        val string = welcome.toLogFormat()
        println(string)
        /*
                assertTrue(welcome.contains("[INFO] > hello, graven!"), string)

                val (_, packaging) = setupAndInvoke(testDir, goals = listOf("package"))
                string = packaging.toLogFormat()
                assertTrue(packaging.contains("[INFO] > hello, graven!"), string)
                assertTrue(packaging.contains("[INFO] > working hard"), string)
        */
    }
}
