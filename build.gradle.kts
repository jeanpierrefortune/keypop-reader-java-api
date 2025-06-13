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
    withJavadocJar()
    withSourcesJar()
}

///////////////////////////////////////////////////////////////////////////////
//  TASKS CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
tasks {
    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }
    val javadocJar by creating(Jar::class) {
        archiveClassifier.set("javadoc")
        from(tasks.javadoc.get())
    }

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
            artifact(tasks.getByName("sourcesJar"))
            artifact(tasks.getByName("javadocJar"))

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
