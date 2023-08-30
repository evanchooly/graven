package com.antwerkz.build

import com.antwerkz.build.model.TargetPlugin
import java.io.File
import java.util.Properties
import javax.inject.Named
import javax.inject.Singleton
import org.apache.maven.AbstractMavenLifecycleParticipant
import org.apache.maven.execution.MavenSession
import org.apache.maven.model.Model
import org.apache.maven.model.Plugin
import org.apache.maven.model.PluginExecution
import org.apache.maven.project.MavenProject
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
            listOf(
                TargetPlugin(
                    "org.apache.maven.plugins",
                    "maven-compiler-plugin",
                    "testClasses",
                    "test-compile",
                    "default-testCompile"
                ),
                TargetPlugin(
                    "org.apache.maven.plugins",
                    "maven-compiler-plugin",
                    "classes",
                    "compile",
                    "default-compile"
                ),
                TargetPlugin(
                    "org.apache.maven.plugins",
                    "maven-jar-plugin",
                    "assemble",
                    "package",
                    "default-jar"
                ),
            )
    }

    private var logger: Logger? = null

    override fun enableLogging(logger: Logger) {
        this.logger = logger
    }

    override fun afterProjectsRead(session: MavenSession) {
        session.projects.forEach { project ->
            val model = project.model
            updateClean(model)
            textReplacements(model)
            applyDefaultPluginReplacements(project)
            attachJar(project, model)
        }
    }

    private fun attachJar(project: MavenProject, model: Model) {
        logger?.info("Attaching gradle artifacts")
        val gradleName = loadGradleName(project)
        val plugins = model.build.plugins
        val plugin = Plugin()
        plugins.add(plugin)
        plugin.groupId = "org.codehaus.mojo"
        plugin.artifactId = "build-helper-maven-plugin"
        plugin.version = "3.4.0"
        plugin.executions.add(
            PluginExecution().also {
                it.id = "attach-artifacts"
                it.phase = "package"
                it.goals.add("attach-artifact")
                it.configuration =
                    dom(
                        "configuration",
                        dom(
                            "artifacts",
                            dom(
                                "artifact",
                                dom("file", "build/libs/${gradleName}.jar"),
                                dom("type", "jar")
                            ),
                            dom(
                                "artifact",
                                dom("file", "build/libs/${gradleName}-javadoc.jar"),
                                dom("type", "jar"),
                                dom("classifier", "javadoc")
                            ),
                            dom(
                                "artifact",
                                dom("file", "build/libs/${gradleName}-sources.jar"),
                                dom("type", "jar"),
                                dom("classifier", "sources")
                            )
                        )
                    )
            }
        )
    }

    private fun loadGradleName(project: MavenProject): String {
        val file = File(project.basedir, "settings.gradle")
        val settings = Properties()
        settings.load(file.inputStream())
        var name =
            (settings["rootProject.name"]
                ?: throw RuntimeException(
                    "settings.gradle must contain the property 'rootProject.name'"
                ))
                as String
        if (name.startsWith('"')) name = name.drop(1).dropLast(1)
        return name
    }

    private fun updateClean(model: Model) {
        val clean = TargetPlugin("org.apache.maven.plugins", "maven-clean-plugin", "", "", "")
        val target = findOrInjectPlugin(model, clean)
        target.executions.add(
            PluginExecution().also {
                it.id = "graven-clean-build-folder"
                it.configuration =
                    dom("configuration", dom("filesets", dom("fileset", dom("directory", "build"))))
            }
        )
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

    private fun applyDefaultPluginReplacements(project: MavenProject) {
        findGravenPlugin(project.model)?.let { plugin ->
            defaults.forEach { targetPlugin ->
                val target = findOrInjectPlugin(project.model, targetPlugin)
                val execution =
                    target.executions.firstOrNull { it.id == targetPlugin.executionId }
                        ?: PluginExecution().also {
                            it.id = targetPlugin.executionId
                            target.executions.add(it)
                        }
                execution.phase = "none"

                project.addLifecyclePhase(targetPlugin.phase)

                plugin.executions.add(
                    PluginExecution().also {
                        it.id = "injected-graven-${targetPlugin.artifactId}-${targetPlugin.phase}"
                        it.goals.add(GradleInvocationMojo.MOJO_NAME)
                        it.phase = targetPlugin.phase
                        it.configuration =
                            dom("configuration", dom("task", dom("name", targetPlugin.gradleTask)))
                    }
                )
            }
        }
    }

    private fun findGravenPlugin(model: Model) =
        model.build.plugins.firstOrNull { plugin ->
            (GROUPID == plugin.groupId) && (ARTIFACTID == plugin.artifactId)
        }

    private fun findOrInjectPlugin(model: Model, target: TargetPlugin): Plugin {
        return findPlugin(model, target) ?: injectPlugin(model, target)
    }

    private fun findPlugin(model: Model, target: TargetPlugin) =
        model.build.plugins.firstOrNull { plugin ->
            (target.groupId == plugin.groupId) && (target.artifactId == plugin.artifactId)
        }

    private fun injectPlugin(model: Model, target: TargetPlugin): Plugin {
        val plugin = Plugin()
        model.build.plugins.add(plugin)
        plugin.groupId = target.groupId
        plugin.artifactId = target.artifactId

        return plugin
    }

    private fun dom(name: String, vararg children: Xpp3Dom): Xpp3Dom {
        val dom = Xpp3Dom(name)
        children.forEach { dom.addChild(it) }

        return dom
    }

    private fun dom(name: String, value: String): Xpp3Dom {
        val dom = Xpp3Dom(name)
        dom.value = value
        return dom
    }

    private fun showExecutions(plugin: Plugin) {
        println("**************** plugin.artifactId = ${plugin.artifactId}")
        plugin.executions.forEachIndexed { index, pluginExecution ->
            println("**************** execution $index: id =    ${pluginExecution.id}")
            println("**************** execution $index: phase = ${pluginExecution.phase}")
            println("**************** execution $index: goals = ${pluginExecution.goals}")
        }
    }
}
