@echo off
echo 챗봇 AI 서비스 가상환경 설정 스크립트
echo =====================================

:: 현재 디렉토리 확인
echo 현재 디렉토리: %cd%

:: Python 버전 확인
echo Python 버전 확인 중...
python --version
if %ERRORLEVEL% neq 0 (
    echo ERROR: Python이 설치되지 않았습니다.
    echo Python을 먼저 설치해주세요: https://www.python.org/downloads/
    pause
    exit /b 1
)

:: 가상환경 생성
echo.
echo 가상환경 생성 중... (venv)
python -m venv venv
if %ERRORLEVEL% neq 0 (
    echo ERROR: 가상환경 생성에 실패했습니다.
    pause
    exit /b 1
)

:: 가상환경 활성화
echo.
echo 가상환경 활성화 중...
call venv\Scripts\activate.bat
if %ERRORLEVEL% neq 0 (
    echo ERROR: 가상환경 활성화에 실패했습니다.
    pause
    exit /b 1
)

:: pip 업그레이드
echo.
echo pip 업그레이드 중...
python -m pip install --upgrade pip

:: requirements.txt 패키지 설치
echo.
echo 필수 패키지 설치 중...
pip install -r requirements.txt
if %ERRORLEVEL% neq 0 (
    echo ERROR: 패키지 설치에 실패했습니다.
    echo requirements.txt 파일을 확인해주세요.
    pause
    exit /b 1
)

:: 설치 완료 메시지
echo.
echo =====================================
echo 가상환경 설정이 완료되었습니다!
echo.
echo 사용법:
echo 1. 가상환경 활성화: venv\Scripts\activate
echo 2. FastAPI 서버 실행: python main.py
echo 3. 테스트 실행: python test_runner.py
echo 4. 가상환경 비활성화: deactivate
echo.
echo 현재 가상환경이 활성화된 상태입니다.
echo =====================================

pause