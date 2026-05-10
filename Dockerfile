# Build stage
FROM gradle:8.5-jdk17-alpine AS build

WORKDIR /app

# Copy the build files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Copy the source code
COPY src ./src

# Build the application
RUN gradle bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jdk-alpine

# Install wget for health check
RUN apk add --no-cache wget

WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Set environment variables (these will be overridden by Digital Ocean App Platform)
ENV DATABASE_URL=jdbc:postgresql://localhost:5432/kitchen \
    DATABASE_USERNAME=postgres \
    DATABASE_PWD=postgres \
    ADMIN_NAME=admin \
    ADMIN_PWD=admin \
    NOTIFICATION_TOKEN=adminadminadmin \
    JAVA_OPTS="-Xms128m -Xmx768m \
    -XX:+UseContainerSupport \
    -XX:+UseG1GC \
    -XX:+UnlockExperimentalVMOptions \
    -XX:MaxGCPauseMillis=100 \
    -XX:G1HeapRegionSize=8m \
    -XX:G1NewSizePercent=20 \
    -XX:G1MaxNewSizePercent=40 \
    -XX:+ExitOnOutOfMemoryError \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp/heapdump.hprof \
    -XX:+UseStringDeduplication \
    -XX:StringDeduplicationAgeThreshold=3 \
    -XX:MaxMetaspaceSize=128m \
    -XX:CompressedClassSpaceSize=32m \
    -XX:ReservedCodeCacheSize=32m \
    -XX:InitialCodeCacheSize=16m \
    -Xss256k \
    -XX:+TieredCompilation \
    -XX:TieredStopAtLevel=1 \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    -Djava.awt.headless=true \
    -Dspring.backgroundpreinitializer.ignore=true"

# Run the application with optimized JVM settings for containers
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

# Digital Ocean App Platform Deployment Notes:
# 1. Make sure to set the following environment variables in the App Platform:
#    - DATABASE_URL: Your PostgreSQL database URL
#    - DATABASE_USERNAME: Database username
#    - DATABASE_PWD: Database password
#    - ADMIN_NAME: Admin username for the application
#    - ADMIN_PWD: Admin password for the application
#    - NOTIFICATION_TOKEN: Telegram bot token
# 2. The application runs on port 8080, make sure to set this as the HTTP port in App Platform
# 3. For production deployments, consider setting appropriate memory limits
