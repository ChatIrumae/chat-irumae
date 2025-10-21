import BackgroundShapes from "../components/LoginPage/BackgroundShapes";
import AuthCard from "../components/LoginPage/AuthCard";
import Logo from "../components/LoginPage/Logo";
import TextInput from "../components/LoginPage/TextInput";
import Button from "../components/LoginPage/Button";
import "../styles/login.css";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi, tokenUtils } from "../utils/api";

export interface LoginFormData {
  portalId: string;
  portalPassword: string;
}

export default function Login() {
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

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const response = await authApi.login(formData);

      if (response) {
        // 토큰과 사용자 정보 저장
        tokenUtils.setToken(response.token);
        tokenUtils.setUserInfo(response.studentId, response.name);

        // 로그인 성공 처리
        console.log("로그인 성공:", response);

        // 메인 페이지로 리다이렉트
        navigate("/dashboard");
      } else {
        setError("로그인에 실패했습니다.");
      }
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "로그인 중 오류가 발생했습니다.";
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  //
  return (
    <div className="login-root">
      <BackgroundShapes />

      <div className="login-center">
        <AuthCard>
          <div className="login-logo">
            <Logo />
          </div>

          <form className="login-form" onSubmit={onSubmit}>
            <TextInput
              type="text"
              placeholder="USERNAME"
              icon="user"
              name="portalId"
              value={formData.portalId}
              onChange={handleInputChange}
              autoComplete="username"
              disabled={isLoading}
            />
            <TextInput
              type="password"
              placeholder="PASSWORD"
              icon="lock"
              name="portalPassword"
              value={formData.portalPassword}
              onChange={handleInputChange}
              autoComplete="current-password"
              disabled={isLoading}
            />

            {error && <div className="error-message">{error}</div>}

            <Button type="submit" disabled={isLoading}>
              {isLoading ? "로그인 중..." : "LOGIN"}
            </Button>

            <a className="forgot" href="#">
              Forgot password?
            </a>
          </form>
        </AuthCard>
      </div>
    </div>
  );
}
