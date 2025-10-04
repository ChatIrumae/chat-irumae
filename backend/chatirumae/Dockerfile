# 빌드 스테이지 (변경 없음)
FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle gradle.* ./
RUN gradle dependencies --no-daemon
COPY . .
RUN gradle clean build --no-daemon

# =========================================================================

# 💡 [수정] 런타임 이미지를 안정적인 debian 기반으로 변경
FROM bellsoft/liberica-openjdk-debian:17
WORKDIR /app

# debian에서는 apt-get을 사용하며, 라이브러리 목록도 검증되어 있음
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
    && rm -rf /var/lib/apt/lists/*

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 3001
ENTRYPOINT ["java","-jar","app.jar"]