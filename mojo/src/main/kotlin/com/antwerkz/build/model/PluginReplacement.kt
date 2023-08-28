package com.antwerkz.build.model

data class PluginReplacement(
    val groupId: String,
    val artifactId: String,
    val gradleTask: String,
    val phase: String,
    val executionId: String? = null
)