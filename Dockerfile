#Dockerfile to builds loggers.
FROM eclipse-temurin:11.0.15_10-jdk as builder
#RUN apk --no-cache add curl
RUN apt update && apt install curl -y
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew build -x test
#RUN ./gradlew jibDockerBuild

FROM eclipse-temurin:11.0.15_10-jdk
COPY --from=builder /build/libs/torback-0.0.1-SNAPSHOT.jar /app.jar
#COPY build/libs/*.jar app.jar
ENV JAVA_OPTS=""
EXPOSE 9000
ENTRYPOINT exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9999 -jar /app.jar

