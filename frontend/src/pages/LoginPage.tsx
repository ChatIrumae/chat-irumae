import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi, tokenUtils } from "../utils/api";
import "./LoginPage.css";

export interface LoginFormData {
  portalId: string;
  portalPassword: string;
}

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<LoginFormData>({
    portalId: "",
    portalPassword: "",
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // 에러 메시지 초기화
    if (error) setError("");
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const response = await authApi.login(formData);

      if (response) {
        // 토큰 저장
        tokenUtils.setToken(response);

        // 로그인 성공 처리
        console.log("로그인 성공:", response.data);

        // 메인 페이지로 리다이렉트
        navigate("/dashboard");
      } else {
        setError(response.message || "로그인에 실패했습니다.");
      }
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "로그인 중 오류가 발생했습니다.";
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <h1>Chat Irumae</h1>
          <p>로그인하여 채팅을 시작하세요</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="portalId">사용자명</label>
            <input
              type="text"
              id="portalId"
              name="portalId"
              value={formData.portalId}
              onChange={handleInputChange}
              placeholder="사용자명을 입력하세요"
              required
              disabled={isLoading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="portalPassword">비밀번호</label>
            <input
              type="portalPassword"
              id="portalPassword"
              name="portalPassword"
              value={formData.portalPassword}
              onChange={handleInputChange}
              placeholder="비밀번호를 입력하세요"
              required
              disabled={isLoading}
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" className="login-button" disabled={isLoading}>
            {isLoading ? "로그인 중..." : "로그인"}
          </button>
        </form>

        <div className="login-footer">
          <p>
            계정이 없으신가요? <a href="/register">회원가입</a>
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
