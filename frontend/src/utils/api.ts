import axios, {
  type AxiosInstance,
  type AxiosResponse,
  AxiosError,
} from "axios";
import { config } from "../config/env";

// API 기본 URL 설정
const API_BASE_URL = config.API_BASE_URL;

// Axios 인스턴스 생성
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: config.API_TIMEOUT,
  headers: {
    "Content-Type": "application/json",
  },
});

// 요청 인터셉터 (요청 전에 실행)
apiClient.interceptors.request.use(
  (requestConfig) => {
    // 토큰이 있다면 Authorization 헤더에 추가
    const token = localStorage.getItem(config.TOKEN_KEY);
    if (token) {
      requestConfig.headers.Authorization = `Bearer ${token}`;
    }
    return requestConfig;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터 (응답 후에 실행)
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  (error: AxiosError) => {
    // 401 에러 시 토큰 제거 및 로그인 페이지로 리다이렉트
    if (error.response?.status === 401) {
      localStorage.removeItem("authToken");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

// API 응답 타입 정의
export interface ApiResponse<T = any> {
  success: boolean;
  data: T;
  message?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    id: string;
    username: string;
    email?: string;
  };
}

export interface ErrorResponse {
  message: string;
  code?: string;
}

// API 함수들
export const authApi = {
  // 로그인
  login: async (
    credentials: LoginRequest
  ): Promise<ApiResponse<LoginResponse>> => {
    try {
      const response = await apiClient.post<ApiResponse<LoginResponse>>(
        "/api/auth/login",
        credentials
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  // 로그아웃
  logout: async (): Promise<void> => {
    try {
      await apiClient.post("/api/auth/logout");
      localStorage.removeItem("authToken");
    } catch (error) {
      // 로그아웃은 실패해도 토큰을 제거
      localStorage.removeItem("authToken");
      throw handleApiError(error);
    }
  },

  // 토큰 검증
  verifyToken: async (): Promise<ApiResponse<{ valid: boolean }>> => {
    try {
      const response = await apiClient.get<ApiResponse<{ valid: boolean }>>(
        "/api/auth/verify"
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },
};

// 에러 처리 함수
const handleApiError = (error: any): Error => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ErrorResponse>;

    if (axiosError.response) {
      // 서버에서 응답을 받았지만 에러 상태
      const message =
        axiosError.response.data?.message || "서버 오류가 발생했습니다.";
      return new Error(message);
    } else if (axiosError.request) {
      // 요청을 보냈지만 응답을 받지 못함
      return new Error(
        "네트워크 오류가 발생했습니다. 인터넷 연결을 확인해주세요."
      );
    }
  }

  // 기타 에러
  return new Error("알 수 없는 오류가 발생했습니다.");
};

// 토큰 관리 유틸리티
export const tokenUtils = {
  setToken: (token: string): void => {
    localStorage.setItem(config.TOKEN_KEY, token);
  },

  getToken: (): string | null => {
    return localStorage.getItem(config.TOKEN_KEY);
  },

  removeToken: (): void => {
    localStorage.removeItem(config.TOKEN_KEY);
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem(config.TOKEN_KEY);
  },
};

export default apiClient;
