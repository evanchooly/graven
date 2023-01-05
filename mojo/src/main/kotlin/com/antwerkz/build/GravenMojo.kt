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
            Replacers.values()
                .forEach { factory: Replacers ->
                    val replacer = factory.create(input)
                    if (replacer.matches()) {
                        val dependency = groups["${replacer.groupId}:${replacer.artifactId}"]
                        if (dependency != null) {
                            return replacer.replace(dependency)
                        }
                    }
                }

            return input
        }
        fun groupDeps(dependencies: List<Dependency>): Map<String, Dependency> {
            val groups: Map<String, Dependency> = dependencies
                .groupBy {
                    "${it.groupId}:${it.artifactId}"
                }.map { it.key to it.value.first() }
                .toMap()
            return groups
        }

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

        gradleBuild.writeText(
            gradleBuild.readLines(Charset.defaultCharset())
                .joinToString("\n") { line: String -> replace(groups, line)
            }
        )
    }
}
