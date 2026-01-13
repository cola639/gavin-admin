# Use Java 17 runtime (matches pom.xml java.version=17)
FROM eclipse-temurin:17-jre-alpine

# Build arguments (provided by Jenkinsfile when building the image)
ARG PROFILE
# Build argument for the JAR file name
ARG JAR_FILE

# Convert ARG to ENV so they are available at container runtime
ENV PROFILE=${PROFILE}
ENV JAR_FILE=${JAR_FILE}

# Create a non-root user/group named "spring" to run the process for better security
RUN addgroup -S spring && adduser -S spring -G spring

# Install fonts required for generating captcha images
RUN apk add --no-cache ttf-dejavu

# Prevent monitoring/runtime errors by installing glibc (for some native dependencies)
# Add the glibc package repository key, download the glibc APK, then install it
RUN apk --no-cache add ca-certificates wget && \
    wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub && \
    wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.33-r0/glibc-2.33-r0.apk && \
    apk add glibc-2.33-r0.apk

# Install a libudev-dev alternative on Alpine (used by some native libraries)
RUN apk --no-cache add eudev-dev

# Create the logs directory required by the application.
# This should match logback.xml, e.g. <property name="log.path" value="./logs"/>
# Set ownership to the "spring" user and group.
RUN mkdir -p /logs && chown -R spring:spring /logs

# Switch to the non-root user
USER spring:spring

# Copy the built JAR into the container
COPY ${JAR_FILE} /app.jar

# Expose container ports (documentation only; actual publishing is done via `docker run -p ...`)
EXPOSE 80 443

# Default container startup command:
# - Enable container memory awareness
# - Limit JVM memory usage with MaxRAMPercentage
# - Improve entropy source for faster startup
# - Run the Spring Boot JAR and set the active profile
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -jar /app.jar --spring.profiles.active=${PROFILE}"]
