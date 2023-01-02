buildscript {
    dependencies {
        classpath("org.apache.maven:maven-model:3.8.6")
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

