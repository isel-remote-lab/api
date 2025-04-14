plugins {
    kotlin("jvm")
}

group = "rl.isel.pt"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    // To get dependency annotations
    api("org.springframework:spring-context:6.2.4")
}

kotlin {
    jvmToolchain(21)
}