dependencies {
    // Module dependencies
    implementation(project(":domain"))
    implementation(project(":services"))

    // Spring dependencies
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
}
