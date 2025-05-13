import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.springframework.boot.gradle.dsl.SpringBootExtension
import org.springframework.boot.gradle.tasks.run.BootRun

repositories {
    mavenCentral()
}

// Declared plugins with versions; 'apply false' means they are not applied to the root project
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false // Ktlint for code formatting
    id("org.springframework.boot") version "3.3.3" apply false // Spring Boot plugin
    id("io.spring.dependency-management") version "1.1.6" apply false // Dependency management
    kotlin("jvm") version "2.0.10" // Kotlin JVM plugin
    kotlin("plugin.spring") version "1.9.25" apply false // Kotlin Spring plugin
}

group = "isel.rl.core"
version = "1.0-SNAPSHOT"

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
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
    }

    // Define common dependencies for all subprojects
    dependencies {
        // Kotlinx datetime library
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
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
        mainClass.set("isel.rl.core.host.RemoteLabAppKt")
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

    /**
     * Docker related task
     */
    task<Copy>("extractUberJar") {
        dependsOn("assemble")
        // opens the JAR containing everything...
        from(zipTree(layout.buildDirectory.file("libs/host-$version.jar").get().toString()))
        // ... into the 'build/dependency' folder
        into("build/dependency")
    }
}

/**
 * Docker images names
 */
data class DockerImages(
    val api: String,
)

val dockerImages =
    DockerImages(
        api = "rl-api",
    )

object DockerFiles {
    private const val DOCKER_FOLDER = "docker"

    const val JVM = "$DOCKER_FOLDER/Dockerfile"
}

/**
 * Build tasks
 */
task<Exec>("buildImageJvm") {
    dependsOn(":host:extractUberJar")
    commandLine("docker", "build", "-t", dockerImages.api, "-f", DockerFiles.API, ".")
}

/**
 * Logs tasks
 */
task<Exec>("showLogs") {
    commandLine("docker", "compose", "logs", "-f")
}
