FROM openjdk:latest

ADD target/adb-java-health-check-*-jar-with-dependencies.jar /app.jar
CMD java -jar app.jar
