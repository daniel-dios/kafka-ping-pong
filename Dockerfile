FROM gradle:6.7.1-jdk15 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew clean build -x test

FROM openjdk:15-jdk
COPY --from=builder /home/gradle/src/build/libs/kafka-ping-pong.jar /app/app.jar
WORKDIR /app
CMD ["java", "-jar", "app.jar"]