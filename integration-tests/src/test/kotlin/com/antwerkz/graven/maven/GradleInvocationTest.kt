package com.antwerkz.graven.maven

import java.io.File
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class GradleInvocationTest : MavenTester() {
    @Test
    fun invokeGradle() {
        val testDir = initProject("gradleInvoke")

        setupAndInvoke(testDir, goals = listOf("package"))
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
