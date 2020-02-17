import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.61"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
}

val buildNumber: String? = System.getenv("BUILD_NUMBER")
val versionPrefix = "1.1"

group = "org.araqnid"

if (buildNumber != null)
    version = "${versionPrefix}.${buildNumber}"

repositories {
    jcenter()
}

dependencies {
    api("com.natpryce:hamkrest:${LibraryVersions.hamkrest}")
    api("com.fasterxml.jackson.core:jackson-databind:${LibraryVersions.jackson}")
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test-junit"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
        options.isIncremental = true
        options.isDeprecation = true
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    withType<Jar>().configureEach {
        manifest {
            attributes["Implementation-Title"] = project.description ?: project.name
            attributes["Implementation-Version"] = project.version
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
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
    if (project.version != Project.DEFAULT_VERSION) {
        pkg.version.name = project.version.toString()
        pkg.version.vcsTag = "v" + project.version
    }
}
