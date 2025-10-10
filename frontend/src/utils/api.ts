import axios, {
  type AxiosInstance,
  type AxiosResponse,
  AxiosError,
} from "axios";
import { config } from "../config/env";
import type { Message } from "../pages/DashboardPage";

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
  portalId: string;
  portalPassword: string;
}

export interface LoginResponse {
  token: string;
  studentId: string;
  name: string;
}

export interface ErrorResponse {
  message: string;
  code?: string;
}

export interface ChatRequest {
  message: string;
}

export interface ChatResponse {
  response: string;
}

// API 함수들
export const authApi = {
  // 로그인
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    try {
      const response = await apiClient.post<LoginResponse>(
        "/api/login",
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

// 채팅 API
export const chatApi = {
  // 채팅 메시지 전송
  sendMessage: async (message: Message): Promise<string> => {
    try {
      const response = await apiClient.post<string>("/api/chat", message);
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
    console.log(axiosError);

    if (axiosError.response) {
      // 서버에서 응답을 받았지만 에러 상태
      const message =
        axiosError.response.data?.message ||
        "아이디 또는 비밀번호가 틀렸습니다";
      return new Error(message);
    } else if (axiosError.request) {
      // 요청을 보냈지만 응답을 받지 못함
      return new Error("올바른 아이디와 비밀번호를 입력해주세요.");
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

  // 사용자 정보 저장
  setUserInfo: (studentId: string, name: string): void => {
    localStorage.setItem("studentId", studentId);
    localStorage.setItem("userName", name);
  },

  // 사용자 정보 가져오기
  getStudentId: (): string | null => {
    return localStorage.getItem("studentId");
  },

  getUserName: (): string | null => {
    return localStorage.getItem("userName");
  },

  // 사용자 정보 제거
  removeUserInfo: (): void => {
    localStorage.removeItem("studentId");
    localStorage.removeItem("userName");
  },

  // 로그아웃 (토큰과 사용자 정보 모두 제거)
  logout: (): void => {
    tokenUtils.removeToken();
    tokenUtils.removeUserInfo();
  },
};

export default apiClient;
