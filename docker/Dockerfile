# Stage 1: Build
FROM gradle:8.7-jdk21 AS builder

WORKDIR /app

# Copy all project files
COPY . .

# Build the Uber JAR (host module)
RUN gradle :host:extractUberJar --no-daemon

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