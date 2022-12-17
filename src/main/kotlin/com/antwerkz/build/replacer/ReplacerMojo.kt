package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.file.FileUtils
import com.antwerkz.build.replacer.include.FileSelector
import java.io.File
import java.nio.charset.Charset
import java.util.Arrays
import java.util.regex.PatternSyntaxException
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_SOURCES
import org.apache.maven.plugins.annotations.Mojo

/**
 * Goal replaces token with value inside file
 *
 * @goal replace
 *
 * @phase compile
 */
@Mojo(name = "replacer", defaultPhase = PROCESS_SOURCES, threadSafe = true)
class ReplacerMojo : AbstractMojo {
    private val tokenValueMapFactory: TokenValueMapFactory
    private val fileSelector: FileSelector
    private val patternFlagsFactory: PatternFlagsFactory
    private val outputFilenameBuilder: OutputFilenameBuilder
    private val summaryBuilder: SummaryBuilder
    private val processor: ReplacementProcessor

    /**
     * File to check and replace tokens. Path to single file to replace tokens in. The file must be
     * text (ascii). Based on current execution path.
     *
     * @parameter
     */
    var file: String? = null

    /**
     * List of files to include for multiple (or single) replacement. In Ant format (*\/directory/
     * **.properties) Cannot use with outputFile.
     *
     * @parameter
     */
    private var includes: MutableList<String> = ArrayList()

    /**
     * List of files to exclude for multiple (or single) replacement. In Ant format (*\/directory/
     * **.properties) Cannot use with outputFile.
     *
     * @parameter
     */
    private var excludes: MutableList<String> = ArrayList()

    /**
     * Comma separated list of includes. This is split up and used the same way a array of includes
     * would be. In Ant format (*\/directory/ **.properties). Files not found are ignored by
     * default.
     *
     * @parameter
     */
    var filesToInclude: String? = null

    /**
     * List of comma separated files to exclude (must have some includes) for multiple (or single)
     * replacement. This is split up and used the same way a array of excludes would be. In Ant
     * format (**\/directory/do-not-replace.properties). The files replaced will be derived from the
     * list of includes and excludes.
     *
     * @parameter
     */
    var filesToExclude: String? = null

    /**
     * Token to replace. The text to replace within the given file. This may or may not be a regular
     * expression (see regex notes above).
     *
     * @parameter
     */
    private var token: String? = null

    /**
     * Token file containing a token to be replaced in the target file/s. May be multiple words or
     * lines. This is useful if you do not wish to expose the token within your pom or the token is
     * long.
     *
     * @parameter
     */
    private var tokenFile: String? = null

    /**
     * Ignore missing target file. Use only with file configuration (not includes etc). Set to true
     * to not fail build if the file is not found. First checks if file exists and exits without
     * attempting to replace anything.
     *
     * @parameter
     */
    private var ignoreMissingFile = false

    /**
     * Value to replace token with. The text to be written over any found tokens. If no value is
     * given, the tokens found are replaced with an empty string (effectively removing any tokens
     * found). You can also reference grouped regex matches made in the token here by $1, $2, etc.
     *
     * @parameter
     */
    private var value: String? = null

    /**
     * A file containing a value to replace the given token with. May be multiple words or lines.
     * This is useful if you do not wish to expose the value within your pom or the value is long.
     *
     * @parameter
     */
    private var valueFile: String? = null

    /**
     * Indicates if the token should be located with regular expressions. This should be set to
     * false if the token contains regex characters which may miss the desired tokens or even
     * replace the wrong tokens.
     *
     * @parameter
     */
    private var regex = true

    /**
     * Output to another file. The input file is read and the final output (after replacing tokens)
     * is written to this file. The path and file are created if it does not exist. If it does
     * exist, the contents are overwritten. You should not use outputFile when using a list of
     * includes.
     *
     * @parameter
     */
    var outputFile: String? = null

    /**
     * Output to another dir. Destination directory relative to the execution directory for all
     * replaced files to be written to. Use with outputDir to have files written to a specific base
     * location.
     *
     * @parameter
     */
    var outputDir: String? = null

    /**
     * Map of tokens and respective values to replace with. A file containing tokens and respective
     * values to replace with. This file may contain multiple entries to support a single file
     * containing different tokens to have replaced. Each token/value pair should be in the format:
     * "token=value" (without quotations). If your token contains ='s you must escape the =
     * character to \=. e.g. tok\=en=value
     *
     * @parameter
     */
    private var tokenValueMap: String? = null

    /**
     * Optional base directory for each file to replace. Path to base relative files for
     * replacements from. This feature is useful for multi-module projects. Default "." which is the
     * default Maven basedir.
     *
     * @parameter
     */
    var basedir = ""

    /**
     * List of standard Java regular expression Pattern flags (see Java Doc). Must contain one or
     * more of:
     * * CANON_EQ
     * * CASE_INSENSITIVE
     * * COMMENTS
     * * DOTALL
     * * LITERAL
     * * MULTILINE
     * * UNICODE_CASE
     * * UNIX_LINES
     *
     * @parameter
     */
    private var regexFlags: List<String> = emptyList()

    /**
     * List of replacements with token/value pairs. Each replacement element to contain sub-elements
     * as token/value pairs. Each token within the given file will be replaced by it's respective
     * value.
     *
     * @parameter
     */
    private var replacements: List<Replacement>? = null

    /**
     * Comments enabled in the tokenValueMapFile. Comment lines start with '#'. If your token starts
     * with an '#' then you must supply the commentsEnabled parameter and with a value of false.
     * Default is true.
     *
     * @parameter default-value="true"
     */
    var isCommentsEnabled = true

    /**
     * Skip running this plugin. Default is false.
     *
     * @parameter default-value="false"
     */
    var isSkip = false

    /**
     * Base directory (appended) to use for outputDir. Having this existing but blank will cause the
     * outputDir to be based on the execution directory.
     *
     * @parameter
     */
    var outputBasedir: String? = null

    /**
     * Parent directory is preserved when replacing files found from includes and being written to
     * an outputDir. Default is true.
     *
     * @parameter default-value="true"
     */
    var isPreserveDir = true

    /**
     * Stops printing a summary of files that have had replacements performed upon them when true.
     * Default is false.
     *
     * @parameter default-value="false"
     */
    private var quiet = false

    /**
     * Unescape tokens and values to Java format. e.g. token\n is unescaped to token(carriage
     * return). Default is false.
     *
     * @parameter default-value="false"
     */
    var isUnescape = false

    /**
     * Add a list of delimiters which are added on either side of tokens to match against. You may
     * also use the '' character to place the token in the desired location for matching. e.g. @
     * would match @token@. e.g. ${} would match ${token}.
     *
     * @parameter
     */
    var delimiters: List<String> = ArrayList()

    /**
     * Variable tokenValueMap. Same as the tokenValueMap but can be an include configuration rather
     * than an outside property file. Similar to tokenValueMap but incline configuration inside the
     * pom. This parameter may contain multiple entries to support a single file containing
     * different tokens to have replaced. Format is comma separated. e.g. token=value,token2=value2
     * Comments are not supported.
     *
     * @parameter
     */
    var variableTokenValueMap: String? = null

    /**
     * Ignore any errors produced by this plugin such as files not being found and continue with the
     * build.
     *
     * First checks if file exists and exits without attempting to replace anything. Only usable
     * with file parameter.
     *
     * Default is false.
     *
     * @parameter default-value="false"
     */
    var isIgnoreErrors = false

    /**
     * X-Path expression for locating node's whose content you wish to replace. This is useful if
     * you have the same token appearing in many nodes but wish to only replace the contents of one
     * or more of them.
     *
     * @parameter
     */
    private var xpath: String? = null

    /**
     * File encoding used when reading and writing files. Default system encoding used when not
     * specified.
     *
     * @parameter default-value="${project.build.sourceEncoding}"
     */
    private var encoding: String = "UTF-8"

    /**
     * Regular expression is run on an input file's name to create the output file with. Must be
     * used in conjunction with outputFilePattern.
     *
     * @parameter
     */
    var inputFilePattern: String? = null

    /**
     * Regular expression groups from inputFilePattern are used in this pattern to create an output
     * file per input file. Must be used in conjunction with inputFilePattern.
     *
     * The parameter outputFile is ignored when outputFilePattern is used.
     *
     * @parameter
     */
    var outputFilePattern: String? = null

    /**
     * Set a maximum number of files which can be replaced per execution.
     *
     * @parameter
     */
    private var maxReplacements = Int.MAX_VALUE

    constructor() : super() {
        tokenValueMapFactory = TokenValueMapFactory()
        fileSelector = FileSelector()
        patternFlagsFactory = PatternFlagsFactory()
        outputFilenameBuilder = OutputFilenameBuilder()
        summaryBuilder = SummaryBuilder()
        processor = ReplacementProcessor()
    }

    constructor(
        processor: ReplacementProcessor,
        tokenValueMapFactory: TokenValueMapFactory,
        fileSelector: FileSelector,
        patternFlagsFactory: PatternFlagsFactory,
        outputFilenameBuilder: OutputFilenameBuilder,
        summaryBuilder: SummaryBuilder
    ) : super() {
        this.processor = processor
        this.tokenValueMapFactory = tokenValueMapFactory
        this.fileSelector = fileSelector
        this.patternFlagsFactory = patternFlagsFactory
        this.outputFilenameBuilder = outputFilenameBuilder
        this.summaryBuilder = summaryBuilder
    }

    override fun execute() {
        try {
            if (isSkip) {
                log.info("Skipping")
                return
            }
            if (checkFileExists()) {
                log.info("Ignoring missing file")
                return
            }
            val replacements: List<Replacement> = getDelimiterReplacements(buildReplacements())
            addIncludesFilesAndExcludedFiles()
            if (includes.isEmpty() && file?.isBlank() == true) {
                log.warn("No input file/s defined")
                return
            }
            if (includes.isEmpty()) {
                replaceContents(processor, limit(replacements), file!!)
                return
            }
            for (file in limit(fileSelector.listIncludes(basedir, includes, excludes))) {
                replaceContents(processor, replacements, file)
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
        return ignoreMissingFile && FileUtils.fileNotExists(getBaseDirPrefixedFilename(file!!))
    }

    private fun getBaseDirPrefixedFilename(file: String): String {
        return if (basedir.isBlank() || FileUtils.isAbsolutePath(file)) {
            file
        } else basedir + File.separator + file
    }

    private fun addIncludesFilesAndExcludedFiles() {
        if (filesToInclude != null) {
            val splitFiles =
                filesToInclude!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            addToList(Arrays.asList<String>(*splitFiles), includes)
        }
        if (filesToExclude != null) {
            val splitFiles =
                filesToExclude!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            addToList(Arrays.asList<String>(*splitFiles), excludes)
        }
    }

    private fun addToList(toAdds: List<String>, destination: MutableList<String>) {
        for (toAdd in toAdds) {
            destination.add(toAdd.trim { it <= ' ' })
        }
    }

    private fun replaceContents(
        processor: ReplacementProcessor,
        replacements: List<Replacement>,
        inputFile: String
    ) {
        val outputFileName: String = outputFilenameBuilder.buildFrom(inputFile, this)
        try {
            processor.replace(
                replacements,
                regex,
                getBaseDirPrefixedFilename(inputFile),
                outputFileName,
                patternFlagsFactory.buildFlags(regexFlags),
                Charset.forName(encoding)
            )
        } catch (e: PatternSyntaxException) {
            if (!delimiters.isEmpty()) {
                log.error(String.format(REGEX_PATTERN_WITH_DELIMITERS_MESSAGE, e.message))
                throw e
            }
        }
        summaryBuilder.add(getBaseDirPrefixedFilename(inputFile), outputFileName, encoding, log)
    }

    private fun buildReplacements(): List<Replacement> {
        if (replacements != null) {
            return replacements!!
        }
        if (variableTokenValueMap != null) {
            return tokenValueMapFactory.replacementsForVariable(
                variableTokenValueMap!!,
                isCommentsEnabled,
                isUnescape,
                Charset.forName(encoding)
            )
        }
        if (tokenValueMap == null) {
            val replacement =
                Replacement(
                    FileUtils.readFile(tokenFile!!, Charset.forName(encoding)),
                    FileUtils.readFile(valueFile!!, Charset.forName(encoding)),
                    isUnescape,
                    Charset.forName(encoding)
                )
            return listOf(replacement)
        }
        var tokenValueMapFile = getBaseDirPrefixedFilename(tokenValueMap!!)
        if (FileUtils.fileNotExists(tokenValueMapFile)) {
            log.info(
                "'$tokenValueMapFile' does not exist and assuming this is an absolute file name."
            )
            tokenValueMapFile = tokenValueMap!!
        }
        return tokenValueMapFactory.replacementsForFile(
            tokenValueMapFile,
            isCommentsEnabled,
            isUnescape,
            Charset.forName(encoding)
        )
    }

    private fun getDelimiterReplacements(replacements: List<Replacement>): List<Replacement> {
        if (delimiters.isEmpty()) {
            return replacements
        }
        val newReplacements: MutableList<Replacement> = ArrayList<Replacement>()
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
