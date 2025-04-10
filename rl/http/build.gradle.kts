plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

group = "rl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Module dependencies
    implementation(project(":rl:domain"))
    implementation(project(":rl:services"))

    // Spring dependencies
    implementation("org.springframework.boot:spring-boot-starter-web")

    // SLF4J
    implementation("org.slf4j:slf4j-api:2.0.16")

    // Kotlin specific date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
}

kotlin {
    jvmToolchain(21)
}