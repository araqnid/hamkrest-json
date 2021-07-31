import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    kotlin("jvm") version "1.3.61"
    `maven-publish`
}

group = "org.araqnid"
version = "1.1.1"

repositories {
    mavenCentral()
}

dependencies {
    api("com.natpryce:hamkrest:1.7.0.0")
    api("com.fasterxml.jackson.core:jackson-databind")
    api(platform("com.fasterxml.jackson:jackson-bom:2.10.2"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test-junit"))
}

java(Action {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
})

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

publishing(Action {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            url = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = ""
                password = ""
            }
        }
    }
})
