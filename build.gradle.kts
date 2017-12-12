import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.2.0"
    `maven-publish`
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
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
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
    repositories {
        maven(url = "https://repo.araqnid.org/maven/") {
            credentials {
                username = "repo-user"
                password = "repo-password"
            }
        }
    }
    (publications) {
        "mavenJava"(MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar)
        }
    }
}
