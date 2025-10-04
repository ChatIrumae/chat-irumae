# 빌드 스테이지: Gradle + JDK21
FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /app

# 1) 의존성 캐시용: build.gradle과 wrapper 설정만 복사
COPY build.gradle settings.gradle gradle.* ./
# 의존성만 미리 다운로드 (테스트 제외)
RUN gradle dependencies --no-daemon

# 2) 실제 소스 복사 및 빌드
COPY . .
RUN gradle clean build --no-daemon

# 런타임 이미지: Liberica JDK21 Alpine
FROM bellsoft/liberica-openjdk-alpine:17
WORKDIR /app
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

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
    
EXPOSE 3001
ENTRYPOINT ["java","-jar","app.jar"]
