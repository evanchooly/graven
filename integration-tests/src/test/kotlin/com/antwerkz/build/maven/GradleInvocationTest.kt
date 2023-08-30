package com.antwerkz.build.maven

import java.io.File
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class GradleInvocationTest : MavenTester() {
    @Test
    fun invokeGradle() {
        val testDir = initProject("gradleInvoke")

        val (_, packaging) = setupAndInvoke(testDir, goals = listOf("package"))
        assertTrue(
            File(testDir, "build/libs/gradle-invoke-0.1.0-SNAPSHOT.jar").exists(),
            "Should find the main artifact"
        )
        assertTrue(
            File(testDir, "build/libs/gradle-invoke-0.1.0-SNAPSHOT-javadoc.jar").exists(),
            "Should find the javadoc artifact"
        )
        assertTrue(
            File(testDir, "build/libs/gradle-invoke-0.1.0-SNAPSHOT-sources.jar").exists(),
            "Should find the sources artifact"
        )
    }
}
