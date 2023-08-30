package com.antwerkz.build.maven

import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.io.UnsupportedEncodingException
import java.nio.file.Files
import java.util.Properties
import org.apache.commons.io.FileUtils
import org.apache.commons.io.output.TeeOutputStream
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.InvokerLogger
import org.apache.maven.shared.invoker.PrintStreamHandler
import org.apache.maven.shared.invoker.PrintStreamLogger

/**
 * Implementation of verifier using a forked process that is still running while verifying. The
 * process is stop when [RunningInvoker.stop] is called.
 */
class RunningInvoker
@JvmOverloads
constructor(basedir: File, private val debug: Boolean, private val parallel: Boolean = false) :
    MavenProcessInvoker() {
    private var result: MavenProcessInvocationResult? = null
    private val log: File?
    private val outStreamHandler: PrintStreamHandler

    companion object {
        /**
         * Creates a [PrintStream] with an underlying [TeeOutputStream] composed of `one` and `two`
         * OutputStreams
         *
         * @param one
         * @param two
         * @return
         */
        private fun createTeePrintStream(one: OutputStream, two: OutputStream): PrintStream {
            val tee: OutputStream = TeeOutputStream(one, two)
            return try {
                PrintStream(tee, true, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                PrintStream(tee, true)
            }
        }
    }

    private val errStreamHandler: PrintStreamHandler

    init {
        workingDirectory = basedir
        var repo = System.getProperty("maven.repo.local")
        if (repo == null) {
            repo = File(System.getProperty("user.home"), ".m2/repository").absolutePath
        }
        localRepositoryDirectory = File(repo)
        log = File(basedir, "build-" + basedir.name + ".log")
        val outStream: PrintStream?
        outStream =
            try {
                createTeePrintStream(System.out, Files.newOutputStream(log.toPath()))
            } catch (ioe: IOException) {
                System.out
            }
        outStreamHandler = PrintStreamHandler(outStream, true)
        setOutputHandler(outStreamHandler)
        val errStream =
            try {
                createTeePrintStream(System.err, Files.newOutputStream(log.toPath()))
            } catch (ioe: IOException) {
                System.err
            }
        errStreamHandler = PrintStreamHandler(errStream, true)
        setErrorHandler(errStreamHandler)
        logger =
            PrintStreamLogger(outStream, if (debug) InvokerLogger.DEBUG else InvokerLogger.INFO)
    }

    fun stop() {
        result?.destroy()
    }

    fun execute(goals: List<String>, envVars: Map<String, String>): MavenProcessInvocationResult {
        return execute(goals, envVars, Properties())
    }

    fun execute(
        goals: List<String>,
        envVars: Map<String, String>,
        properties: Properties
    ): MavenProcessInvocationResult {
        val request = DefaultInvocationRequest()
        request.goals = goals
        request.isDebug = debug
        if (parallel) {
            request.threads = "1C"
        }
        request.setLocalRepositoryDirectory(localRepositoryDirectory)
        request.baseDirectory = workingDirectory
        request.pomFile = File(workingDirectory, "pom.xml")
        request.properties = properties
        if (System.getProperty("mavenOpts") != null) {
            request.mavenOpts = System.getProperty("mavenOpts")
        } else {
            // we need to limit the memory consumption, as we can have a lot of these processes
            // running at once, if they add default to 75% of total mem we can easily run out
            // of physical memory as they will consume way more than what they need instead of
            // just running GC
            request.mavenOpts = "-Xmx128m"
        }
        request.isShellEnvironmentInherited = true
        envVars.forEach { (name: String?, value: String?) ->
            request.addShellEnvironment(name, value)
        }
        request.setOutputHandler(outStreamHandler)
        request.setErrorHandler(errStreamHandler)
        result = execute(request) as MavenProcessInvocationResult
        return result!!
    }

    fun log(): String? {
        return if (log == null) {
            null
        } else FileUtils.readFileToString(log, "UTF-8")
    }
}
