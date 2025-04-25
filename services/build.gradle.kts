dependencies {
    api(project(":domain"))
    implementation(project(":repository"))
    implementation(project(":security"))

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
}

tasks.test {
    useJUnitPlatform()
    if (System.getenv("DB_URL") == null) {
        environment("DB_URL", "jdbc:postgresql://localhost:5432/db?user=dbuser&password=changeit")
    }

    if (System.getenv("TEST_MODE") == null) {
        environment("TEST_MODE", "true")
    }
    dependsOn(":repository-jdbi:dbTestsWait")
    finalizedBy(":repository-jdbi:dbTestsDown")
}
