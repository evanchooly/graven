package com.antwerkz.build

import com.antwerkz.build.GradleInvocationMojo.Companion.MOJO_NAME
import java.io.File
import java.io.FileOutputStream
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.logging.Logger
import org.zeroturnaround.exec.ProcessExecutor

@Mojo(name = MOJO_NAME)
class GradleInvocationMojo : AbstractMojo() {
    companion object {
        const val MOJO_NAME = "gradle"
        private val baseCommand =
            listOf(
                "java",
                "-Xmx64m",
                "-Xms64m",
                "-Dorg.gradle.appname=gradlew",
                "-cp",
                "gradle/wrapper/gradle-wrapper.jar",
                "org.gradle.wrapper.GradleWrapperMain",
                "--console",
                "plain"
            )
    }

    @Component lateinit var logger: Logger

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    lateinit var project: MavenProject
    @Parameter lateinit var task: GradleTask
    @Parameter(defaultValue = "true") var log = true

    override fun execute() {
        var command = baseCommand
        command += listOf(task.name, *task.args.toTypedArray())
        val executor = ProcessExecutor().command(command).directory(project.basedir)
        if (log) {
            logger.info(
                "invoking gradle:\n\tdirectory: ${project.basedir}\n\tcommand: ${command.joinToString(" ")}"
            )
            val infoLogFile = File(project.basedir, "build/graven/${task.name}-info.log")
            infoLogFile.parentFile.mkdirs()
            logger.debug("Logging enabled. See build/graven for details.")
            executor
                .redirectOutput(FileOutputStream(infoLogFile))
                .redirectError(
                    FileOutputStream(File(project.basedir, "build/graven/${task.name}-error.log"))
                )
        }
        val execute = executor.execute()

        if (execute.exitValue != 0) {
            if (log) {
                throw MojoExecutionException("gradle run failed. check logs in ${File(project.basedir, "build/graven").absolutePath}")
            } else {
                throw MojoExecutionException(
                    "gradle run failed. enabling logging to see the gradle output."
                )
            }
        }
    }
}
