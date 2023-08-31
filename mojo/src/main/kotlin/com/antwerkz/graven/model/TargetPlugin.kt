package com.antwerkz.graven.model

data class TargetPlugin(
    val groupId: String,
    val artifactId: String,
    val gradleTask: String,
    val phase: String,
    val executionId: String? = null
)
