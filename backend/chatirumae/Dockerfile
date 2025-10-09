# 빌드 스테이지 (변경 없음)
FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle gradle.* ./
RUN gradle dependencies --no-daemon
COPY . .
RUN gradle clean build -x test --no-daemon

# =========================================================================

# 런타임 이미지를 안정적인 debian 기반으로 변경
FROM bellsoft/liberica-openjdk-debian:17
WORKDIR /app

# 💡 [수정 포인트 1] 기존 라이브러리 설치 시 nodejs와 npm을 추가
RUN apt-get update && apt-get install -y \
    libnss3 \
    libnspr4 \
    libdbus-1-3 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libdrm2 \
    libxkbcommon0 \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxrandr2 \
    libgbm1 \
    libasound2 \
    libpangocairo-1.0-0 \
    libx11-xcb1 \
    libgtk-3-0 \
    nodejs \
    npm \
    && rm -rf /var/lib/apt/lists/*

# 💡 [수정 포인트 2] Playwright CLI를 설치하고, Chromium 브라우저를 미리 다운로드
RUN npm i -g playwright && \
    playwright install chromium

# 💡 [수정 포인트 3] Java Playwright가 미리 설치된 브라우저를 찾을 경로를 환경변수로 지정
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 3001
ENTRYPOINT ["java","-jar","app.jar"]