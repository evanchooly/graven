package com.antwerkz.build.maven

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.PrintStream
import java.lang.System.getProperty
import java.util.Properties
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.apache.maven.shared.invoker.InvocationRequest
import org.apache.maven.shared.invoker.InvocationResult
import org.apache.maven.shared.invoker.Invoker
import org.apache.maven.shared.invoker.InvokerLogger.DEBUG
import org.apache.maven.shared.invoker.PrintStreamLogger
import org.codehaus.plexus.util.FileUtils.copyDirectoryStructure
import org.slf4j.LoggerFactory

open class MavenTester {
    companion object {
        val LOG = LoggerFactory.getLogger(MavenTester::class.java)
        val mavenHome: File by lazy {
            val env = System.getenv()
            val path = env["Path"] ?: env["PATH"] ?: "."
            path
                .split(File.pathSeparator)
                .map { File(it) }
                .filter { File(it, "mvn").exists() }
                .map { it.parentFile }
                .first()
        }

        val gravenVersion: String by lazy { pomVersion() }

        private fun pomVersion(): String {
            val reader = MavenXpp3Reader()
            val model = reader.read(FileReader("../pom.xml"))
            return model.version
        }

        fun getTargetDir(name: String): File {
            return File("target/test-projects/$name")
        }

        fun initProject(name: String, output: File = getTargetDir(name)): File {
            val tc = File("src/test/resources/maven/projects")
            if (!tc.isDirectory) {
                throw FileNotFoundException(tc.absolutePath)
            }
            val input = File(tc, name)
            if (!input.isDirectory) {
                throw RuntimeException("Cannot find directory: " + input.absolutePath)
            }
            if (output.isDirectory) {
                output.deleteRecursively()
            }
            output.mkdirs()
            try {
                copyDirectoryStructure(input, output)
            } catch (e: IOException) {
                throw RuntimeException("Cannot copy project resources", e)
            }
            return output
        }
    }

    fun initInvoker(): Invoker {
        val invoker: Invoker =
            object : DefaultInvoker() {
                override fun execute(request: InvocationRequest): InvocationResult {
                    env.forEach { request.addShellEnvironment(it.key, it.value) }
                    return super.execute(request)
                }
            }
        invoker.mavenHome = mavenHome
        invoker.localRepositoryDirectory =
            File(
                getProperty("maven.repo.local")
                    ?: File(getProperty("user.home"), ".m2/repository").absolutePath
            )
        return invoker
    }

    protected fun setupAndInvoke(
        testDir: File,
        goals: List<String> = listOf("clean", "test-compile"),
        quiet: Boolean = false,
        params: Properties = Properties(),
    ): Pair<InvocationResult, List<String>> {
        val output = mutableListOf<String>()
        val request: InvocationRequest = DefaultInvocationRequest()
        request.isBatchMode = true
        request.isDebug = false
        request.isShowErrors = true
        request.properties = params
        request.setQuiet(quiet)
        request.goals = goals
        request.baseDirectory = testDir
        request.setOutputHandler { line -> output += line }
        request.properties["graven.version"] = gravenVersion
        request.properties["gradle.version"] = getProperty("gradle.version", "8.2.1")

        val invoker = initInvoker()
        invoker.logger =
            PrintStreamLogger(
                PrintStream(
                    FileOutputStream(File(testDir, "maven-${testDir.name}.log")),
                    true,
                    "UTF-8"
                ),
                DEBUG
            )
        return invoker.execute(request) to output
    }

    val env: Map<String, String>
        get() {
            val env = mutableMapOf<String, String>()
            getProperty("mavenOpts")?.let { env["MAVEN_OPTS"] = it }
            return env
        }
}

fun List<String>.toLogFormat() = joinToString("\n", prefix = "\n")
