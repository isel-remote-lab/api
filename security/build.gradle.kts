dependencies {
    implementation(project(":domain"))

    // To use JWT
    implementation("com.auth0:java-jwt:4.5.0")

    // To get the Spring DI annotation
    api("org.springframework:spring-context:6.2.4")
}
