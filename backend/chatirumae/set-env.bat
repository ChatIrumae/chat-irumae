@echo off
echo ========================================
echo Chat-Irumae 환경변수 설정
echo ========================================
echo.
echo OpenAI API 키를 설정합니다.
echo (API 키는 'sk-'로 시작해야 합니다)
echo.
set /p OPENAI_API_KEY="OpenAI API 키를 입력하세요: "
echo.
echo 환경변수가 설정되었습니다: %OPENAI_API_KEY%
echo.
echo ChromaDB 서버 상태를 확인합니다...
curl -s http://54.180.203.59:8000/api/v1/heartbeat >nul 2>&1
if %errorlevel% equ 0 (
    echo ChromaDB 서버: 연결됨
) else (
    echo ChromaDB 서버: 연결 실패 - 서버가 실행 중인지 확인해주세요
)
echo.
echo 애플리케이션을 실행하려면 다음 명령을 사용하세요:
echo gradlew bootRun
echo.
echo 헬스체크를 확인하려면:
echo curl http://localhost:3001/api/health
echo.
pause
