plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "rl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Module dependencies
    implementation(project(":rl:domain"))
    implementation(project(":rl:http"))
    implementation(project(":rl:services"))
    implementation(project(":rl:repository-jdbi"))
    implementation(project(":rl:repository"))

    // dotenv
    //implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

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

kotlin {
    jvmToolchain(21)
}
