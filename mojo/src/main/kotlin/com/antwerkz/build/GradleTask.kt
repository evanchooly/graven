package com.antwerkz.build

import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("task")
class GradleTask {
    lateinit var name: String

    var args = listOf<String>()
}
