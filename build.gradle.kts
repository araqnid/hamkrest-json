import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
}

group = "org.araqnid"
version = "1.1.1"

val jacksonVersion by extra("2.9.9")
val hamkrestVersion by extra("1.7.0.0")

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("reflect"))
    compile("com.natpryce:hamkrest:$hamkrestVersion")
    compile("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    testImplementation(kotlin("test-junit"))
}

tasks {
    withType<JavaCompile>().configureEach {
        sourceCompatibility = "1.8"
        sourceCompatibility = "1.8"
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

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
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
    pkg.version.name = project.version.toString()
    pkg.version.vcsTag = "v" + project.version
}
