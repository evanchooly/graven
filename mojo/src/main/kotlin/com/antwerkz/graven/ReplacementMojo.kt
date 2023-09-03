package com.antwerkz.graven

import com.antwerkz.expression.RegularExpression
import com.antwerkz.expression.toRegex
import com.antwerkz.graven.model.RegexReplacement
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

@Mojo(name = "sync", defaultPhase = PROCESS_SOURCES)
class ReplacementMojo : AbstractMojo() {
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
                    return "$key = $v"
                }
            }
            return input
        }
    }

    @Component lateinit var logger: Logger

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    lateinit var project: MavenProject
    @Parameter var replacements: List<RegexReplacement> = listOf()

    @Parameter(defaultValue = "build.gradle,build.gradle.kts,gradle.properties,settings.gradle")
    lateinit var files: String
    @Parameter(property = "gradle.version") var gradleVersion: String? = null

    override fun execute() {
        val dependencies = project.dependencies.groupDeps()
        val properties = project.properties
        updateGradleWrapper()
        updateProjectVersion()
        files
            .split(",")
            .map { it.trim() }
            .forEach {
                val file = File(project.basedir, it)

                logger.info("Updating ${file}")
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

    private fun updateBuildFiles() {
        val dependencies = project.dependencies.groupDeps()
        val properties = project.properties
        updateGradleWrapper()
        files
            .split(",")
            .map { it.trim() }
            .forEach {
                val file = File(project.basedir, it)

                logger.info("Updating ${file}")
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

    fun updateProjectVersion() {
        val file = File(project.basedir, "gradle.properties")
        val versionMatcher =
            RegularExpression.startOfInput()
                .string("version")
                .optional { char(' ') }
                .anyOfChars("=:")
                .optional { char(' ') }
                .atLeast(1) { anyChar() }
                .toRegex()

        file.writeText(
            file
                .readLines(Charset.defaultCharset())
                .map { line: String ->
                    if (line.matches(versionMatcher)) {
                        "version = ${project.version}"
                    } else line
                }
                .joinToString("\n")
        )
    }

    private fun updateGradleWrapper() {
        gradleVersion?.let {
            val file = File(project.basedir, "gradle/wrapper/gradle-wrapper.properties")
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
}

fun List<Dependency>.groupDeps(): Map<String, Dependency> {
    return groupBy { "${it.groupId}:${it.artifactId}" }.map { it.key to it.value.first() }.toMap()
}
