FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/jenkindocker.jar
COPY ${JAR_FILE} jenkindocker.jar
ENTRYPOINT ["java","-jar","/jenkindocker.jar"]