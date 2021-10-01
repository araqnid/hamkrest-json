import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    kotlin("jvm") version "1.4.30"
    `maven-publish`
    signing
}

group = "org.araqnid.hamkrest"
version = "1.1.2"

description = "JSON matchers for Hamkrest"

repositories {
    mavenCentral()
}

dependencies {
    api("com.natpryce:hamkrest:1.7.0.0")
    api("com.fasterxml.jackson.core:jackson-databind")
    api(platform("com.fasterxml.jackson:jackson-bom:2.13.0"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test-junit"))
}

java(Action {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
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
            pom {
                name.set(project.name)
                description.set(project.description)
                licenses {
                    license {
                        name.set("Apache")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                url.set("https://github.com/araqnid/hamkrest-json")
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/araqnid/hamkrest-json/issues")
                }
                scm {
                    connection.set("https://github.com/araqnid/hamkrest-json.git")
                    url.set("https://github.com/araqnid/hamkrest-json")
                }
                developers {
                    developer {
                        name.set("Steven Haslam")
                        email.set("araqnid@gmail.com")
                    }
                }
            }
        }
    }

    repositories {
        val sonatypeUser: String? by project
        if (sonatypeUser != null) {
            maven {
                name = "OSSRH"
                url = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val sonatypePassword: String by project
                credentials {
                    username = sonatypeUser
                    password = sonatypePassword
                }
            }
        }
    }
})

signing(Action {
    useGpgCmd()
    sign(publishing.publications)
})
