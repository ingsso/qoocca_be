# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Copy build scripts first to maximize Docker layer cache hits.
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY qoocca-common/build.gradle qoocca-common/build.gradle
COPY qoocca-db/build.gradle qoocca-db/build.gradle
COPY qoocca-auth/build.gradle qoocca-auth/build.gradle
COPY qoocca-api/build.gradle qoocca-api/build.gradle

# Warm up Gradle dependencies cache.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :qoocca-api:dependencies -x test --no-daemon

# Copy source code after dependency layer.
COPY qoocca-common ./qoocca-common
COPY qoocca-db ./qoocca-db
COPY qoocca-auth ./qoocca-auth
COPY qoocca-api ./qoocca-api
COPY src ./src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :qoocca-api:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/qoocca-api/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
