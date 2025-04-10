plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "rl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    // To use Spring Boot Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // To get the DI annotation
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
}

kotlin {
    jvmToolchain(21)
}