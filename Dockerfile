FROM openjdk:14
ADD target/diary_app-1.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]