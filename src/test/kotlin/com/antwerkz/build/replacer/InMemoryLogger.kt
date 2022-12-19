package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.InMemoryLogger.Entry
import org.codehaus.plexus.logging.AbstractLogger
import org.codehaus.plexus.logging.Logger
import org.codehaus.plexus.logging.Logger.LEVEL_DEBUG
import org.codehaus.plexus.logging.Logger.LEVEL_ERROR
import org.codehaus.plexus.logging.Logger.LEVEL_FATAL
import org.codehaus.plexus.logging.Logger.LEVEL_INFO
import org.codehaus.plexus.logging.Logger.LEVEL_WARN
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue

class InMemoryLogger : AbstractLogger(Logger.LEVEL_DEBUG, "in memory") {
    val logLevels: MutableMap<Int, MutableList<Entry>> = mutableMapOf()

    data class Entry(val level: Int, val line: String, val throwable: Throwable?)

    init {
        (LEVEL_DEBUG..LEVEL_FATAL).forEach { logLevels[it] = mutableListOf() }
    }

    fun debug() = logLevels[LEVEL_DEBUG] as MutableList<Entry>
    fun warn() = logLevels[LEVEL_WARN] as MutableList<Entry>
    fun info() = logLevels[LEVEL_INFO] as MutableList<Entry>
    fun error() = logLevels[LEVEL_ERROR] as MutableList<Entry>
    fun fatal() = logLevels[LEVEL_FATAL] as MutableList<Entry>
    override fun debug(message: String, throwable: Throwable?) {
        debug() += Entry(LEVEL_DEBUG, message, throwable)
    }

    override fun info(message: String, throwable: Throwable?) {
        info() += Entry(LEVEL_INFO, message, throwable)
    }

    override fun warn(message: String, throwable: Throwable?) {
        warn() += Entry(LEVEL_WARN, message, throwable)
    }

    override fun error(message: String, throwable: Throwable?) {
        error() += Entry(LEVEL_ERROR, message, throwable)
    }

    override fun fatalError(message: String, throwable: Throwable?) {
        fatal() += Entry(LEVEL_FATAL, message, throwable)
    }

    override fun getChildLogger(name: String): Logger {
        TODO("Not implemented")
    }

    fun assertNoLogging() = assertTrue(logLevels.values.all { it.isEmpty() })
    fun verify() = LogVerify(this)
}

class LogVerify(val log: InMemoryLogger) {
    fun info(line: String, count: Int = 1) = check(log.info(), line, count)
    fun debug(line: String, count: Int = 1) = check(log.debug(), line, count)
    fun warn(line: String, count: Int = 1) = check(log.warn(), line, count)
    fun error(line: String, count: Int = 1) = check(log.error(), line, count)
    fun fatal(line: String, count: Int = 1) = check(log.fatal(), line, count)
    private fun check(entries: List<Entry>, line: String, count: Int) =
        assertEquals(entries.count { it.line.contains(line) }, count)
}
