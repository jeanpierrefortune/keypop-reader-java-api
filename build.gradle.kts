///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////

// The 'maven-publish' and 'signing' plugins are required for publishing and signing artifacts.
plugins {
    java
    `maven-publish`
    signing
    id("com.diffplug.spotless") version "6.25.0"
}

// The buildscript block is a legacy way to apply plugins, but it's kept here
// as it was in the original file for the custom 'keypop' plugin.
buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("org.eclipse.keypop:keypop-gradle:0.1.+") { isChanging = true }
    }
}
apply(plugin = "org.eclipse.keypop")

///////////////////////////////////////////////////////////////////////////////
//  APP CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // Corrected to a valid JUnit BOM version.
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
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
    // These two lines automatically configure the sources and Javadoc JARs for publishing.
    withJavadocJar()
    withSourcesJar()
}

///////////////////////////////////////////////////////////////////////////////
//  TASKS CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
tasks {
    // Configure the existing 'test' task provided by the Java plugin
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    // Configure spotless via its extension
    spotless {
        java {
            target("src/**/*.java")
            licenseHeaderFile("${project.rootDir}/LICENSE_HEADER")
            importOrder("java", "javax", "org", "com", "")
            removeUnusedImports()
            googleJavaFormat()
        }
    }
}

///////////////////////////////////////////////////////////////////////////////
//  PUBLISHING CONFIGURATION
//  This block must be at the top level, not inside 'tasks'.
///////////////////////////////////////////////////////////////////////////////
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            // 'from(components["java"])' now automatically includes the main, sources, and javadoc jars
            // because of the 'withSourcesJar()' and 'withJavadocJar()' configuration above.
            from(components["java"])

            pom {
                name.set("Keypop Gradle Plugin")
                description.set("Gradle Plugin that regroups common tasks used by all Keypop Projects.")
                url.set("https://projects.eclipse.org/projects/iot.keypop")
                organization {
                    name.set("Eclipse Keypop")
                    url.set("https://projects.eclipse.org/projects/iot.keypop")
                }
                licenses {
                    license {
                        name.set("Eclipse Public License - v 2.0")
                        url.set("https://www.eclipse.org/legal/epl-2.0/")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        name.set("Andrei Cristea")
                        email.set("andrei.cristea019@gmail.com")
                    }
                    developer {
                        name.set("Jean-Pierre Fortune")
                        email.set("jean-pierre.fortune@ialto.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/eclipse-keypop/keypop-ops.git")
                    developerConnection.set("scm:git:https://github.com/eclipse-keypop/keypop-ops.git")
                    url.set("http://github.com/eclipse-keypop/keypop-ops/tree/main")
                }
            }
        }
    }
    repositories {
        maven {
            // It's safer to check if properties exist before using them.
            // These properties must be provided via gradle.properties or command line.
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

// The signing block should also be at the top level.
signing {
    // Only sign if the 'RELEASE' property is set, which is a good practice.
    if (project.hasProperty("RELEASE")) {
        useGpgCmd()
        sign(publishing.publications["mavenJava"])
    }
}
