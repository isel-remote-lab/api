plugins {
    kotlin("plugin.serialization") version "2.1.20"
}

dependencies {
    // To get the Spring DI annotation
    api("org.springframework:spring-context:6.2.4")

    // To use Spring Boot Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
}
