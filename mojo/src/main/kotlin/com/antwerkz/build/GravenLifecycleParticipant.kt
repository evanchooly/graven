package com.antwerkz.build

import com.antwerkz.build.model.PluginReplacement
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import javax.inject.Named
import javax.inject.Singleton
import org.apache.maven.AbstractMavenLifecycleParticipant
import org.apache.maven.execution.MavenSession
import org.apache.maven.model.Model
import org.apache.maven.model.Plugin
import org.apache.maven.model.PluginExecution
import org.codehaus.plexus.component.annotations.Component
import org.codehaus.plexus.logging.LogEnabled
import org.codehaus.plexus.logging.Logger
import org.codehaus.plexus.util.xml.Xpp3Dom

@Component(
    role = AbstractMavenLifecycleParticipant::class,
    hint = "com.antwerkz.build.GravenLifecycleParticipant"
)
@Named("com.antwerkz.build.GravenLifecycleParticipant")
@Singleton
class GravenLifecycleParticipant : AbstractMavenLifecycleParticipant(), LogEnabled {
    companion object {
        val GROUPID = "com.antwerkz.build"
        val ARTIFACTID = "graven"
        val defaults =
            listOf<PluginReplacement>(
                //                PluginReplacement(
                //                    "org.apache.maven.plugins",
                //                    "maven-compiler-plugin",
                //                    "classes",
                //                    "default-compile"
                //                ),
                //                PluginReplacement(
                //                    "org.apache.maven.plugins",
                //                    "maven-compiler-plugin",
                //                    "testClasses",
                //                    "default-testCompile"
                //                ),
                )
    }
    private var logger: Logger? = null

    override fun enableLogging(logger: Logger) {
        this.logger = logger
    }

    override fun afterProjectsRead(session: MavenSession) {
        session.projects
            .map { project -> project.model }
            .forEach { model ->
                updateClean(model)
                textReplacements(model)
                applyDefaultPluginReplacements(model)
            }
    }

    private fun updateClean(model: Model) {
        findPluginByGA(model, "org.apache.maven.plugins", "maven-clean-plugin")?.let { target ->
            target.executions.add(
                PluginExecution().also {
                    it.id = "graven-clean-build-folder"
                    it.configuration =
                        configure(
                            mapOf("filesets" to mapOf("fileset" to mapOf("directory" to "build")))
                        )
                }
            )
        }
    }

    private fun textReplacements(model: Model) {
        findGravenPlugin(model)?.let { plugin ->
            plugin.executions.add(
                PluginExecution().also {
                    it.id = "default-graven-replacements"
                    it.goals = listOf("sync")
                    it.phase = "process-sources"
                    it.configuration = plugin.configuration
                }
            )
        }
    }

    private fun applyDefaultPluginReplacements(model: Model) {
        findGravenPlugin(model)?.let { plugin ->
            val configuration = plugin.configuration as Xpp3Dom
            var config = XmlMapper().readValue(configuration.toString(), GravenConfig::class.java)
            defaults.forEach { replacement ->
                findPluginByGA(model, replacement.groupId, replacement.artifactId)?.let { target ->
                    var oldExecution = target.executions.first { it.id == replacement.executionId }
                    target.executions.remove(oldExecution)

                    plugin.executions.add(
                        PluginExecution().also {
                            it.id = "injected-graven-${target.key}"
                            it.goals.add(GradleInvocationMojo.MOJO_NAME)
                            it.phase = oldExecution.phase
                            it.configuration =
                                configure(mapOf("task" to mapOf("name" to replacement.gradleTask)))
                        }
                    )
                }
            }
        }
    }

    private fun configure(map: Map<String, Any>) = Xpp3Dom("configuration").addChildren(map)

    protected fun findGravenPlugin(model: Model) = findPluginByGA(model, GROUPID, ARTIFACTID)

    private fun findPluginByGA(model: Model, groupId: String, artifactId: String): Plugin? {
        return model.build
            ?.plugins
            ?.filter { plugin -> (groupId == plugin.groupId) && (artifactId == plugin.artifactId) }
            ?.first()
    }

    private fun Xpp3Dom.addChildren(children: Map<*, *>): Xpp3Dom {
        children
            .map { entry ->
                Xpp3Dom(entry.key.toString()).also {
                    if (entry.value is Map<*, *>) {
                        it.addChildren((entry.value as Map<*, *>))
                    } else {
                        it.value = (entry.value.toString())
                    }
                }
            }
            .forEach { this.addChild(it) }

        return this
    }
}
