plugins {
    kotlin("plugin.serialization") version "2.1.20"
}

dependencies {
    // To get the Spring DI annotation
    api("org.springframework:spring-context:6.2.4")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    // To use JWT
    implementation("com.auth0:java-jwt:4.5.0")
}
