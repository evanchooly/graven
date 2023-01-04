package com.antwerkz.build.replacer

import org.testng.annotations.Ignore

@Ignore
class ReplacerMojoTest {
    /*
        val logger = InMemoryLogger()
        val log = DefaultLog(logger)

        private val summaryBuilder = SummaryBuilder()
        lateinit var regexFlags: List<String>
        lateinit var mojo: ReplacerMojo

        @BeforeMethod
        fun setUp() {
            regexFlags = listOf(REGEX_FLAG)
            `when`(PatternFlagsFactory.buildFlags(regexFlags)).thenReturn(REGEX_PATTERN_FLAGS)
            mojo =
                object : ReplacerMojo() {
                    override fun getLog(): Log {
                        return this@ReplacerMojoTest.log
                    }
                }
            `when`(OutputFilenameBuilder.buildFrom(FILE.absolutePath, mojo))
                .thenReturn(OUTPUT_FILE.absolutePath)
        }

        @Test
        fun shouldReplaceContentsInReplacements() {
            val replacement: Replacement = mock(Replacement::class.java)
            val replacements: List<Replacement> = listOf(replacement)
            mojo.regexFlags = regexFlags
            mojo.regex = REGEX
            mojo.replacements = replacements
            mojo.file = FILE.absolutePath
            mojo.ignoreMissingFile = true
            mojo.outputFile = OUTPUT_FILE.absolutePath
            mojo.basedir = BASE_DIR
            mojo.encoding = ENCODING
            mojo.execute()
            assertSame(FILE.absolutePath, mojo.file)
            verify(ReplacementProcessor)
                .replace(
                    replacements,
                    REGEX,
                    File(BASE_DIR, FILE.absolutePath),
                    OUTPUT_FILE,
                    REGEX_PATTERN_FLAGS,
                    utf8
                )
            verify(summaryBuilder).add(File(BASE_DIR, FILE.absolutePath), OUTPUT_FILE, utf8, log)
            verify(summaryBuilder).print(log)
        }

        @Test
        fun shouldSkipAndDoNothing() {
            mojo.token = TOKEN
            mojo.value = VALUE
            mojo.file = FILE.absolutePath
            mojo.isSkip = true
            mojo.execute()
            logger.assertNoLogging()
        }

        @Test
        fun shouldIgnoreBaseDirWhenFileIsAbsolutePath() {
            val replacement: Replacement = mock(Replacement::class.java)
            val replacements: List<Replacement> = mutableListOf(replacement)
            `when`(FILE.isAbsolute).thenReturn(true)
            mojo.replacements = replacements
            mojo.file = FILE.absolutePath
            mojo.execute()
            verify(ReplacementProcessor).replace(replacements, REGEX, FILE, OUTPUT_FILE, 0, utf8)
            verify(summaryBuilder).add(FILE, OUTPUT_FILE, utf8, log)
            verify(summaryBuilder).print(log)
        }

        @Test
        fun shouldLimitReplacementsToMaxReplacements() {
            val replacement1: Replacement = mock(Replacement::class.java)
            val replacement2: Replacement = mock(Replacement::class.java)
            val replacements: List<Replacement> = mutableListOf(replacement1, replacement2)
            `when`(FILE.isAbsolute).thenReturn(true)
            mojo.replacements = replacements
            mojo.maxReplacements = 1
            mojo.file = FILE.absolutePath
            mojo.execute()
            verify(ReplacementProcessor)
                .replace(mutableListOf(replacement1), REGEX, FILE, OUTPUT_FILE, 0, utf8)
            verify(summaryBuilder).add(FILE, OUTPUT_FILE, utf8, log)
            verify(summaryBuilder).print(log)
        }

        @Test
        fun shouldReplaceContentsInLocalFile() {
            val replacement: Replacement = mock(Replacement::class.java)
            val replacements: List<Replacement> = mutableListOf(replacement)
            mojo.regexFlags = regexFlags
            mojo.regex = REGEX
            mojo.replacements = replacements
            mojo.file = FILE.name
            mojo.outputFile = OUTPUT_FILE.name
            mojo.execute()
            assertSame(FILE.name, mojo.file)
            verify(ReplacementProcessor)
                .replace(replacements, REGEX, FILE, OUTPUT_FILE, REGEX_PATTERN_FLAGS, utf8)
            verify(summaryBuilder).add(FILE, OUTPUT_FILE, utf8, log)
            verify(summaryBuilder).print(log)
        }

        @Test
        fun shouldReplaceContentsInReplacementsButNotPrintSummaryIfQuiet() {
            val replacement: Replacement = mock(Replacement::class.java)
            val replacements: List<Replacement> = mutableListOf(replacement)
            mojo.quiet = true
            mojo.regexFlags = regexFlags
            mojo.regex = REGEX
            mojo.replacements = replacements
            mojo.file = FILE.name
            mojo.outputFile = OUTPUT_FILE.name
            mojo.basedir = BASE_DIR
            mojo.execute()
            verify(ReplacementProcessor)
                .replace(
                    replacements,
                    REGEX,
                    File(BASE_DIR, FILE.name),
                    OUTPUT_FILE,
                    REGEX_PATTERN_FLAGS,
                    utf8
                )
            verify(summaryBuilder).add(File(BASE_DIR, FILE.name), OUTPUT_FILE, utf8, log)
            verify(summaryBuilder, never()).print(log)
        }

        @Test
        fun shouldReplaceContentsInIncludeAndExcludes() {
            val includes = mutableListOf("include")
            val excludes = mutableListOf("exclude")
            `when`(FileSelector.listIncludes(BASE_DIR, includes, excludes))
                .thenReturn(mutableListOf(FILE.name))
            mojo.includes = includes
            mojo.excludes = excludes
            mojo.token = TOKEN
            mojo.value = VALUE
            mojo.basedir = BASE_DIR
            mojo.execute()
            assertSame(mojo.includes, includes)
            assertSame(mojo.excludes, excludes)
            verify(ReplacementProcessor)
                .replace(
                    argThat(replacementOf(VALUE, false, TOKEN)),
                    eq(REGEX),
                    eq(File(BASE_DIR, FILE.name)),
                    eq(OUTPUT_FILE),
                    anyInt(),
                    eq(utf8)
                )
        }

        @Test
        fun shouldReplaceContentsInFilesToIncludeAndExclude() {
            val includes = "include1, include2"
            val excludes = "exclude1, exclude2"
            `when`(
                    FileSelector.listIncludes(
                        BASE_DIR,
                        mutableListOf("include1", "include2"),
                        mutableListOf("exclude1", "exclude2")
                    )
                )
                .thenReturn(mutableListOf(FILE.name))
            mojo.filesToInclude = includes
            mojo.filesToExclude = excludes
            mojo.token = TOKEN
            mojo.value = VALUE
            mojo.basedir = BASE_DIR
            mojo.execute()
            assertSame(mojo.filesToInclude, includes)
            assertSame(mojo.filesToExclude, excludes)
            verify(ReplacementProcessor)
                .replace(
                    argThat(replacementOf(VALUE, false, TOKEN)),
                    eq(REGEX),
                    eq(File(BASE_DIR, FILE.name)),
                    eq(OUTPUT_FILE),
                    anyInt(),
                    eq(utf8)
                )
        }

        @Test
        fun shouldReplaceContentsWithTokenValuesInMapWithComments() {
            val replacement: Replacement = mock(Replacement::class.java)
            val replacements: List<Replacement> = mutableListOf(replacement)
            `when`(
                    TokenValueMapFactory.replacementsForFile(
                        File(BASE_DIR, TOKEN_VALUE_MAP),
                        commentsEnabled = true,
                        unescape = false,
                        encoding = utf8
                    )
                )
                .thenReturn(replacements)
            mojo.regexFlags = regexFlags
            mojo.regex = REGEX
            mojo.tokenValueMap = TOKEN_VALUE_MAP
            mojo.file = FILE.name
            mojo.outputFile = OUTPUT_FILE.name
            mojo.basedir = BASE_DIR
            mojo.execute()
            verify(ReplacementProcessor)
                .replace(
                    replacements,
                    REGEX,
                    File(BASE_DIR, FILE.name),
                    OUTPUT_FILE,
                    REGEX_PATTERN_FLAGS,
                    utf8
                )
        }

        @Test
        fun shouldReplaceContentsWithTokenValuesInMapWithoutComments() {
            val replacement: Replacement = mock(Replacement::class.java)
            val replacements: List<Replacement> = mutableListOf(replacement)
            `when`(
                    TokenValueMapFactory.replacementsForFile(
                        File(BASE_DIR, TOKEN_VALUE_MAP),
                        commentsEnabled = false,
                        unescape = false,
                        encoding = utf8
                    )
                )
                .thenReturn(replacements)
            mojo.regexFlags = regexFlags
            mojo.regex = REGEX
            mojo.tokenValueMap = TOKEN_VALUE_MAP
            mojo.file = FILE.name
            mojo.outputFile = OUTPUT_FILE.name
            mojo.basedir = BASE_DIR
            mojo.isCommentsEnabled = false
            mojo.encoding = ENCODING
            mojo.execute()
            verify(ReplacementProcessor)
                .replace(
                    replacements,
                    REGEX,
                    File(BASE_DIR, FILE.name),
                    OUTPUT_FILE,
                    REGEX_PATTERN_FLAGS,
                    utf8
                )
        }

        @Test
        fun shouldReplaceContentsWithTokenAndValueWithDelimiters() {
            val delimiters: List<String> = mutableListOf("@", "\${*}")
            mojo.regexFlags = regexFlags
            mojo.regex = REGEX
            mojo.file = FILE.name
            mojo.token = TOKEN
            mojo.value = VALUE
            mojo.outputFile = OUTPUT_FILE.name
            mojo.basedir = BASE_DIR
            mojo.delimiters = delimiters
            mojo.execute()
            assertThat(mojo.delimiters, equalTo(delimiters))
            verify(ReplacementProcessor)
                .replace(
                    argThat(replacementOf(VALUE, false, "@" + TOKEN + "@", "\${" + TOKEN + "}")),
                    eq(REGEX),
                    eq(File(BASE_DIR, FILE.name)),
                    eq(OUTPUT_FILE),
                    eq(REGEX_PATTERN_FLAGS),
                    eq(utf8)
                )
            verify(summaryBuilder).add(File(BASE_DIR, FILE.name), OUTPUT_FILE, utf8, log)
            verify(summaryBuilder).print(log)
        }

        @Test
        fun shouldReplaceContentsWithTokenAndValue() {
            mojo.regexFlags = regexFlags
            mojo.regex = REGEX
            mojo.file = FILE.name
            mojo.token = TOKEN
            mojo.value = VALUE
            mojo.outputFile = OUTPUT_FILE.name
            mojo.basedir = BASE_DIR
            mojo.execute()
            verify(ReplacementProcessor)
                .replace(
                    argThat(replacementOf(VALUE, false, TOKEN)),
                    eq(REGEX),
                    eq(File(BASE_DIR, FILE.name)),
                    eq(OUTPUT_FILE),
                    eq(REGEX_PATTERN_FLAGS),
                    eq(utf8)
                )
            verify(summaryBuilder).add(File(BASE_DIR, FILE.name), OUTPUT_FILE, utf8, log)
            verify(summaryBuilder).print(log)
        }

        @Test
        fun shouldReplaceContentsWithTokenAndValueUnescaped() {
            mojo.regexFlags = regexFlags
            mojo.regex = REGEX
            mojo.file = FILE.name
            mojo.token = TOKEN
            mojo.value = VALUE
            mojo.outputFile = OUTPUT_FILE.name
            mojo.basedir = BASE_DIR
            mojo.isUnescape = true
            mojo.execute()
            assertTrue(mojo.isUnescape)
            verify(ReplacementProcessor)
                .replace(
                    argThat(replacementOf(VALUE, true, TOKEN)),
                    eq(REGEX),
                    eq(File(BASE_DIR, FILE.name)),
                    eq(OUTPUT_FILE),
                    eq(REGEX_PATTERN_FLAGS),
                    eq(utf8)
                )
            verify(summaryBuilder).add(File(BASE_DIR, FILE.name), OUTPUT_FILE, utf8, log)
            verify(summaryBuilder).print(log)
        }

        @Test
        fun shouldReplaceContentsWithTokenValuesInTokenAndValueFiles() {
            `when`(FileUtils.readFile(File(TOKEN_FILE), utf8)).thenReturn(TOKEN)
            `when`(FileUtils.readFile(File(VALUE_FILE), utf8)).thenReturn(VALUE)
            mojo.regexFlags = regexFlags
            mojo.regex = REGEX
            mojo.file = FILE.name
            mojo.tokenFile = TOKEN_FILE
            mojo.valueFile = VALUE_FILE
            mojo.outputFile = OUTPUT_FILE.name
            mojo.basedir = BASE_DIR
            mojo.encoding = ENCODING
            mojo.execute()
            verify(ReplacementProcessor)
                .replace(
                    argThat(replacementOf(VALUE, false, TOKEN)),
                    eq(REGEX),
                    eq(File(BASE_DIR, FILE.name)),
                    eq(OUTPUT_FILE),
                    eq(REGEX_PATTERN_FLAGS),
                    eq(utf8)
                )
            verify(FileUtils).readFile(File(TOKEN_FILE), utf8)
            verify(FileUtils).readFile(File(VALUE_FILE), utf8)
            verify(summaryBuilder).add(File(BASE_DIR, FILE.name), OUTPUT_FILE, utf8, log)
            verify(summaryBuilder).print(log)
        }

        @Test
        fun shouldReplaceContentsInReplacementsInSameFileWhenNoOutputFile() {
            val replacement: Replacement = mock(Replacement::class.java)
            val replacements: List<Replacement> = mutableListOf(replacement)
            mojo.regexFlags = regexFlags
            mojo.regex = REGEX
            mojo.replacements = replacements
            mojo.file = FILE.name
            mojo.basedir = BASE_DIR
            mojo.execute()
            verify(ReplacementProcessor)
                .replace(
                    replacements,
                    REGEX,
                    File(BASE_DIR, FILE.name),
                    OUTPUT_FILE,
                    REGEX_PATTERN_FLAGS,
                    utf8
                )
            verify(summaryBuilder).add(File(BASE_DIR, FILE.name), OUTPUT_FILE, utf8, log)
            verify(summaryBuilder).print(log)
        }

        @Test
        fun shouldReplaceContentsWithVariableTokenValueMap() {
            val replacement: Replacement = mock(Replacement::class.java)
            val replacements: List<Replacement> = mutableListOf(replacement)
            `when`(
                    TokenValueMapFactory.replacementsForVariable(
                        TOKEN_VALUE_MAP,
                        commentsEnabled = true,
                        unescape = false,
                        encoding = utf8
                    )
                )
                .thenReturn(replacements)
            mojo.variableTokenValueMap = TOKEN_VALUE_MAP
            mojo.file = FILE.name
            mojo.basedir = BASE_DIR
            mojo.encoding = ENCODING
            mojo.execute()
            assertThat(mojo.variableTokenValueMap, equalTo(TOKEN_VALUE_MAP))
            verify(ReplacementProcessor)
                .replace(replacements, true, File(BASE_DIR, FILE.name), OUTPUT_FILE, 0, utf8)
            verify(summaryBuilder).add(File(BASE_DIR, FILE.name), OUTPUT_FILE, utf8, log)
            verify(summaryBuilder).print(log)
        }

        @Test
        fun shouldNotReplaceIfIgnoringMissingFilesAndFileNotExists() {
            `when`(File(BASE_DIR, FILE.name).exists()).thenReturn(true)
            mojo.file = FILE.name
            mojo.basedir = BASE_DIR
            mojo.ignoreMissingFile = true
            mojo.execute()
            assertTrue(logger.info().isNotEmpty())
            fail(
                "verify(summaryBuilder, never()).add(anyString(), anyString(), anyString(), isA(Log::class.java))"
            )
            verify(summaryBuilder).print(log)
        }

        @Test
        fun shouldThrowExceptionWhenUsingIgnoreMissingFilesAndNoFileSpecified() {
            mojo.ignoreMissingFile = true
            try {
                mojo.execute()
                fail("Should have thrown exception")
            } catch (e: MojoExecutionException) {
                logger.error("<ignoreMissingFile> only usable with <file>")
                fail(
                    "verify(summaryBuilder, never()).add(anyString(), anyString(), anyString(), isA(Log::class.java))"
                )
                verify(summaryBuilder).print(log)
            }
        }

        @Test
        fun shouldRethrowIOExceptionsAsMojoExceptions() {
            assertThrows(MojoExecutionException::class.java) {
                mojo.regexFlags = regexFlags
                mojo.regex = REGEX
                mojo.file = "/../../.."
                mojo.tokenFile = TOKEN_FILE
                mojo.valueFile = VALUE_FILE
                mojo.outputFile = ".././/.."
                mojo.basedir = BASE_DIR
                mojo.execute()
            }
        }

        @Test
        fun shouldNotThrowExceptionWhenIgnoringErrors() {
            mojo.isIgnoreErrors = true
            mojo.file = "....//"
            mojo.tokenFile = TOKEN_FILE
            mojo.valueFile = VALUE_FILE
            mojo.outputFile = "....//"
            mojo.execute()
        }

        companion object {
            private const val ENCODING = "encoding"
            private const val REGEX_FLAG = "regex flag"
            private val FILE = File("file")
            private const val REGEX = true
            private val OUTPUT_FILE = File("output file")
            private const val REGEX_PATTERN_FLAGS = 999
            private const val BASE_DIR = "base dir"
            private const val TOKEN_VALUE_MAP = "token value map"
            private const val TOKEN_FILE = "token file"
            private const val VALUE_FILE = "value file"
            private const val TOKEN = "token"
            private const val VALUE = "value"
            private val utf8 = Charset.forName("UTF-8")
        }

        @Suppress("UNCHECKED_CAST")
        private fun replacementOf(
            value: String,
            unescape: Boolean,
            vararg tokens: String
        ): BaseMatcher<List<Replacement>> {
            return object : BaseMatcher<List<Replacement>>() {
                override fun matches(arg0: Any): Boolean {
                    val replacements = arg0 as List<Replacement>
                    for (i in tokens.indices) {
                        val replacement = replacements[i]
                        val equalsBuilder = EqualsBuilder()
                        equalsBuilder.append(tokens[i], replacement.token)
                        equalsBuilder.append(value, replacement.value)
                        equalsBuilder.append(unescape, replacement.isUnescape)
                        val equals = equalsBuilder.isEquals
                        if (!equals) {
                            return false
                        }
                    }
                    return true
                }

                override fun describeTo(desc: Description) {
                    desc.appendText("tokens").appendValue(Arrays.asList(*tokens))
                    desc.appendText("value").appendValue(value)
                    desc.appendText("unescape").appendValue(unescape)
                }
            }
        }
    */
}
