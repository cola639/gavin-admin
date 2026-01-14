# Use Java 17 runtime (matches pom.xml java.version=17)
FROM eclipse-temurin:17-jre-jammy

# Build arguments (provided by Jenkinsfile when building the image)
ARG PROFILE=prod
# Build argument for the JAR file path (relative to build context)
ARG JAR_FILE=api-boot/target/api-server.jar

# Convert ARG to ENV so they are available at container runtime
ENV PROFILE=${PROFILE}
ENV JAR_FILE=${JAR_FILE}

# Create a non-root user/group named "spring" to run the process for better security
RUN addgroup -S spring && adduser -S spring -G spring

# Install fonts and libudev for native libraries (e.g., OSHI)
RUN apt-get update && \
    apt-get install -y --no-install-recommends fonts-dejavu-core fontconfig libudev1 && \
    rm -rf /var/lib/apt/lists/*

# Create the logs directory required by the application.
# This should match logback.xml, e.g. <property name="log.path" value="./logs"/>
# Set ownership to the "spring" user and group.
RUN mkdir -p /logs && chown -R spring:spring /logs

# Switch to the non-root user
USER spring:spring

# Copy the built JAR into the container
COPY ${JAR_FILE} /admin-server.jar

# Expose container ports (documentation only; actual publishing is done via `docker run -p ...`)
EXPOSE 80 443

# Default container startup command:
# - Enable container memory awareness
# - Limit JVM memory usage with MaxRAMPercentage
# - Improve entropy source for faster startup
# - Run the Spring Boot JAR and set the active profile
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -jar /admin-server.jar --spring.profiles.active=${PROFILE}"]
