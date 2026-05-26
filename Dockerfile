FROM gradle:8.7-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN ./gradlew buildFatJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
EXPOSE 5055

RUN mkdir -p /app
RUN mkdir -p /app/keys

COPY --from=build /home/gradle/src/build/libs/*-all.jar /app/ktor-auth-api.jar

ENTRYPOINT ["java","-jar","/app/ktor-auth-api.jar"]