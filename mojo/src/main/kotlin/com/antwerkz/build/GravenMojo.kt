package com.antwerkz.build

import java.io.File
import java.nio.charset.Charset
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
        fun replace(groups: Map<String, Dependency>, input: String): String {
            Updaters.values().forEach { factory: Updaters ->
                val replacer = factory.create(input)
                if (replacer.matches()) {
                    val dependency = groups["${replacer.groupId}:${replacer.artifactId}"]
                    dependency?.let { dep ->
                        return replacer.replace(dep)
                    }
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

    @Parameter(defaultValue = "build.gradle, build.gradle.kts,gradle.properties,settings.gradle")
    lateinit var files: String

    override fun execute() {
        val dependencies = project.dependencies.groupDeps()
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
