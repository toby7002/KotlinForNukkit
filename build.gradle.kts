import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.diffplug.spotless") version "6.13.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val kotlin_version: String by project
val maven_group: String by project

version = kotlin_version
group = maven_group

repositories {
    maven {
        setUrl("https://repo.opencollab.dev/maven-snapshots")
    }
    maven {
        setUrl("https://repo.opencollab.dev/maven-releases")
    }
    mavenCentral()
}

base { archivesName.set("kotlinfornukkit") }

dependencies {
    val coroutines_version: String by project
    val serialization_version: String by project

    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serialization_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")

    compileOnly("cn.nukkit:nukkit:1.0-SNAPSHOT")
}

tasks.processResources {
    filesMatching("**/plugin.yml") {
        expand(mapOf("version" to project.version))
    }
}

tasks.withType<JavaCompile> { options.encoding = "UTF-8" }

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.named("assemble") {
    dependsOn("shadowJar")
}


java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set(archiveBaseName.get())
    archiveClassifier.set("")
    archiveVersion.set(getVersion().toString())

    from("LICENSE") {
        rename { "${it}_${archiveBaseName.get()}" }
    }
}

spotless {
    java {
        target("src/*/java/**/*.java")

        endWithNewline()
        indentWithTabs()
        removeUnusedImports()
        palantirJavaFormat()
    }
}
