dependencies {
    // Module dependencies
    implementation(project(":domain"))
    implementation(project(":security"))
    implementation(project(":services"))

    // To use Spring MVC and the Servlet API
    implementation("org.springframework:spring-webmvc:6.1.14")

    // To use JWT
    implementation("com.auth0:java-jwt:4.5.0")

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
}
