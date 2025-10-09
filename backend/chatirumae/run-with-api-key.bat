@echo off
echo ========================================
echo Chat-Irumae 애플리케이션 실행
echo ========================================
echo.

REM API 키 입력 받기
set /p OPENAI_API_KEY="OpenAI API 키를 입력하세요: "

if "%OPENAI_API_KEY%"=="" (
    echo.
    echo 오류: API 키가 입력되지 않았습니다.
    echo 애플리케이션을 종료합니다.
    pause
    exit /b 1
)

echo.
echo API 키가 설정되었습니다: %OPENAI_API_KEY:~0,10%...
echo.

REM 환경변수 설정하고 애플리케이션 실행
set OPENAI_API_KEY=%OPENAI_API_KEY%
echo 애플리케이션을 시작합니다...
echo.

gradlew bootRun

pause
