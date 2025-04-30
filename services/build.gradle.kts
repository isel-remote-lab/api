dependencies {
    api(project(":domain"))
    implementation(project(":repository"))
    implementation(project(":security"))

    // To get the DI annotation
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")

    // To use Kotlin specific date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
}
