package com.antwerkz.graven.maven

import org.testng.annotations.Test

class GradleInvocationTest : MavenTester() {
    @Test
    fun assembleJars() {
        val testDir = initProject("assembleJars")

        setupAndInvoke(testDir, goals = listOf("package"))

        val version = loadValue(testDir, "gradle.properties", "version")
        val projectName = loadValue(testDir, "settings.gradle", "rootProject.name")

        findArtifact(testDir, projectName, version, "main")
        findArtifact(testDir, projectName, version, "javadoc")
        findArtifact(testDir, projectName, version, "sources")
    }
}
