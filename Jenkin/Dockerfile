FROM openjdk:8
EXPOSE 8080
ADD target/jenkinsdocker.jar jenkinsdocker.jar
ENTRYPOINT["java","-jar","jenkinsdocker.jar"]