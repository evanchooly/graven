package com.antwerkz.build

import com.antwerkz.build.GravenMojo.Companion.groupDeps
import org.apache.maven.model.Dependency
import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class GravenMojoTest {
    private fun inc(value: String): String {
        return "REPLACED"
    }

    @Test(dataProvider = "versions")
    fun versionMatching(tests: VersionTests) {
        val deps = groupDeps(listOf(Dependency().also {
            it.groupId = "org.apache.maven"
            it.artifactId = "maven-model"
            it.version = "3.8.6"
        },
        Dependency().also {
            it.groupId = "com.fasterxml.jackson.core"
            it.artifactId = "jackson-databind"
            it.version = "2.14.1"
        }))
        assertEquals(GravenMojo.replace(deps, tests.input), tests.target)
    }

    @DataProvider(name = "versions")
    fun versions(): Iterator<Any> =
        listOf(
            VersionTests(
                "        classpath(\"org.apache.maven:maven-model:1.2.3\")",
                "        classpath(\"org.apache.maven:maven-model:3.8.6\")"),
            VersionTests(
                "        implementation(\"com.fasterxml.jackson.core:jackson-databind:1.2.3\")",
                "        implementation(\"com.fasterxml.jackson.core:jackson-databind:2.14.1\")")
        ).iterator()
}

data class VersionTests(val input: String, val target: String)