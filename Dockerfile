FROM openjdk:11-jre-slim-buster
COPY . /app
WORKDIR /app
CMD java -agentlib:jdwp=transport=dt_socket,address=20713,suspend=n,server=y -jar target/*.jar
