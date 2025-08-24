FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# 3. Gradle Wrapper와 프로젝트 파일 복사
COPY . .

# 4. 프로젝트 빌드 (Gradle 사용 시)
RUN ./gradlew bootJar --no-daemon

# 5. 환경 변수 기본값 (Render가 자동으로 PORT를 주입)
ENV PORT=8080
ENV JAVA_OPTS=""

# 6. 컨테이너 시작 시 실행할 명령어
CMD java $JAVA_OPTS -Dserver.port=$PORT -jar build/libs/block-file-extension.jar
