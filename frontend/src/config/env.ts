// 환경 변수 설정
export const config = {
  // API 기본 URL
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",

  // 개발 환경 여부
  IS_DEVELOPMENT: import.meta.env.VITE_NODE_ENV === "development",

  // 프로덕션 환경 여부
  IS_PRODUCTION: import.meta.env.VITE_NODE_ENV === "production",

  // 앱 이름
  APP_NAME: "Chat Irumae",

  // API 타임아웃 (밀리초)
  API_TIMEOUT: 10000,

  // 토큰 키
  TOKEN_KEY: "authToken",
} as const;

// 환경 변수 검증
export const validateEnv = () => {
  const requiredVars = ["VITE_API_BASE_URL"] as const;

  const missingVars = requiredVars.filter(
    (varName) => !import.meta.env[varName]
  );

  if (missingVars.length > 0) {
    console.warn(
      `다음 환경 변수가 설정되지 않았습니다: ${missingVars.join(
        ", "
      )}. 기본값을 사용합니다.`
    );
  }

  return missingVars.length === 0;
};

// 개발 환경에서만 환경 변수 정보 출력
if (config.IS_DEVELOPMENT) {
  console.log("환경 설정:", {
    API_BASE_URL: config.API_BASE_URL,
    NODE_ENV: import.meta.env.VITE_NODE_ENV,
  });
}
