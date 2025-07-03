dependencies {
    // Module dependencies
    api(project(":domain"))

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("io.lettuce:lettuce-core:6.3.2.RELEASE")
}
