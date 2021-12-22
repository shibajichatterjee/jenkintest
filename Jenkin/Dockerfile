FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/jenkinsdocker.jar
COPY ${JAR_FILE} jenkinsdocker.jar
ENTRYPOINT["java","-jar","/jenkinsdocker.jar"]