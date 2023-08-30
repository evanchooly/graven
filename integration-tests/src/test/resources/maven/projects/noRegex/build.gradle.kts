import jdk.tools.jlink.resources.plugins

buildscript {
    dependencies {
        classpath("org.apache.maven:maven-model:2.3.1")
        classpath("com.fasterxml.jackson.core:jackson-databind:2.14.1")
    }
}
repositories {
    mavenLocal()
    mavenCentral()
}

plugins {
    id("com.gradle.plugin-publish") version "0.13.0"
    kotlin("jvm") version "1.6.0"
    `java-gradle-plugin`
}

dependencies {
    implementation("com.antwerkz.build:maven:${project.version}")
}

