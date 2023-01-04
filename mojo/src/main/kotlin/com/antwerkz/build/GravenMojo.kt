package com.antwerkz.build

import com.antwerkz.expression.RegularExpression.Companion.capture
import com.antwerkz.expression.RegularExpression.Companion.oneOrMore
import com.antwerkz.expression.toRegex
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
            return versionMatcher.matchEntire(input)?.let {
                val groupId = it.groups["groupId"]!!.value
                val artifactId = it.groups["artifactId"]!!.value
                val dependency = groups["${groupId}:${artifactId}"]
                dependency?.let {
                    input.replace(versionMatcher, "$1(\"$2:$3:${it.version}\")")
                }
            } ?: input
        }
        fun groupDeps(dependencies: List<Dependency>): Map<String, Dependency> {
            val groups: Map<String, Dependency> = dependencies
                .groupBy {
                    "${it.groupId}:${it.artifactId}"
                }.map { it.key to it.value.first() }
                .toMap()
            return groups
        }

        val gavCharacters = //"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-"
            oneOrMore {
                anyOf {
                    range('a', 'z')
                        .range('A', 'Z')
                        .char('-')
                        .char('.')
                }
            }
        private val versionMatcher =
            capture {
                oneOrMore {
                    anyOf {
                        range('a', 'z')
                            .range('A', 'Z')
                            .whitespaceChar()
                    }
                }
            }
                .string("(\"")
                .namedCapture("groupId") { subexpression(gavCharacters) }
                .char(':')
                .namedCapture("artifactId") { subexpression(gavCharacters) }
                .char(':')
                .capture {
                    oneOrMore { digit() }
                        .char('.')
                        .oneOrMore { digit() }
                        .char('.')
                        .oneOrMore { digit() }
                        .zeroOrMoreLazy { anyChar() }
                }
                .string("\")")
                .toRegex()
    }

    @Component
    lateinit var logger: Logger

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    lateinit var project: MavenProject

    override fun execute() {
        val groups: Map<String, Dependency> = groupDeps(project.dependencies)
        var gradleBuild = File("build.gradle")
        if (!gradleBuild.exists()) {
            gradleBuild = File("build.gradle.kts")
        }
        val lines = gradleBuild.readLines(Charset.defaultCharset())
            .map { line: String ->
                replace(groups, line)
            }

        gradleBuild.writeText(lines.joinToString("\n"))
    }
}
