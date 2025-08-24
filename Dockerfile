# 빌드
FROM gradle:8.14.3-jdk17 AS build

WORKDIR /home/gradle/src

COPY . .

RUN gradle bootJar --no-daemon

# 실행용 이미지
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=build /home/gradle/src/build/libs/app.jar app.jar

ENV JAVA_OPTS=""

CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar /app/app.jar"]
