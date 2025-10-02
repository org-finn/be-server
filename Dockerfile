# --- 1. 빌드 스테이지 ---
FROM amazoncorretto:21-alpine-jdk AS builder
WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x ./gradlew
RUN ./gradlew bootJar

# --- 2. 실행 스테이지 ---
FROM amazoncorretto:21-alpine-jre
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일만 복사
COPY --from=builder /workspace/app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]