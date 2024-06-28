import groovy.json.JsonSlurper

plugins {
    id("java")
    id("com.diffplug.spotless") version "6.13.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val kotlin_version = file("generated/kotlin_version.txt").readText()
val artifact_group: String by project
val project_version: String by project
val project_id: String by project
val libVersions = JsonSlurper().parse(file("generated/library_versions.json")) as Map<*, *>
val kotlinLib = "org.jetbrains.kotlin:kotlin-stdlib"
val libraries = listOf(
    kotlinLib,
    "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk7",
    "org.jetbrains.kotlin:kotlin-reflect",

    "org.jetbrains.kotlinx:kotlinx-coroutines-core",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm",
    "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8",
    "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm",
    "org.jetbrains.kotlinx:kotlinx-serialization-json-jvm",
    "org.jetbrains.kotlinx:kotlinx-serialization-cbor-jvm",
    "org.jetbrains.kotlinx:atomicfu-jvm",
    "org.jetbrains.kotlinx:kotlinx-datetime-jvm"
)

version = "$project_version+kotlin.$kotlin_version"
group = artifact_group

repositories {
    maven("https://repo.opencollab.dev/maven-snapshots")
    maven("https://repo.opencollab.dev/maven-releases")

    mavenCentral()
}

base {
    archivesName = project_id
}

dependencies {
    val api_version: String by project

    libraries.forEach {
        implementation("$it:${libVersions[it]}")
    }

    compileOnly("cn.nukkit:nukkit:$api_version")
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set(archiveBaseName.get())
        archiveVersion.set(version.toString())
    }
    build { dependsOn(shadowJar) }

    processResources {
        val project_name: String by project
        val project_license: String by project
        val project_author: String by project
        val project_description: String by project

        filesMatching(listOf("plugin.yml")) {
            expand(
                mapOf(
                    "project_id" to project_id,
                    "project_name" to project_name,
                    "project_license" to project_license,
                    "project_version" to version,
                    "project_author" to project_author,
                    "project_description" to project_description,
                    "artifact_group" to artifact_group
                )
            )
        }
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}