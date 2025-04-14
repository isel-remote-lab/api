plugins {
    kotlin("jvm")
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
    implementation(project(":host"))
    implementation(project(":http"))
    implementation(project(":repository"))
    implementation(project(":repository-jdbi"))
    implementation(project(":services"))

    // JDBI
    implementation("org.jdbi:jdbi3-core:3.37.1")
    implementation("org.jdbi:jdbi3-kotlin:3.37.1")
    implementation("org.jdbi:jdbi3-postgres:3.37.1")
    implementation("org.postgresql:postgresql:42.7.2")

    // Kotlin specific date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    // WebTestClient and Spring on tests
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
    if (System.getenv("DB_URL") == null) {
        environment("DB_URL", "jdbc:postgresql://localhost:5432/db?user=dbuser&password=changeit")
    }
    dependsOn(":tests:dbTestsWait")
    finalizedBy(":tests:dbTestsDown")
}

/**
 * DB related tasks
 * - To run `psql` inside the container, do
 *      docker exec -ti db-tests psql -d db -U dbuser -W
 *   and provide it with the same password as define on `docker/Dockerfile-db-tests`
 */

val composeFileDir: Directory by parent!!.extra
val dockerComposePath = composeFileDir.file("docker-compose.yml").toString()

tasks.register<Exec>("dbTestsUp", fun Exec.() {
    commandLine("docker", "compose", "-f", dockerComposePath, "up", "-d", "--build", "--force-recreate", "db-tests")
})

tasks.register<Exec>("dbTestsWait", fun Exec.() {
    commandLine("docker", "exec", "db-tests", "/app/bin/wait-for-postgres.sh", "localhost")
    dependsOn("dbTestsUp")
})

tasks.register<Exec>("dbTestsDown", fun Exec.() {
    commandLine("docker", "compose", "-f", dockerComposePath, "down", "db-tests")
})