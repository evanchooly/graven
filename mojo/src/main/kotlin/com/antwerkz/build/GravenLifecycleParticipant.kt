package com.antwerkz.build

import javax.inject.Named
import javax.inject.Singleton
import org.apache.maven.AbstractMavenLifecycleParticipant
import org.apache.maven.execution.MavenSession
import org.apache.maven.model.Model
import org.apache.maven.model.PluginExecution
import org.codehaus.plexus.component.annotations.Component
import org.codehaus.plexus.logging.LogEnabled
import org.codehaus.plexus.logging.Logger

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
    }
    private var logger: Logger? = null

    override fun enableLogging(logger: Logger) {
        this.logger = logger
    }

    override fun afterSessionStart(session: MavenSession?) {
        throw RuntimeException("am I running?")
    }

    override fun afterProjectsRead(session: MavenSession) {
        println("**************** session = ${session}")
        session.projects.map { project -> project.model }.forEach { model -> replacePlugins(model) }

        throw RuntimeException("am I running?")
    }

    private fun replacePlugins(model: Model) {
        findGravenPlugin(model)?.let { plugin ->
            plugin.executions.forEach { execution: PluginExecution ->
                val configuration = execution.configuration
                logger?.info("**************** configuration = ${configuration}")
                /*
                                findPluginByGA(model, "", "")?.let { target ->
                                    // TODO: better would be to remove them targeted?
                                    // But this mojo has only 3 goals, but only one of them is usable in builds
                                    // ("deploy")
                                    target.executions.clear()
                                    // add executions to nexus-staging-maven-plugin
                                    val execution = PluginExecution()
                                    execution.id = "injected-nexus-deploy"
                                    execution.goals.add("deploy")
                                    execution.phase = "deploy"
                                    execution.configuration = plugin.configuration
                                    plugin.executions.add(execution)
                                }
                */
            }
        }
    }

    protected fun findGravenPlugin(model: Model) = findPluginByGA(model, GROUPID, ARTIFACTID)

    private fun findPluginByGA(model: Model, groupId: String, artifactId: String) =
        model.build
            ?.plugins
            ?.filter { plugin -> (groupId == plugin.groupId) && (artifactId == plugin.artifactId) }
            ?.first()
}
