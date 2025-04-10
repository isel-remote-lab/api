plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    kotlin("jvm") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    id("org.springframework.boot") version "3.4.3" apply false
    id("io.spring.dependency-management") version "1.1.7"  apply false
}

include("rl")
include("rl:host")
include("rl:domain")
include("rl:http")
include("rl:services")
include("rl:repository")
include("rl:repository-jdbi")
include("rl:tests")

