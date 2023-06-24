buildscript {
    dependencies {
        implementation("org.apache.maven:maven-model:2.3.1")
        classpath("com.fasterxml.jackson.core:jackson-databind:1.0.0")
    }
}

plugins {
    id("dev.morphia.critter") version "${findProperty("morphia.version")}"
    kotlin("jvm") version "1.6.0"
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    implementation("dev.morphia.morphia:morphia-core:${findProperty("morphia.version")}")
    testImplementation("org.testng:testng:${findProperty("testng.version")}")
    testImplementation("org.testcontainers:mongodb:${findProperty("testcontainers.version")}")
}

tasks {
    test {
        useTestNG()
    }

    critter {
    }
}

tasks.withType(JavaCompile::class.java) {
    options.compilerArgs = listOf("-parameters")
}
