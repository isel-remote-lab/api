import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.springframework.boot.gradle.dsl.SpringBootExtension
import org.springframework.boot.gradle.tasks.run.BootRun

buildscript {
    repositories {
        mavenCentral()
    }
}

// Declare plugins with versions; 'apply false' means they are not applied to the root project
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" // Ktlint for code formatting
    id("org.springframework.boot") version "3.3.3" apply false // Spring Boot plugin
    id("io.spring.dependency-management") version "1.1.6" apply false // Dependency management
    kotlin("jvm") version "2.0.10" apply false // Kotlin JVM plugin
    kotlin("plugin.spring") version "1.9.25" apply false // Kotlin Spring plugin
}

// Define a custom task to bring down Docker Compose services
task<Exec>("composeDown") {
    commandLine("docker", "compose", "down")
}

// Set an extra property for the compose file directory
extra["composeFileDir"] = layout.projectDirectory

// Configure all subprojects
subprojects {
    group = "isel.rl.core"
    version = "1.0-SNAPSHOT"

    // Apply necessary plugins to subprojects
    apply {
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
    }

    // Define common dependencies for all subprojects
    dependencies {
        // Kotlinx datetime library
        add("implementation", "org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

        add("testImplementation", (kotlin("test")))
    }

    // Configure test tasks to use JUnit Platform
    tasks.withType<Test> {
        useJUnitPlatform()
    }

    // Set the Kotlin JVM toolchain to use Java 21
    kotlinExtension.jvmToolchain(21)

    // Define repositories for dependency resolution
    repositories {
        mavenCentral()
    }

    // Configure dependency management to import Spring Boot BOM
    the<DependencyManagementExtension>().apply {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    // Set the main class for Spring Boot application
    configure<SpringBootExtension> {
        mainClass.set("isel.rl.host.RemoteLabAppKt")
    }

    // Disable the bootRun task by default
    tasks.named<BootRun>("bootRun") {
        enabled = false
    }
}

// Configure the 'host' project specifically
project(":host") {
    tasks.named<BootRun>("bootRun") {
        // Set the SPRING_PROFILES_ACTIVE environment variable to 'local' if not already set
        environment["SPRING_PROFILES_ACTIVE"] = environment["SPRING_PROFILES_ACTIVE"] ?: "local"
        // Set the working directory to the root project directory
        workingDir = rootProject.projectDir
        // Enable the bootRun task for the 'host' project
        enabled = true
    }
}
