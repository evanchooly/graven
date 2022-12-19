package com.antwerkz.build.replacer

import com.antwerkz.build.replacer.file.FileUtils
import java.io.File
import java.util.Random
import java.util.regex.PatternSyntaxException
import org.apache.maven.monitor.logging.DefaultLog
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.logging.Log
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.mockito.Matchers.argThat
import org.testng.Assert.assertFalse
import org.testng.Assert.assertThrows
import org.testng.Assert.assertTrue
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class ReplacerMojoIntegrationTest {
    lateinit var mojo: ReplacerMojo
    lateinit var filenameAndPath: String
    val logger = InMemoryLogger()
    var mavenLog = DefaultLog(logger)

    @BeforeMethod
    fun setUp() {
        filenameAndPath = createTempFile(TOKEN)

        mojo =
            object : ReplacerMojo() {
                init {
                    basedir = "."
                }

                override fun getLog(): Log {
                    return mavenLog
                }
            }
    }

    @Test
    fun shouldReplaceContentsInFile() {
        mojo.file = filenameAndPath
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo(VALUE))
        logger.verify().info("Replacement run on 1 file.")
    }

    @Test
    fun shouldWarnOfMissingProperities() {
        val inputFile = createTempFile("test-filename-error", TOKEN)
        mojo.inputFilePattern = "(.*)test-(.+)-error"
        mojo.outputFilePattern = "$1test-$2-error.replaced"
        mojo.execute()
        assertFalse(File("$inputFile.replaced").exists())
        logger.verify().warn("No input file/s defined")
    }

    @Test
    fun shouldReplaceContentsInAbsolutePathFile() {
        mojo.file = File(filenameAndPath).absolutePath
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo(VALUE))
        logger.verify().info("Replacement run on 1 file.")
    }

    @Test
    fun shouldReplaceContentsMaintainingSpacesAndNewLines() {
        val valueWithSpacing = " new value" + System.getProperty("line.separator") + " replaced "
        mojo.file = filenameAndPath
        val replacement = Replacement()
        replacement.token = TOKEN
        replacement.value = valueWithSpacing
        mojo.replacements = listOf(replacement)
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo(valueWithSpacing))
        logger.verify().info("Replacement run on 1 file.")
    }

    @Test
    fun shouldIgnoreErrors() {
        mojo.isIgnoreErrors = true
        mojo.file = "invalid"
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo(TOKEN))
        logger.verify().info("Replacement run on 0 file.")
    }

    @Test
    fun shouldReplaceContentsInFileWithBackreferences() {
        val tokenValueMap = createTempFile("test ([^;]*);=group $1 backreferenced")
        filenameAndPath = createTempFile("test 123;")
        mojo.file = filenameAndPath
        mojo.tokenValueMap = tokenValueMap
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo("group 123 backreferenced"))
        logger.verify().info("Replacement run on 1 file.")
    }

    @Test
    fun shouldReplaceContentsInFileWithDelimitedToken() {
        filenameAndPath = createTempFile("@$TOKEN@ and \${$TOKEN}")
        mojo.file = filenameAndPath
        mojo.regex = false
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.delimiters = mutableListOf("@", "\${*}")
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo("$VALUE and $VALUE"))
        logger.verify().info("Replacement run on 1 file.")
    }

    @Test
    fun shouldLogErrorWhenDelimitersHaveRegexAndRegexEnabled() {
        assertThrows(MojoExecutionException::class.java) {
            filenameAndPath = createTempFile("@$TOKEN@ and \${$TOKEN}")
            mojo.file = filenameAndPath
            mojo.token = TOKEN
            mojo.value = VALUE
            mojo.delimiters = mutableListOf("@", "\${*}")
            try {
                mojo.execute()
            } catch (e: PatternSyntaxException) {
                val results: String = File(filenameAndPath).readText()
                assertThat(results, equalTo("@$TOKEN@ and \${$TOKEN}"))
                logger
                    .verify()
                    .error(argThat(containsString("Error: Illegal repetition near index 0")))
                logger.verify().info("Replacement run on 0 file.")
                throw e
            }
        }
    }

    @Test
    fun shouldReplaceContentsInFileButNotReportWhenQuiet() {
        mojo.quiet = true
        mojo.file = filenameAndPath
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.encoding = ENCODING
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo(VALUE))
        assertTrue(logger.info().isEmpty())

        logger
            .verify()
            .debug(
                "Replacement run on .${File.separator}$filenameAndPath and writing to .${File.separator}$filenameAndPath with encoding $ENCODING"
            )
    }

    @Test
    fun shouldReplaceContentsInFileWithTokenContainingEscapedChars() {
        filenameAndPath = createTempFile("test\n123\t456")
        mojo.file = filenameAndPath
        mojo.token = "test\\n123\\t456"
        mojo.value = "$VALUE\\n987"
        mojo.isUnescape = true
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo("""
    $VALUE
    987
    """.trimIndent()))
        logger.verify().info("Replacement run on 1 file.")
    }

    @Test
    fun shouldReplaceAllNewLineChars() {
        filenameAndPath = createTempFile("test" + System.getProperty("line.separator") + "123")
        mojo.file = filenameAndPath
        mojo.token = System.getProperty("line.separator")
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo("test123"))
        logger.verify().info("Replacement run on 1 file.")
    }

    @Test
    fun shouldReplaceRegexCharContentsInFile() {
        filenameAndPath = createTempFile("\$to*ken+")
        mojo.regex = false
        mojo.file = filenameAndPath
        mojo.token = "\$to*ken+"
        mojo.value = VALUE
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo(VALUE))
        logger.verify().info("Replacement run on 1 file.")
    }

    @Test
    fun shouldRegexReplaceContentsInFile() {
        mojo.file = filenameAndPath
        mojo.token = "(.+)"
        mojo.value = "$1$VALUE"
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo(TOKEN + VALUE))
        logger.verify().info("Replacement run on 1 file.")
    }

    @Test
    fun shouldReplaceContentsAndWriteToOutputDirWithBaseDirAndPreservingAsDefault() {
        mojo.basedir = "."
        mojo.file = filenameAndPath
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.outputDir = OUTPUT_DIR
        mojo.execute()
        val results: String = File("./$OUTPUT_DIR$filenameAndPath").readText()
        assertThat(results, equalTo(VALUE))
        logger.verify().info("Replacement run on 1 file.")
    }

    @Test
    fun shouldNotReplaceIfIgnoringMissingFilesAndFileNotExists() {
        assertFalse(File("bogus").exists())
        mojo.file = "bogus"
        mojo.ignoreMissingFile = true
        mojo.execute()
        assertFalse(File("bogus").exists())
        logger.verify().info("Ignoring missing file")
    }

    @Test
    fun shouldRethrowIOExceptionsAsMojoExceptions() {
        assertThrows(MojoExecutionException::class.java) {
            mojo.file = "bogus"
            mojo.execute()
            logger.assertNoLogging()
        }
    }

    @Test
    fun shouldReplaceContentsWithTokenValuesInTokenAndValueFiles() {
        val tokenFilename = createTempFile(TOKEN)
        val valueFilename = createTempFile(VALUE)
        mojo.file = filenameAndPath
        mojo.tokenFile = tokenFilename
        mojo.valueFile = valueFilename
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo(VALUE))
    }

    @Test
    fun shouldReplaceContentsWithTokenValuesInMap() {
        val tokenValueMapFilename: String = createTempFile(listOf("#comment", "$TOKEN=$VALUE"))
        mojo.tokenValueMap = tokenValueMapFilename
        mojo.file = filenameAndPath
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo(VALUE))
    }

    @Test
    fun shouldReplaceContentsWithTokenValuesInMapWithAbsolutePath() {
        val tokenValueMapFilename: String = createTempFile(listOf("$TOKEN=$VALUE"))
        val absolutePath: String = File(tokenValueMapFilename).absolutePath
        mojo.tokenValueMap = absolutePath
        mojo.file = filenameAndPath
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo(VALUE))
    }

    @Test
    fun shouldReplaceContentsWithTokenValuesInMapWithAbsolutePathAndIncludes() {
        val tokenValueMapFilename: String = createTempFile(listOf("$TOKEN=$VALUE"))
        val absolutePath: String = File(tokenValueMapFilename).absolutePath
        mojo.tokenValueMap = absolutePath
        mojo.includes = mutableListOf(filenameAndPath)
        mojo.execute()
        assertThat(File(filenameAndPath).readText(), equalTo(VALUE))
    }

    @Test
    fun shouldWriteIntoTransformedOutputFilesFromInputFilePattern() {
        val inputFile = createTempFile("test-filename", TOKEN)
        mojo.file = inputFile
        mojo.inputFilePattern = "(.*)test-(.+)"
        mojo.outputFilePattern = "$1test-$2.replaced"
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.execute()
        val results: String = File("$inputFile.replaced").readText()
        assertThat(results, equalTo(VALUE))
        logger.verify().info("Replacement run on 1 file.")
    }

    @Test
    fun shouldWriteIntoTransformedOutputFilesFromInputFilePatternFromIncludes() {
        val inputFile = createTempFile("test-filename", TOKEN)
        mojo.includes = mutableListOf(inputFile)
        mojo.inputFilePattern = "(.*)test-(.+)"
        mojo.outputFilePattern = "$1test-$2.replaced"
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.execute()
        val results: String = File("$inputFile.replaced").readText()
        assertThat(results, equalTo(VALUE))
        logger.verify().info("Replacement run on 1 file.")
    }

    @Test
    fun shouldWriteIntoTransformedOutputFilesFromInputFilePatternFromFilesToInclude() {
        val inputFile = createTempFile("test-filename", TOKEN)
        mojo.filesToInclude = inputFile
        mojo.inputFilePattern = "(.*)test-(.+)"
        mojo.outputFilePattern = "$1test-$2.replaced"
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.execute()
        val results: String = File("$inputFile.replaced").readText()
        assertThat(results, equalTo(VALUE))
        logger.verify().info("Replacement run on 1 file.")
    }

    @Test
    fun shouldReplaceContentsWithTokenValuesInInlineMap() {
        val variableTokenValueMap = "$TOKEN=$VALUE"
        mojo.variableTokenValueMap = variableTokenValueMap
        mojo.file = filenameAndPath
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo(VALUE))
    }

    @Test
    fun shouldReplaceContentsWithTokenValuesInDelimiteredMap() {
        filenameAndPath = createTempFile("@$TOKEN@")
        val tokenValueMapFilename: String = createTempFile(listOf("#comment", "$TOKEN=$VALUE"))
        mojo.delimiters = mutableListOf("@")
        mojo.tokenValueMap = tokenValueMapFilename
        mojo.file = filenameAndPath
        mojo.execute()
        val results: String = File(filenameAndPath).readText()
        assertThat(results, equalTo(VALUE))
    }

    @Test
    fun shouldReplaceContentsInFilesToInclude() {
        val include1 = createTempFile(TOKEN)
        val include2 = createTempFile(TOKEN)
        mojo.filesToInclude = "$include1, $include2"
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.execute()
        val include1Results: String = File(include1).readText()
        assertThat(include1Results, equalTo(VALUE))
        val include2Results: String = File(include2).readText()
        assertThat(include2Results, equalTo(VALUE))
    }

    @Test
    fun shouldOnlyReplaceUpToMaxReplacements() {
        val randomBase: String = java.lang.String.valueOf(Random().nextInt(10))
        val include1 = createTempFile("$randomBase/prefix1", TOKEN)
        val include2 = createTempFile("$randomBase/prefix2", TOKEN)
        mojo.isPreserveDir = false
        mojo.includes = mutableListOf("target/$randomBase**/prefix*")
        mojo.token = TOKEN
        mojo.maxReplacements = 1
        mojo.value = VALUE
        mojo.execute()
        val include1Results: String = File(include1).readText()
        val include2Results: String = File(include2).readText()
        assertTrue(
            TOKEN == include1Results && VALUE == include2Results ||
                VALUE == include1Results && TOKEN == include2Results
        )
    }

    @Test
    fun shouldReplaceContentsInIncludeButNotExcludesAndNotPreserveWhenDisabled() {
        val include1 = createTempFile("test/prefix1", TOKEN)
        val include2 = createTempFile("test/prefix2", TOKEN)
        val exclude = createTempFile(TOKEN)
        mojo.isPreserveDir = false
        mojo.includes = mutableListOf("target/**/prefix*")
        mojo.excludes = mutableListOf(exclude)
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.execute()
        assertThat(File(include1).readText(), equalTo(VALUE))
        assertThat(File(include2).readText(), equalTo(VALUE))
        assertThat(File(exclude).readText(), equalTo(TOKEN))
    }

    @Test
    fun shouldPreserveFilePathWhenUsingIncludesAndOutputDir() {
        val include1 = createTempFile("test/prefix1", TOKEN)
        val include2 = createTempFile("test/prefix2", TOKEN)
        val exclude = createTempFile(TOKEN)
        mojo.includes = mutableListOf("target/**/prefix*")
        mojo.excludes = mutableListOf(exclude)
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.execute()
        val include1Results: String = File(include1).readText()
        assertThat(include1Results, equalTo(VALUE))
        val include2Results: String = File(include2).readText()
        assertThat(include2Results, equalTo(VALUE))
        val excludeResults: String = File(exclude).readText()
        assertThat(excludeResults, equalTo(TOKEN))
    }

    @Test
    fun shouldReplaceIncludesThatAreAbsolutePaths() {
        val include1 = createTempFile("test/prefix1", TOKEN)
        val includeAsAbs = File(include1).parentFile.parentFile.absolutePath
        mojo.basedir = "USE_ABSOLUTE_PATH"
        mojo.includes = mutableListOf("$includeAsAbs/**/prefix*")
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.execute()
        assertThat(File(include1).readText(), equalTo(VALUE))
    }

    @Test
    fun shouldReplaceContentsAndWriteToOutputFile() {
        val outputFilename = createTempFile("")
        mojo.file = filenameAndPath
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.outputFile = outputFilename
        mojo.execute()
        val results: String = File(outputFilename).readText()
        assertThat(results, equalTo(VALUE))
    }

    @Test
    fun shouldReplaceContentsInReplacementsInSameFileWhenNoOutputFile() {
        val replacement = Replacement()
        replacement.token = TOKEN
        replacement.value = VALUE
        mojo.replacements = listOf(replacement)
        mojo.file = filenameAndPath
        mojo.execute()
        assertThat(File(filenameAndPath).readText(), equalTo(VALUE))
    }

    @Test
    fun shouldWriteToOutputDirBasedOnOutputBaseDir() {
        mojo.outputBasedir = "target/outputBasedir"
        mojo.file = filenameAndPath
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.outputDir = OUTPUT_DIR
        mojo.execute()
        val results: String = File("target/outputBasedir/$OUTPUT_DIR$filenameAndPath").readText()
        assertThat(results, equalTo(VALUE))
    }

    @Test
    fun shouldWriteToFileOutsideBaseDir() {
        val tmpFile = System.getProperty("user.home") + "/tmp/test"
        mojo.file = filenameAndPath
        mojo.token = TOKEN
        mojo.value = VALUE
        mojo.outputFile = tmpFile
        mojo.execute()
        val results: String = File(tmpFile).readText()
        assertThat(results, equalTo(VALUE))
    }

    private fun createTempFile(contents: String): String {
        val filename = Throwable().fillInStackTrace().stackTrace[1].methodName
        return createTempFile(filename, contents)
    }

    private fun createTempFile(filename: String, contents: String): String {
        val file = File("target/" + filename + Random().nextInt())
        FileUtils.ensureFolderStructureExists(file)
        file.writeText(contents)
        file.deleteOnExit()
        return file.absolutePath
    }

    private fun createTempFile(contents: List<String>): String {
        val filename = Throwable().fillInStackTrace().stackTrace[1].methodName
        val file = File("target/$filename")
        file.writeText(contents.joinToString("\n"))
        file.deleteOnExit()
        return "target/" + file.name
    }

    companion object {
        private const val ENCODING = "UTF-8"
        private const val TOKEN = "token"
        private const val VALUE = "value"
        private const val OUTPUT_DIR = "target/outputdir/"
    }
}
