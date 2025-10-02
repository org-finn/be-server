# --- 1. 빌드 스테이지: Gradle과 전체 JDK를 사용하여 애플리케이션을 빌드 ---
FROM amazoncorretto:21-alpine-jdk AS builder

# 작업 디렉토리 설정
WORKDIR /workspace/app

# 먼저 빌드 설정 파일들을 복사하여 Gradle 종속성 캐시를 활용합니다.
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 각 모듈의 빌드 스크립트도 복사
COPY module-app/build.gradle ./module-app/
COPY module-api/build.gradle ./module-api/
COPY module-domain/build.gradle ./module-domain/
COPY module-persistence/build.gradle ./module-persistence/
COPY module-external/build.gradle ./module-external/
COPY module-common/build.gradle ./module-common/

# 실제 소스코드를 복사하기 전에, 먼저 종속성만 다운로드하여 빌드 속도를 최적화
RUN chmod +x ./gradlew

RUN ./gradlew dependencies

# 이제 프로젝트의 모든 소스코드를 복사
COPY . .

# 'module-app' 모듈을 지정하여 실행 가능한 JAR 파일을 빌드
# '-x test' 옵션으로 빌드 시 테스트를 생략하여 CI/CD 시간을 단축
RUN ./gradlew :module-app:bootJar -x test

# --- 2. 실행 스테이지: JRE와 빌드된 JAR 파일만 사용하여 가볍고 안전한 최종 이미지 생성 ---
FROM amazoncorretto:21-alpine

# 작업 디렉토리 설정
WORKDIR /app

COPY --from=builder /workspace/app/module-app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]