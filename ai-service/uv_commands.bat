@echo off
echo UV 명령어 도우미 스크립트
echo =========================

if "%1"=="" (
    echo 사용법: uv_commands.bat [command]
    echo.
    echo 사용 가능한 명령어:
    echo   serve    - FastAPI 서버 실행
    echo   test     - 테스트 실행
    echo   install  - 의존성 설치/동기화
    echo   add      - 새 패키지 추가 ^(예: uv_commands.bat add requests^)
    echo   remove   - 패키지 제거 ^(예: uv_commands.bat remove requests^)
    echo   shell    - UV 환경에서 인터랙티브 쉘 시작
    echo   info     - 프로젝트 정보 출력
    echo   clean    - 캐시 및 임시파일 정리
    echo.
    exit /b 0
)

if "%1"=="serve" (
    echo FastAPI 서버 실행 중... ^(Ctrl+C로 종료^)
    uv run uvicorn main:app --reload --host localhost --port 8001
    goto :end
)

if "%1"=="test" (
    echo 테스트 실행 중...
    uv run pytest tests/ -v
    goto :end
)

if "%1"=="install" (
    echo 의존성 설치/동기화 중...
    uv sync
    goto :end
)

if "%1"=="add" (
    if "%2"=="" (
        echo 패키지명을 입력해주세요. 예: uv_commands.bat add requests
        exit /b 1
    )
    echo 패키지 추가 중: %2
    uv add %2
    goto :end
)

if "%1"=="remove" (
    if "%2"=="" (
        echo 패키지명을 입력해주세요. 예: uv_commands.bat remove requests
        exit /b 1
    )
    echo 패키지 제거 중: %2
    uv remove %2
    goto :end
)

if "%1"=="shell" (
    echo UV 환경에서 인터랙티브 쉘 시작...
    uv run python
    goto :end
)

if "%1"=="info" (
    echo 프로젝트 정보:
    echo ===============
    echo.
    echo 📦 의존성 트리:
    uv tree
    echo.
    echo 🐍 Python 버전:
    uv run python --version
    echo.
    echo 📁 프로젝트 구조:
    dir /b *.py 2>nul
    echo.
    goto :end
)

if "%1"=="clean" (
    echo 캐시 및 임시파일 정리 중...
    if exist "__pycache__" rmdir /s /q __pycache__
    if exist ".pytest_cache" rmdir /s /q .pytest_cache
    if exist "*.pyc" del /s *.pyc
    echo ✅ 정리 완료
    goto :end
)

echo 알 수 없는 명령어: %1
echo uv_commands.bat 를 실행하여 도움말을 확인하세요.

:end