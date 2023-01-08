package com.antwerkz.build

import org.apache.maven.model.Dependency
import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class GravenMojoTest {
    private val deps = listOf(
                Dependency().also {
                    it.groupId = "org.apache.maven"
                    it.artifactId = "maven-model"
                    it.version = "3.8.6"
                },
                Dependency().also {
                    it.groupId = "com.fasterxml.jackson.core"
                    it.artifactId = "jackson-databind"
                    it.version = "2.14.1"
                }).groupDeps()

    @Test(dataProvider = "versions")
    fun doubleQuoteVersionMatching(test: UpdateTest) {
        assertEquals(GravenMojo.replace(deps, test.input), test.target, test.toString())
    }
    @Test(dataProvider = "versions")
    fun singleQuoteVersionMatching(test: UpdateTest) {
        assertEquals(GravenMojo.replace(deps, test.input.singleQuote()), test.target.singleQuote(), test.toString())
    }

    @Test(dataProvider = "regexes")
    fun rawRegexes(input: String, replacement: RegexReplacement) {
        assertEquals(replacement.replace(input), replacement.value)
    }

    @DataProvider(name = "regexes")
    fun regexes() = listOf(
        arrayOf("kotlin(\"jvm\") version \"1.6.0\"",
            RegexReplacement("(kotlin\\(\"jvm\"\\).*)",
                "kotlin(\"jvm\") version \"1.8.0\"")
    )
    )
        .iterator()

    @DataProvider(name = "versions")
    fun versions(): Iterator<UpdateTest> = listOf(
        UpdateTest(
            "        classpath(\"org.apache.maven:maven-model:1.2.3\")",
            "        classpath(\"org.apache.maven:maven-model:3.8.6\")"),
        UpdateTest(
            "        implementation(\"com.fasterxml.jackson.core:jackson-databind:1.2.3\")",
            "        implementation(\"com.fasterxml.jackson.core:jackson-databind:2.14.1\")"))
        .iterator()
}

private fun String.singleQuote(): String {
    return replace('"', '\'')
}

data class UpdateTest(val input: String, val target: String)