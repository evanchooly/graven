package com.antwerkz.build.replacer

import java.nio.charset.Charset
import org.apache.maven.plugin.logging.Log

class SummaryBuilder {
    private var filesReplaced = 0
    fun add(inputFile: String?, outputFile: String?, encoding: Charset, log: Log) {
        log.debug(String.format(FILE_DEBUG_FORMAT, inputFile, outputFile, encoding))
        filesReplaced++
    }

    fun print(log: Log) {
        log.info(String.format(SUMMARY_FORMAT, filesReplaced, if (filesReplaced > 1) "s" else ""))
    }

    companion object {
        private const val FILE_DEBUG_FORMAT =
            "Replacement run on %s and writing to %s with encoding %s"
        private const val SUMMARY_FORMAT = "Replacement run on %d file%s."
    }
}
