package com.antwerkz.graven.model

import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("configuration")
data class GravenConfig(
    var gradleVersion: String = "",
    //    var tasks: List<GradleTask> = listOf(),
    var replacements: List<RegexReplacement> = listOf()
)
