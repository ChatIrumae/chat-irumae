# Dockerfile_frontend

# --- 1. 빌드 단계 (Build Stage) ---
# Node.js 18 버전을 빌드 환경으로 사용합니다.
FROM node:20-alpine as builder

# 작업 디렉토리를 /app으로 설정합니다.
WORKDIR /app

# package.json과 package-lock.json 파일을 복사합니다.
COPY package.json ./
COPY package-lock.json ./

# 의존성 패키지를 설치합니다.
RUN npm install

# 프로젝트의 모든 소스 코드를 복사합니다.
COPY . .

# React 앱을 빌드합니다.
RUN npm run build

# --- 2. 서빙 단계 (Serve Stage) ---
# 경량 웹서버인 Nginx를 기반으로 최종 이미지를 생성합니다.
FROM nginx:1.23-alpine

# 빌드 단계에서 생성된 빌드 결과물을 Nginx의 기본 html 폴더로 복사합니다.
COPY --from=builder /app/dist /usr/share/nginx/html

# 커스텀 Nginx 설정을 복사합니다.
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Nginx가 사용하는 80 포트를 외부에 알립니다.
EXPOSE 3000

# 컨테이너가 시작될 때 Nginx를 실행합니다.
CMD ["nginx", "-g", "daemon off;"]