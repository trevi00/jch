@echo off
echo 가상환경 활성화 중...

if not exist "venv" (
    echo ERROR: 가상환경이 존재하지 않습니다.
    echo 먼저 setup_env.bat을 실행하여 가상환경을 생성해주세요.
    pause
    exit /b 1
)

call venv\Scripts\activate.bat

echo.
echo ✅ 가상환경이 활성화되었습니다!
echo 📁 프로젝트: 잡았다 AI 서비스
echo 🐍 Python: %cd%\venv
echo.
echo 사용 가능한 명령어:
echo   - python main.py          : FastAPI 서버 실행
echo   - python test_runner.py   : 테스트 실행
echo   - pip list                : 설치된 패키지 목록
echo   - deactivate              : 가상환경 비활성화
echo.

cmd /k