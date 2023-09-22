package com.antwerkz.graven.maven

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.lang.System.getProperty
import java.util.Properties
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.apache.maven.shared.invoker.InvocationRequest
import org.apache.maven.shared.invoker.InvocationResult
import org.codehaus.plexus.util.FileUtils.copyDirectoryStructure
import org.slf4j.LoggerFactory
import org.testng.Assert
import org.testng.Assert.assertEquals

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

        val model: Model by lazy {
            val reader = MavenXpp3Reader()
            val read = reader.read(FileReader("../mojo/pom.xml"))
            read
        }

        val gravenGroupId: String by lazy { model.parent.groupId }
        val gravenArtifactid: String by lazy { model.artifactId }
        val gravenVersion: String by lazy { model.parent.version }

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

    fun initInvoker(projectRoot: File, debug: Boolean): DefaultInvoker {
        val invoker =
            object : DefaultInvoker() {
                override fun execute(request: InvocationRequest): InvocationResult {
                    env.forEach { request.addShellEnvironment(it.key, it.value) }
                    return super.execute(request)
                }
            }
        //            RunningInvoker(projectRoot, debug)
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
        debug: Boolean = false,
        params: Properties = Properties(),
    ) {
        val output = File(testDir, "maven-log.txt")
        val request: InvocationRequest = DefaultInvocationRequest()
        request.isBatchMode = true
        request.isDebug = false
        request.isShowErrors = true
        request.properties = params
        request.setQuiet(quiet)
        request.goals = goals
        request.baseDirectory = testDir
        request.setOutputHandler { line -> output.appendText(line + "\n") }
        request.properties["graven.groupId"] = gravenGroupId
        request.properties["graven.artifactId"] = gravenArtifactid
        request.properties["graven.version"] = gravenVersion
        getProperty("gradle.version")?.let { request.properties["gradle.version"] = it }

        assertEquals(
            initInvoker(testDir, debug).execute(request).exitCode,
            0,
            "Maven returned a non-zero exit code"
        )
    }

    protected fun loadValue(testDir: File, fileName: String, propertyName: String): String {
        val settings = Properties()
        val file = File(testDir, fileName)
        settings.load(FileInputStream(file))
        var property = settings.getProperty(propertyName)
        Assert.assertNotNull(
            property,
            "Did not find a ${propertyName} in ${file.absolutePath}: ${settings.toLogFormat()}"
        )

        if (property.startsWith('"')) {
            property = property.drop(1).dropLast(1)
        }
        return property
    }

    protected fun findArtifact(testDir: File, projectName: String, version: String, kind: String) {
        val classifier =
            when (kind) {
                "main" -> ""
                "javadoc" -> "-javadoc"
                "sources" -> "-sources"
                else -> TODO()
            }
        val artifact = File(testDir, "build/libs/$projectName-${version}${classifier}.jar")
        Assert.assertTrue(
            artifact.exists(),
            "Should have found the ${kind} artifact: ${artifact.absolutePath}."
        )
    }

    val env: Map<String, String>
        get() {
            val env = mutableMapOf<String, String>()
            getProperty("mavenOpts")?.let { env["MAVEN_OPTS"] = it }
            return env
        }
}

fun Properties.toLogFormat(): String =
    keys.sortedBy { it.toString() }.joinToString("\n", transform = { it -> "$it: ${this[it]}" })

fun List<String>.toLogFormat() = joinToString("\n", prefix = "\n")
