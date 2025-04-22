import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.springframework.boot.gradle.dsl.SpringBootExtension
import org.springframework.boot.gradle.tasks.run.BootRun

repositories {
    mavenCentral()
}

// Declared plugins with versions; 'apply false' means they are not applied to the root project
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" // Ktlint for code formatting
    id("org.springframework.boot") version "3.3.3" apply false // Spring Boot plugin
    id("io.spring.dependency-management") version "1.1.6" apply false // Dependency management
    kotlin("jvm") version "2.0.10" // Kotlin JVM plugin
    kotlin("plugin.spring") version "1.9.25" apply false // Kotlin Spring plugin
}

tasks.test {
    useJUnitPlatform()
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
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

        testImplementation(kotlin("test"))
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

/**
 * Docker related tasks
 */
task<Copy>("extractUberJar") {
    dependsOn("assemble")
    // opens the JAR containing everything...
    from(zipTree(layout.buildDirectory.file("libs/host-$version.jar").get().toString()))
    // ... into the 'build/dependency' folder
    into("build/dependency")
}

/**
 * Docker images names
 */
data class DockerImages(
    val dbTests: String,
    val postgresDev: String,
    val postgresProd: String,
    val jvm: String,
    val nodeDev: String,
    val nodeProd: String,
    // val nginx: String,
)

val dockerImages =
    DockerImages(
        dbTests = "db-tests",
        postgresDev = "rl-postgres-dev",
        postgresProd = "rl-postgres-prod",
        jvm = "rl-jvm",
        nodeDev = "rl-node-dev",
        nodeProd = "rl-node-prod",
        // nginx = "rl-nginx",
    )

object DockerFiles {
    private const val DOCKER_FOLDER = "docker"
    private const val DOCKER_DB_FOLDER = "../repository-jdbi/docker"
    const val DOCKER_COMPOSE = "docker-compose.yml"

    const val JVM = "$DOCKER_FOLDER/Dockerfile-jvm"
    const val DB_TESTS = "$DOCKER_DB_FOLDER/Dockerfile-db-tests"
    const val POSTGRES_DEV = "$DOCKER_FOLDER/Dockerfile-postgres"
    const val POSTGRES_PROD = "$DOCKER_FOLDER/Dockerfile-postgres"
    const val NODE_DEV = "$DOCKER_FOLDER/Dockerfile-node-dev"
    const val NODE_PROD = "$DOCKER_FOLDER/Dockerfile-node-prod"
}

/**
 * Build tasks
 */
task<Exec>("buildImageJvm") {
    dependsOn("extractUberJar")
    commandLine("docker", "build", "-t", dockerImages.jvm, "-f", DockerFiles.JVM, ".")
}

task<Exec>("buildImageDbTests") {
    commandLine(
        "docker",
        "build",
        "-t",
        dockerImages.dbTests,
        "-f",
        DockerFiles.DB_TESTS,
        "../repository-jdbi",
    )
}

task<Exec>("buildImagePostgresDev") {
    commandLine(
        "docker",
        "build",
        "-t",
        dockerImages.postgresDev,
        "-f",
        DockerFiles.POSTGRES_DEV,
        "../repository-jdbi",
    )
}

task<Exec>("buildImagePostgresProd") {
    commandLine(
        "docker",
        "build",
        "-t",
        dockerImages.postgresProd,
        "-f",
        DockerFiles.POSTGRES_PROD,
        "../repository-jdbi",
    )
}

task<Exec>("buildImageNodeDev") {
    commandLine(
        "docker",
        "build",
        "-t",
        dockerImages.nodeDev,
        "-f",
        DockerFiles.NODE_DEV,
        "../../../js",
    )
}

task<Exec>("buildImageNodeProd") {
    commandLine(
        "docker",
        "build",
        "-t",
        dockerImages.nodeProd,
        "-f",
        DockerFiles.NODE_PROD,
        "../../../js",
    )
}

/*
task<Exec>("buildImageNginx") {
    commandLine(
        "docker",
        "build",
        "-t",
        dockerImages.nginx,
        "-f",
        "docker/Dockerfile-nginx",
        "../../../",
    )
    dependsOn("webpackProd")
}

 */

task("buildImageAllDev") {
    dependsOn("buildImageJvm")
    dependsOn("buildImagePostgresDev")
    dependsOn("buildImageNodeDev")
    // dependsOn("buildImageNginx")
}

task("buildBackendImageAll") {
    dependsOn("buildImageJvm")
    dependsOn("buildImagePostgresDev")
}

/**
 * Webpack tasks
 */
task<Exec>("webpackProd") {
    workingDir = file("../../../js")
    commandLine("npm", "run", "build")
}

/**
 * Docker compose tasks
 */
task<Exec>("allUp") {
    dependsOn("buildImageAll")
    commandLine("docker", "compose", "up", "-d")
    finalizedBy("ngrokUp")
}

task<Exec>("allDown") {
    commandLine("docker", "compose", "down")
    finalizedBy("ngrokDown")
}

task<Exec>("backendAllUp") {
    dependsOn("buildBackendImageAll")
    commandLine("docker", "compose", "up", "-d")
    finalizedBy("ngrokUp")
}

task<Exec>("backendAllDown") {
    commandLine("docker", "compose", "down")
    finalizedBy("ngrokDown")
}

task<Exec>("dbTestsUp") {
    dependsOn("buildImageDbTests")
    workingDir = file("../")
    commandLine("docker", "compose", "up", "-d")
}

task<Exec>("dbTestsDown") {
    commandLine("docker", "compose", "down", dockerImages.dbTests)
}

/**
 * Ngrok tasks
 */
task<Exec>("ngrokUp") {
    commandLine(
        "docker", "run", "-d", "--net=host", "-e",
        "NGROK_AUTHTOKEN=2qV7kqaFqYEKQCDmV939PgrdsT3_2Mq2EbxAvtRHc8L28DM9w", "ngrok/ngrok:latest", "http",
        "--url=awaited-louse-elegant.ngrok-free.app", "8080",
    )
}

task<Exec>("ngrokDown") {
    commandLine("ngrok", "kill")
}

/**
 * Test tasks
 */
task<Exec>("testBackend") {
    dependsOn("dbTestsUp")
    dependsOn("buildImageJvm")
    commandLine("docker", "compose", "exec", dockerImages.jvm, "test")
    finalizedBy("dbTestsDown")
}

/**
 * Logs tasks
 */
task<Exec>("showLogs") {
    commandLine("docker", "compose", "logs", "-f")
}
