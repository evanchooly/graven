package com.antwerkz.build.maven

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream
import java.lang.System.getProperty
import java.nio.charset.StandardCharsets
import java.util.Properties
import java.util.function.Predicate
import java.util.regex.Pattern
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.apache.maven.shared.invoker.InvocationRequest
import org.apache.maven.shared.invoker.InvocationResult
import org.apache.maven.shared.invoker.Invoker
import org.apache.maven.shared.invoker.InvokerLogger
import org.apache.maven.shared.invoker.PrintStreamLogger
import org.codehaus.plexus.util.FileUtils.copyDirectoryStructure
import org.slf4j.LoggerFactory
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue

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
        fun initProject(name: String): File {
            val tc = File("target/test-classes/maven")
            if (!tc.isDirectory) {
                LOG.info("test-classes created? ${tc.mkdirs()}")
            }
            val projectRoot = File(tc, name)
            if (!projectRoot.exists()) {
                throw RuntimeException("Cannot find directory: " + projectRoot.absolutePath)
            }
            if (!projectRoot.isDirectory) {
                throw RuntimeException("Not a project directory: " + projectRoot.absolutePath)
            }
            return projectRoot
        }

        fun getTargetDir(name: String): File {
            return File("target/test-classes/$name")
        }

        fun initProject(name: String, output: String): File {
            val tc = File("target/test-classes")
            if (!tc.isDirectory) {
                LOG.info("test-classes created? ${tc.mkdirs()}")
            }
            val input = File(tc, name)
            if (!input.isDirectory) {
                throw RuntimeException("Cannot find directory: " + input.absolutePath)
            }
            val out = File(tc, output)
            if (out.isDirectory) {
                out.deleteRecursively()
            }
            val mkdir: Boolean = out.mkdirs()
            LOG.info("${out.absolutePath} created? $mkdir")
            try {
                copyDirectoryStructure(input, out)
            } catch (e: IOException) {
                throw RuntimeException("Cannot copy project resources", e)
            }
            return out
        }

        fun assertThatOutputWorksCorrectly(logs: String) {
            assertFalse(logs.isEmpty())
            val infoLogLevel = "INFO"
            assertTrue(logs.contains(infoLogLevel))
            val datePattern: Predicate<String> =
                Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2},\\d{3}").asPredicate()
            assertTrue(datePattern.test(logs))
            assertTrue(
                logs.contains("cdi, resteasy, smallrye-context-propagation, vertx, websockets")
            )
            assertFalse(logs.contains("JBoss Threads version"))
        }

        fun loadPom(directory: File): Model {
            val pom = File(directory, "pom.xml")
            assertTrue(pom.isFile)
            try {
                InputStreamReader(FileInputStream(pom), StandardCharsets.UTF_8).use { isr ->
                    return MavenXpp3Reader().read(isr)
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("Cannot read the pom.xml file", e)
            }
        }

        fun getFilesEndingWith(dir: File, suffix: String): List<File> {
            val files: Array<File>? =
                dir.listFiles { _: File, name: String -> name.endsWith(suffix) }
            return if (files != null) listOf(*files) else emptyList()
        }
    }

    fun initInvoker(root: File): Invoker {
        val invoker: Invoker =
            object : DefaultInvoker() {
                override fun execute(request: InvocationRequest): InvocationResult {
                    env.forEach { request.addShellEnvironment(it.key, it.value) }
                    return super.execute(request)
                }
            }
        invoker.mavenHome = mavenHome
        invoker.workingDirectory = root
        invoker.localRepositoryDirectory =
            File(
                getProperty("maven.repo.local")
                    ?: File(getProperty("user.home"), ".m2/repository").absolutePath
            )
        return invoker
    }

    protected fun setupAndInvoke(
        testDir: File,
        goals: List<String> = listOf("test-compile"),
        quiet: Boolean = false,
        params: Properties = Properties()
    ): Pair<InvocationResult, List<String>> {
        val output = mutableListOf<String>()
        val request: InvocationRequest = DefaultInvocationRequest()
        request.isBatchMode = true
        request.isDebug = false
        request.isShowErrors = true
        request.properties = params
        request.setQuiet(quiet)
        request.goals = goals
        request.setOutputHandler { line -> output += line }

        val invoker = initInvoker(testDir)
        invoker.logger =
            PrintStreamLogger(
                PrintStream(
                    FileOutputStream(File(testDir, "maven-${testDir.name}.log")),
                    true,
                    "UTF-8"
                ),
                InvokerLogger.DEBUG
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
