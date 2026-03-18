FROM gradle:8.8-jdk21 AS build
WORKDIR /workspace
COPY backend/build.gradle backend/build.gradle
COPY backend/src backend/src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

