buildscript {
    dependencies {
        classpath("org.apache.maven:maven-model:2.3.1")
        classpath("com.fasterxml.jackson.core:jackson-databind:1.0.0")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

plugins {
    kotlin("jvm") version "${findProperty("kotlin.version")}"
    `java-gradle-plugin`
}

dependencies {
    implementation("dev.morphia.morphia:morphia-core:1.2.3")
    testImplementation("org.testng:testng:${findProperty("testng.version")}")
    testImplementation("org.testcontainers:mongodb:${findProperty("testcontainers.version")}")
}

tasks {
    test {
        useTestNG()
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType(JavaCompile::class.java) {
    options.compilerArgs = listOf("-parameters")
}
