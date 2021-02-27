# https://blog.geekhunter.com.br/deploy-imagem-docker-heroku-utilizando-springboot/
FROM gradle:6.8.2-jdk11-openj9 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle bootJar --no-daemon

FROM openjdk:11-jre-slim

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/catalogodeleite.jar

CMD ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILE}", "/app/catalogodeleite.jar"]