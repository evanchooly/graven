package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.file.FileUtils
import com.antwerkz.build.replacer.include.FileSelector
import java.io.File
import java.nio.charset.Charset
import java.util.regex.PatternSyntaxException
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_SOURCES
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

/**
 * Goal replaces token with value inside file
 *
 * @goal replace
 *
 * @phase compile
 */
@Mojo(name = "replacer", defaultPhase = PROCESS_SOURCES, threadSafe = true)
open class ReplacerMojo : AbstractMojo() {
    private val summaryBuilder = SummaryBuilder()

    /**
     * File to check and replace tokens. Path to single file to replace tokens in. The file must be
     * text (ascii). Based on current execution path.
     */
    @Parameter var file: String? = null

    /**
     * List of files to include for multiple (or single) replacement. In Ant format (*\/directory/
     * **.properties) Cannot use with outputFile.
     */
    @Parameter var includes: MutableList<String> = ArrayList()

    /**
     * List of files to exclude for multiple (or single) replacement. In Ant format (*\/directory/
     * **.properties) Cannot use with outputFile.
     */
    @Parameter var excludes: MutableList<String> = ArrayList()

    /**
     * Comma separated list of includes. This is split up and used the same way a array of includes
     * would be. In Ant format (*\/directory/ **.properties). Files not found are ignored by
     * default.
     */
    @Parameter var filesToInclude: String? = null

    /**
     * List of comma separated files to exclude (must have some includes) for multiple (or single)
     * replacement. This is split up and used the same way a array of excludes would be. In Ant
     * format (**\/directory/do-not-replace.properties). The files replaced will be derived from the
     * list of includes and excludes.
     */
    @Parameter var filesToExclude: String? = null

    /**
     * Token to replace. The text to replace within the given file. This may or may not be a regular
     * expression (see regex notes above).
     */
    @Parameter var token: String? = null

    /**
     * Token file containing a token to be replaced in the target file/s. May be multiple words or
     * lines. This is useful if you do not wish to expose the token within your pom or the token is
     * long.
     */
    @Parameter var tokenFile: String? = null

    /**
     * Ignore missing target file. Use only with file configuration (not includes etc). Set to true
     * to not fail build if the file is not found. First checks if file exists and exits without
     * attempting to replace anything.
     */
    @Parameter var ignoreMissingFile = false

    /**
     * Value to replace token with. The text to be written over any found tokens. If no value is
     * given, the tokens found are replaced with an empty string (effectively removing any tokens
     * found). You can also reference grouped regex matches made in the token here by $1, $2, etc.
     */
    @Parameter var value: String? = null

    /**
     * A file containing a value to replace the given token with. May be multiple words or lines.
     * This is useful if you do not wish to expose the value within your pom or the value is long.
     */
    @Parameter var valueFile: String? = null

    /**
     * Indicates if the token should be located with regular expressions. This should be set to
     * false if the token contains regex characters which may miss the desired tokens or even
     * replace the wrong tokens.
     */
    @Parameter var regex = true

    /**
     * Output to another file. The input file is read and the final output (after replacing tokens)
     * is written to this file. The path and file are created if it does not exist. If it does
     * exist, the contents are overwritten. You should not use outputFile when using a list of
     * includes.
     */
    @Parameter var outputFile: String? = null

    /**
     * Output to another dir. Destination directory relative to the execution directory for all
     * replaced files to be written to. Use with outputDir to have files written to a specific base
     * location.
     */
    @Parameter var outputDir: String? = null

    /**
     * Map of tokens and respective values to replace with. A file containing tokens and respective
     * values to replace with. This file may contain multiple entries to support a single file
     * containing different tokens to have replaced. Each token/value pair should be in the format:
     * "token=value" (without quotations). If your token contains ='s you must escape the =
     * character to \=. e.g. tok\=en=value
     */
    @Parameter var tokenValueMap: String? = null

    /**
     * Optional base directory for each file to replace. Path to base relative files for
     * replacements from. This feature is useful for multi-module projects. Default "." which is the
     * default Maven basedir.
     */
    @Parameter(defaultValue = ".") var basedir = "."

    /**
     * List of standard Java regular expression Pattern flags (see Java Doc). Must contain one or
     * more of: <ul> <li>CANON_EQ <li>CASE_INSENSITIVE <li>COMMENTS <li>DOTALL <li>LITERAL
     * <li>MULTILINE <li>UNICODE_CASE <li>UNIX_LINES </ul>
     */
    @Parameter var regexFlags: List<String> = emptyList()

    /**
     * List of replacements with token/value pairs. Each replacement element to contain sub-elements
     * as token/value pairs. Each token within the given file will be replaced by it's respective
     * value.
     */
    @Parameter(required = true) lateinit var replacements: List<Replacement>

    /**
     * Comments enabled in the tokenValueMapFile. Comment lines start with '#'. If your token starts
     * with an '#' then you must supply the commentsEnabled parameter and with a value of false.
     * Default is true.
     */
    @Parameter(defaultValue = "true") var isCommentsEnabled = true

    /** Skip running this plugin. Default is false. */
    @Parameter(defaultValue = "false") var isSkip = false

    /**
     * Base directory (appended) to use for outputDir. Having this existing but blank will cause the
     * outputDir to be based on the execution directory.
     */
    @Parameter var outputBasedir: String? = null

    /**
     * Parent directory is preserved when replacing files found from includes and being written to
     * an outputDir. Default is true.
     */
    @Parameter(defaultValue = "true") var isPreserveDir = true

    /**
     * Stops printing a summary of files that have had replacements performed upon them when true.
     * Default is false.
     */
    @Parameter(defaultValue = "false") var quiet = false

    /**
     * Unescape tokens and values to Java format. e.g. token\n is unescaped to token(carriage
     * return). Default is false.
     */
    @Parameter(defaultValue = "false") var isUnescape = false

    /**
     * Add a list of delimiters which are added on either side of tokens to match against. You may
     * also use the '' character to place the token in the desired location for matching. e.g. @
     * would match @token@. e.g. ${} would match ${token}.
     */
    @Parameter var delimiters: List<String> = ArrayList()

    /**
     * Variable tokenValueMap. Same as the tokenValueMap but can be an include configuration rather
     * than an outside property file. Similar to tokenValueMap but incline configuration inside the
     * pom. This parameter may contain multiple entries to support a single file containing
     * different tokens to have replaced. Format is comma separated. e.g. token=value,token2=value2
     * Comments are not supported.
     */
    @Parameter var variableTokenValueMap: String? = null

    /**
     * Ignore any errors produced by this plugin such as files not being found and continue with the
     * build.
     *
     * First checks if file exists and exits without attempting to replace anything. Only usable
     * with file parameter.
     *
     * @parameter default-value="false"
     */
    @Parameter(defaultValue = "false") var isIgnoreErrors = false

    /**
     * File encoding used when reading and writing files. Default system encoding used when not
     * specified.
     *
     * @parameter default-value="${project.build.sourceEncoding}"
     */
    @Parameter(defaultValue = "\${project.build.sourceEncoding}") var encoding: String = "UTF-8"

    /**
     * Regular expression is run on an input file's name to create the output file with. Must be
     * used in conjunction with outputFilePattern.
     */
    @Parameter var inputFilePattern: String? = null

    /**
     * Regular expression groups from inputFilePattern are used in this pattern to create an output
     * file per input file. Must be used in conjunction with inputFilePattern.
     *
     * The parameter outputFile is ignored when outputFilePattern is used.
     */
    @Parameter var outputFilePattern: String? = null

    /** Set a maximum number of files which can be replaced per execution. */
    @Parameter var maxReplacements = Int.MAX_VALUE

    override fun execute() {
        if (!::replacements.isInitialized) {
            replacements = buildReplacements()
        }
        try {
            if (isSkip) {
                log.info("Skipping")
                return
            }
            if (checkFileExists()) {
                log.info("Ignoring missing file")
                return
            }
            val replacements: List<Replacement> = getDelimiterReplacements(replacements)
            addIncludesFilesAndExcludedFiles()
            if (includes.isEmpty() && file?.isBlank() == true) {
                log.warn("No input file/s defined")
                return
            }
            if (includes.isEmpty()) {
                replaceContents(limit(replacements), file!!)
                return
            }
            for (file in limit(FileSelector.listIncludes(basedir, includes, excludes))) {
                replaceContents(replacements, file)
            }
        } catch (e: Exception) {
            log.error(e.message)
            if (!isIgnoreErrors) {
                throw MojoExecutionException(e.message, e)
            }
        } finally {
            if (!isSkip && !quiet) {
                summaryBuilder.print(log)
            }
        }
    }

    private fun <T> limit(all: List<T>): List<T> {
        if (all.size > maxReplacements) {
            log.info("Max replacements has been exceeded. Limiting to the first: $maxReplacements")
            return all.subList(0, maxReplacements)
        }
        return all
    }

    private fun checkFileExists(): Boolean {
        if (ignoreMissingFile && file == null) {
            log.error(INVALID_IGNORE_MISSING_FILE_MESSAGE)
            throw MojoExecutionException(INVALID_IGNORE_MISSING_FILE_MESSAGE)
        }
        return ignoreMissingFile && getBaseDirPrefixedFilename(file!!).exists()
    }

    private fun getBaseDirPrefixedFilename(file: String) =
        if (basedir.isBlank() || FileUtils.isAbsolutePath(file)) File(file) else File(basedir, file)

    private fun addIncludesFilesAndExcludedFiles() {
        filesToInclude?.let { files ->
            includes.addAll(files.split(",").dropLastWhile { it.isEmpty() }.toTypedArray())
        }
        filesToExclude?.let { files ->
            excludes.addAll(files.split(",").dropLastWhile { it.isEmpty() }.toTypedArray())
        }
    }

    private fun MutableList<String>.addAll(toAdds: Array<String>) {
        addAll(toAdds.map { it.trim() })
    }

    private fun replaceContents(replacements: List<Replacement>, inputFile: String) {
        val outputFileName: String = OutputFilenameBuilder.buildFrom(inputFile, this)
        val outputFile = File(outputFileName)
        try {
            ReplacementProcessor.replace(
                replacements,
                regex,
                getBaseDirPrefixedFilename(inputFile),
                outputFile,
                PatternFlagsFactory.buildFlags(regexFlags),
                Charset.forName(encoding)
            )
        } catch (e: PatternSyntaxException) {
            if (!delimiters.isEmpty()) {
                log.error(String.format(REGEX_PATTERN_WITH_DELIMITERS_MESSAGE, e.message))
                throw e
            }
        }
        summaryBuilder.add(
            getBaseDirPrefixedFilename(inputFile),
            outputFile,
            Charset.forName(encoding),
            log
        )
    }

    private fun buildReplacements(): List<Replacement> {
        val charset = Charset.forName(encoding)
        if (variableTokenValueMap != null) {
            return TokenValueMapFactory.replacementsForVariable(
                variableTokenValueMap!!,
                isCommentsEnabled,
                isUnescape,
                charset
            )
        }
        if (tokenValueMap == null) {
            val replacement =
                Replacement(
                    tokenFile = File(tokenFile!!),
                    valueFile = File(valueFile!!),
                    unescape = isUnescape,
                    encoding = charset
                )
            return listOf(replacement)
        }
        var tokenValueMapFile = getBaseDirPrefixedFilename(tokenValueMap!!)
        if (!tokenValueMapFile.exists()) {
            log.info(
                "'$tokenValueMapFile' does not exist and assuming this is an absolute file name."
            )
            tokenValueMapFile = File(tokenValueMap!!)
        }
        return TokenValueMapFactory.replacementsForFile(
            tokenValueMapFile,
            isCommentsEnabled,
            isUnescape,
            charset
        )
    }

    private fun getDelimiterReplacements(replacements: List<Replacement>): List<Replacement> {
        if (delimiters.isEmpty()) {
            return replacements
        }
        val newReplacements = mutableListOf<Replacement>()
        for (replacement in replacements) {
            for (delimiter in buildDelimiters()) {
                val withDelimiter: Replacement =
                    Replacement.from(replacement).withDelimiter(delimiter)
                newReplacements.add(withDelimiter)
            }
        }
        return newReplacements
    }

    private fun buildDelimiters(): List<DelimiterBuilder> {
        return delimiters.map { DelimiterBuilder(it) }
    }

    companion object {
        private const val INVALID_IGNORE_MISSING_FILE_MESSAGE =
            "<ignoreMissingFile> only usable with <file>"
        private const val REGEX_PATTERN_WITH_DELIMITERS_MESSAGE =
            "Error: %s. " +
                "Check that your delimiters do not contain regex characters. (e.g. '$'). " +
                "Either remove the regex characters from your delimiters or set <regex>false</regex>" +
                " in your configuration."
    }
}
