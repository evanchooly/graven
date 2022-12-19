package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.file.FileUtilsTest.Companion.utf8
import java.io.File
import org.apache.maven.monitor.logging.DefaultLog
import org.testng.annotations.Test

class SummaryBuilderTest {
    @Test
    fun shouldAddToSummaryAndPrintToLog() {
        val logger = InMemoryLogger()
        val log = DefaultLog(logger)
        val builder = SummaryBuilder()
        builder.add(File("INPUT"), File("OUTPUT"), utf8, log)
        builder.add(File("INPUT"), File("OUTPUT"), utf8, log)
        builder.print(log)
        logger
            .verify()
            .debug("Replacement run on INPUT and writing to OUTPUT with encoding ENCODING", 2)
        logger.verify().info("Replacement run on 2 files.")
    }
}
