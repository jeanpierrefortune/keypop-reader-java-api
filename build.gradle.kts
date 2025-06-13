///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
plugins {
    java
    `maven-publish`
    signing
    id("com.diffplug.spotless") version "6.25.0"
}
buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

///////////////////////////////////////////////////////////////////////////////
//  APP CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
repositories {
    mavenLocal()
    mavenCentral()
}
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.25.3")
}

val javaSourceLevel: String by project
val javaTargetLevel: String by project
java {
    sourceCompatibility = JavaVersion.toVersion(javaSourceLevel)
    targetCompatibility = JavaVersion.toVersion(javaTargetLevel)
    println("Compiling Java $sourceCompatibility to Java $targetCompatibility.")
    withJavadocJar()
    withSourcesJar()
}

///////////////////////////////////////////////////////////////////////////////
//  TASKS CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
tasks {
    spotless {
        java {
            target("src/**/*.java")
            licenseHeaderFile("${project.rootDir}/LICENSE_HEADER")
            importOrder("java", "javax", "org", "com", "")
            removeUnusedImports()
            googleJavaFormat()
        }
    }
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set(project.findProperty("title") as String? ?: project.name)
                description.set(project.findProperty("description") as String?)
                url.set(project.findProperty("project.url") as String?)
                licenses {
                    license {
                        name.set(project.findProperty("license.name") as String?)
                        url.set(project.findProperty("license.url") as String?)
                        distribution.set(project.findProperty("license.distribution") as String?)
                    }
                }
                developers {
                    developer {
                        name.set(project.findProperty("developer.name") as String?)
                        email.set(project.findProperty("developer.email") as String?)
                    }
                }
                organization {
                    name.set(project.findProperty("organization.name") as String?)
                    url.set(project.findProperty("organization.url") as String?)
                }
                scm {
                    connection.set(project.findProperty("scm.connection") as String?)
                    developerConnection.set(project.findProperty("scm.developerConnection") as String?)
                    url.set(project.findProperty("scm.url") as String?)
                }
                ciManagement {
                    system.set(project.findProperty("ci.system") as String?)
                    url.set(project.findProperty("ci.url") as String?)
                }
            }
        }
    }
    repositories {
        maven {
            if (project.hasProperty("sonatypeURL")) {
                url = uri(project.property("sonatypeURL") as String)
                credentials {
                    username = project.property("sonatypeUsername") as String
                    password = project.property("sonatypePassword") as String
                }
            }
        }
    }
}

signing {
    if (project.hasProperty("RELEASE")) {
        useGpgCmd()
        sign(publishing.publications["mavenJava"])
    }
}