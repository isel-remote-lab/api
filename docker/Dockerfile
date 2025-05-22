# Stage 1: Build
FROM gradle:8.7-jdk21 AS builder

WORKDIR /app

# Copy Gradle config files first
COPY build.gradle.kts settings.gradle /app/
COPY gradle /app/gradle

# Download the dependencies (uses cache if nothing has changed)
RUN gradle dependencies --no-daemon --info

# Copy the rest of the code
COPY . /app

# Build the Uber JAR (host module)
RUN gradle :host:extractUberJar --no-daemon --info

# Stage 2: Runner
FROM openjdk:21-jdk-slim AS runner

ARG DEPENDENCY=host/build/dependency

WORKDIR /app

# First layer: external libs (change the least)
COPY --from=builder /app/${DEPENDENCY}/BOOT-INF/lib /app/lib

# Second layer: META-INF contents
COPY --from=builder /app/${DEPENDENCY}/META-INF /app/META-INF

# Last layer: application classes (change the most)
COPY --from=builder /app/${DEPENDENCY}/BOOT-INF/classes /app

# Entrypoint
ENTRYPOINT ["java", "-cp", ".:lib/*", "isel.rl.core.host.RemoteLabAppKt"]