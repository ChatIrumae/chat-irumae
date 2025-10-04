# 빌드 스테이지: Gradle + JDK17 (변경 없음)
FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle gradle.* ./
RUN gradle dependencies --no-daemon
COPY . .
RUN gradle clean build --no-daemon

# =========================================================================

# 런타임 이미지: Liberica JDK17 Alpine
FROM bellsoft/liberica-openjdk-alpine:17
WORKDIR /app

# Alpine Linux의 패키지 매니저(apk)를 사용하여 의존성 설치
USER root
RUN apk update && apk add --no-cache \
    udev \
    ttf-freefont \
    chromium

# 보안을 위해 non-root 유저로 다시 전환
USER bellsoft

# 💡 [수정 포인트] --chown 옵션을 추가하여 파일 소유자를 bellsoft로 지정
COPY --chown=bellsoft:bellsoft --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 3001
ENTRYPOINT ["java","-jar","app.jar"]