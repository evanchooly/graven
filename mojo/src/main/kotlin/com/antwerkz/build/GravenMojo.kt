package com.antwerkz.build

import com.antwerkz.expression.RegularExpression
import com.antwerkz.expression.toRegex
import java.io.File
import java.nio.charset.Charset
import java.util.Properties
import org.apache.maven.model.Dependency
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_SOURCES
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.logging.Logger

@Mojo(name = "graven", defaultPhase = PROCESS_SOURCES)
class GravenMojo : AbstractMojo() {
    companion object {
        val PROPERTY_MATCHER =
            RegularExpression.startOfInput()
                .zeroOrMore { char(' ') }
                .anyOfChars(":=")
                .zeroOrMore { char(' ') }
                .oneOrMore { anyChar() }
                .toRegex()

        fun replace(groups: Map<String, Dependency>, input: String): String {
            Updaters.values().forEach { factory: Updaters ->
                val replacer = factory.create(input)
                if (replacer.matches()) {
                    groups["${replacer.groupId}:${replacer.artifactId}"]?.let { dep ->
                        return replacer.replace(dep)
                    }
                }
            }

            return input
        }

        private fun replace(properties: Properties, input: String): String {
            properties.entries.forEach { (k, v) ->
                val key = k.toString()
                if (input.startsWith(key) && input.removePrefix(key).matches(PROPERTY_MATCHER)) {
                    return "$key=$v"
                }
            }
            return input
        }
    }

    @Component lateinit var logger: Logger

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    lateinit var project: MavenProject

    @Parameter var replacements: List<RegexReplacement> = listOf()

    @Parameter var mappedProperties: List<MappedProperty> = listOf()

    @Parameter(defaultValue = "build.gradle,build.gradle.kts,gradle.properties,settings.gradle")
    lateinit var files: String

    @Parameter(defaultValue = "8.1.1") lateinit var gradleVersion: String

    override fun execute() {
        val dependencies = project.dependencies.groupDeps()
        val properties = project.properties
        updateGradleWrapper()
        files
            .split(",")
            .map { it.trim() }
            .forEach {
                val file = File(it)

                if (file.exists()) {
                    file.writeText(
                        file
                            .readLines(Charset.defaultCharset())
                            .map { line: String -> replace(dependencies, line) }
                            .map { line: String -> replace(properties, line) }
                            .map {
                                var line = it
                                replacements.forEach { replacement ->
                                    line = replacement.replace(line)
                                }
                                line
                            }
                            .joinToString("\n")
                    )
                }
            }
    }

    private fun updateGradleWrapper() {
        val file = File("gradle/wrapper/gradle-wrapper.properties")

        file.writeText(
            file
                .readLines(Charset.defaultCharset())
                .map { line: String ->
                    if (line.startsWith("distributionUrl=", ignoreCase = true)) {
                        """distributionUrl=https://services.gradle.org/distributions/gradle-${gradleVersion}-bin.zip"""
                    } else {
                        line
                    }
                }
                .joinToString("\n")
        )
    }
}

class RegexReplacement() {
    lateinit var pattern: String
    lateinit var value: String
    private val regex by lazy { Regex(pattern) }

    constructor(pattern: String, value: String) : this() {
        this.pattern = pattern
        this.value = value
    }

    fun replace(input: String): String {
        return input.replace(regex, value)
    }

    override fun toString(): String {
        return "Replacement(pattern='$pattern', value='$value')"
    }
}

data class MappedProperty(var gradleProperty: String = "", var mavenProperty: String = "")

fun List<Dependency>.groupDeps(): Map<String, Dependency> {
    return groupBy { "${it.groupId}:${it.artifactId}" }.map { it.key to it.value.first() }.toMap()
}
