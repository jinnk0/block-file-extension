FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew bootJar --no-daemon

ENV PORT=8080
ENV JAVA_OPTS=""

CMD java $JAVA_OPTS -Dserver.port=$PORT -jar build/libs/block-file-extension.jar
