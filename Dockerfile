# Build stage - Using Oracle JDK 25
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Install Gradle 9.x manually
RUN apk add --no-cache wget unzip && \
    wget https://services.gradle.org/distributions/gradle-9.2.1-bin.zip && \
    unzip gradle-9.2.1-bin.zip && \
    mv gradle-9.2.1 /opt/gradle && \
    rm gradle-9.2.1-bin.zip

ENV GRADLE_HOME=/opt/gradle
ENV PATH="${GRADLE_HOME}/bin:${PATH}"

# Copy project files and build
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build --no-daemon -x test

# Run stage - Using Eclipse Temurin JRE 25
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
