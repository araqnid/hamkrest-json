import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.2.0"
    `maven-publish`
    id("com.jfrog.bintray") version "1.7.3"
}

val gitVersion by extra {
    val capture = ByteArrayOutputStream()
    project.exec {
        commandLine("git", "describe", "--tags", "--always")
        standardOutput = capture
    }
    String(capture.toByteArray())
            .trim()
            .removePrefix("v")
            .replace('-', '.')
}

group = "org.araqnid"
version = gitVersion

val jacksonVersion by extra("2.9.2")
val hamkrestVersion by extra("1.4.2.2")

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("reflect", "1.2.0"))
    compile("com.natpryce:hamkrest:$hamkrestVersion")
    compile("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    testImplementation(kotlin("test-junit"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        sourceCompatibility = "1.8"
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
        options.isIncremental = true
        options.isDeprecation = true
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    "jar"(Jar::class) {
        manifest {
            attributes["Implementation-Title"] = project.description ?: project.name
            attributes["Implementation-Version"] = project.version
        }
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(java.sourceSets["main"].allSource)
}

publishing {
    (publications) {
        "mavenJava"(MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar)
        }
    }
}

bintray {
    user = (project.properties["bintray.user"] ?: "").toString()
    key = (project.properties["bintray.apiKey"] ?: "").toString()
    publish = true
    setPublications("mavenJava")
    pkg.repo = "maven"
    pkg.name = "hamkrest-json"
    pkg.setLicenses("Apache-2.0")
    pkg.vcsUrl = "https://github.com/araqnid/hamkrest-json"
    pkg.desc = "Hamkrest matchers for JSON"
    pkg.version.name = gitVersion
    if (!gitVersion.contains(".g")) {
        pkg.version.vcsTag = "v" + gitVersion
    }
}
