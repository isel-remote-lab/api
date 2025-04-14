plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "rl.isel.pt"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Module dependencies
    implementation(project(":domain"))
    implementation(project(":http"))
    implementation(project(":services"))
    implementation(project(":repository-jdbi"))
    implementation(project(":repository"))

    // Spring dependencies
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // To get the DI annotation
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")

    // for JDBI and Postgres
    implementation("org.jdbi:jdbi3-core:3.37.1")
    implementation("org.postgresql:postgresql:42.7.2")

    // Kotlin date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}