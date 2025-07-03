dependencies {
    api(project(":domain"))
    implementation(project(":repository"))

    // To get the DI annotation
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")

    // To use Kotlin specific date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    // To use the JDBI-based repository implementation on the tests
    testImplementation(project(":repository-jdbi"))
    testImplementation("org.jdbi:jdbi3-core:3.37.1")
    testImplementation("org.postgresql:postgresql:42.7.2")

    // For the domain configs
    testImplementation(project(":host"))
    testImplementation(kotlin("test"))

    // To use coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // Redis dependencies
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("io.lettuce:lettuce-core:6.3.2.RELEASE")
}

tasks.test {
    useJUnitPlatform()
    dependsOn(":repository-jdbi:dbTestsWait")
    finalizedBy(":repository-jdbi:dbTestsDown")
}
