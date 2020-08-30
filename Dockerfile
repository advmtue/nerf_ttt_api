FROM openjdk:11.0.8-jre-buster

COPY target/api-*-SNAPSHOT.jar /tmp/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/tmp/app.jar"]

