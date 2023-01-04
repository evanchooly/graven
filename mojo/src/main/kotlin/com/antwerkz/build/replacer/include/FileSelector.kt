package com.antwerkz.build.replacer.include

import java.io.File
import org.codehaus.plexus.util.DirectoryScanner

object FileSelector {
    fun listIncludes(
        basedir: String = "/",
        includes: List<String> = emptyList(),
        excludes: List<String> = emptyList()
    ): List<String> {
        if (includes.isEmpty()) {
            return emptyList()
        }
        val directoryScanner = DirectoryScanner()
        directoryScanner.addDefaultExcludes()
        directoryScanner.basedir = File(basedir)
        directoryScanner.setIncludes(includes.toTypedArray())
        directoryScanner.setExcludes(excludes.toTypedArray())
        directoryScanner.scan()
        return directoryScanner.includedFiles.toList()
    }

    /**
     * In case basedir is used elsewhere by users. If this value is set OR the basedir is empty,
     * then don't set the DirectoryScanner's basedir which allows absolute paths to work. Don't like
     * doing this, but it may be the only workaround for existing users and the turn around time for
     * finding issues and releases is large with maven central.
     */
    private const val FLAG_FOR_ABS = "USE_ABSOLUTE_PATH"
}
