# 환경 변수 설정

프로젝트 루트에 `.env.local` 파일을 생성하고 다음 환경 변수를 설정하세요:

```env
# 백엔드 API 기본 URL
VITE_API_BASE_URL=http://localhost:8080

# 개발 환경 설정
VITE_NODE_ENV=development
```

## 환경 변수 설명

- `VITE_API_BASE_URL`: 백엔드 API 서버의 기본 URL
- `VITE_NODE_ENV`: 현재 환경 (development, production)

## 주의사항

- Vite에서는 환경 변수명이 `VITE_`로 시작해야 클라이언트에서 접근 가능합니다.
- `.env.local` 파일은 Git에 커밋하지 마세요.
- 프로덕션 환경에서는 실제 서버 URL로 변경하세요.
