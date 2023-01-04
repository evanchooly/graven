package com.antwerkz.build.maven

import org.apache.maven.shared.invoker.InvocationResult
import org.apache.maven.shared.utils.cli.CommandLineException

class MavenProcessInvocationResult : InvocationResult {
    private var process: Process? = null
    private var exception: CommandLineException? = null
    fun destroy() {
        process?.let {
            it.destroy()
            try {
                it.waitFor()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                e.printStackTrace()
            }
        }
    }

    fun setProcess(process: Process): MavenProcessInvocationResult {
        this.process = process
        return this
    }

    fun setException(exception: CommandLineException): MavenProcessInvocationResult {
        exception.printStackTrace()
        this.exception = exception
        return this
    }

    override fun getExecutionException(): CommandLineException? {
        return exception
    }

    override fun getExitCode(): Int {
        return process?.exitValue() ?: throw IllegalStateException("No process")
    }
}
