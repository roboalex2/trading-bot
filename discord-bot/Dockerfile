#
# Build stage
#
FROM maven:3.9.7-eclipse-temurin-21-alpine AS build
ENV HOME=/home/app
WORKDIR $HOME
ADD pom.xml $HOME
RUN mvn dependency:go-offline --batch-mode
RUN mvn verify --fail-never --batch-mode
COPY src /home/app/src
RUN mvn clean package --batch-mode -DskipTests

#
# Package stage
#
FROM openjdk:21-slim-bullseye
COPY --from=build /home/app/target/discord-bot-1.0.0-SNAPSHOT.jar /usr/local/lib/master.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/master.jar"]