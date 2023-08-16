package com.antwerkz.build

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.logging.Logger
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.LogOutputStream

@Mojo(name = "gradle")
class GradleInvocationMojo : AbstractMojo() {
    companion object {
        private val baseCommand =
            listOf(
                "java",
                "-cp",
                "gradle/wrapper/gradle-wrapper.jar",
                "org.gradle.wrapper.GradleWrapperMain",
                "--no-daemon",
                "--console",
                "plain"
            )
    }

    @Component lateinit var logger: Logger

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    lateinit var project: MavenProject

    @Parameter lateinit var task: GradleTask

    @Parameter(defaultValue = "true") var quiet = true

    /*
    <configuration>
        <executable>java</executable>
        <workingDirectory>${project.basedir}</workingDirectory>
        <arguments>
            <argument>-cp</argument>
            <argument>gradle/wrapper/gradle-wrapper.jar</argument>
            <argument>org.gradle.wrapper.GradleWrapperMain</argument>
            <argument>classes</argument>
        </arguments>
        <useMavenLogger>true</useMavenLogger>
    </configuration>

         */
    override fun execute() {
        var command = baseCommand
        if (quiet) {
            command += "--quiet"
        }
        command += listOf("-i", task.name, *task.args.toTypedArray())
        logger.debug("executing command: ${command.joinToString(" ")}")
        ProcessExecutor()
            .command(command)
            .directory(project.basedir)
            .redirectOutput(
                object : LogOutputStream() {
                    override fun processLine(line: String) {
                        if (!quiet) {
                            logger.info("> ${line}")
                        }
                    }
                }
            )
            .redirectError(
                object : LogOutputStream() {
                    override fun processLine(line: String) {
                        logger.error("[error] > ${line}")
                    }
                }
            )
            .execute()
    }
}
