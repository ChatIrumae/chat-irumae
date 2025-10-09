@echo off
echo OpenAI API 키를 설정합니다.
echo.
set /p OPENAI_API_KEY="OpenAI API 키를 입력하세요: "
echo.
echo 환경변수가 설정되었습니다: %OPENAI_API_KEY%
echo.
echo 애플리케이션을 실행하려면 다음 명령을 사용하세요:
echo gradlew bootRun
pause
