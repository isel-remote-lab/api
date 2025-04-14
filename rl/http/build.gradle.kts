plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "rl.isel.pt"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Module dependencies
    implementation(project(":domain"))
    implementation(project(":services"))

    // To use Spring MVC and the Servlet API
    implementation("org.springframework:spring-webmvc:6.1.14")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

    // To use Kotlin specific date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}