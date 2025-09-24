@echo off
echo UV를 사용한 챗봇 AI 서비스 가상환경 설정
echo ============================================

:: 현재 디렉토리 확인
echo 현재 디렉토리: %cd%

:: UV 설치 확인
echo UV 설치 상태 확인 중...
uv --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo UV가 설치되지 않았습니다. UV를 설치하는 중...
    
    :: UV 설치 (PowerShell 방식)
    powershell -Command "irm https://astral.sh/uv/install.ps1 | iex"
    if %ERRORLEVEL% neq 0 (
        echo ERROR: UV 설치에 실패했습니다.
        echo 수동으로 설치해주세요: https://github.com/astral-sh/uv
        pause
        exit /b 1
    )
    
    :: PATH 새로고침을 위해 새 세션에서 계속 진행
    echo UV 설치가 완료되었습니다. 새로운 터미널을 열어주세요.
    pause
    exit /b 0
) else (
    echo ✅ UV가 이미 설치되어 있습니다.
    uv --version
)

:: Python 프로젝트 초기화
echo.
echo Python 프로젝트 초기화 중...
uv init --no-readme
if %ERRORLEVEL% neq 0 (
    echo 프로젝트가 이미 초기화되어 있거나 오류가 발생했습니다.
)

:: pyproject.toml이 없다면 생성
if not exist "pyproject.toml" (
    echo pyproject.toml 생성 중...
    (
        echo [project]
        echo name = "jbd-ai-service"
        echo version = "1.0.0"
        echo description = "잡았다 플랫폼 AI 서비스"
        echo dependencies = [
        echo     "fastapi==0.104.1",
        echo     "uvicorn[standard]==0.24.0",
        echo     "openai==1.3.8",
        echo     "langchain==0.1.0",
        echo     "langchain-openai==0.0.2",
        echo     "langchain-community==0.0.10",
        echo     "chromadb==0.4.18",
        echo     "pypdf2==3.0.1",
        echo     "python-multipart==0.0.6",
        echo     "pydantic==2.5.0",
        echo     "python-dotenv==1.0.0",
        echo     "aiofiles==23.2.1",
        echo     "pillow==10.1.0",
        echo     "requests==2.31.0",
        echo     "numpy==1.24.3",
        echo     "pandas==2.0.3",
        echo     "sentence-transformers==2.2.2",
        echo     "faiss-cpu==1.7.4",
        echo     "llama-index==0.9.6",
        echo     "llama-index-llms-openai==0.1.5",
        echo     "llama-index-embeddings-openai==0.1.6",
        echo     "pytest==7.4.3",
        echo     "pytest-asyncio==0.21.1",
        echo     "httpx==0.25.2",
        echo ]
        echo.
        echo [tool.pytest.ini_options]
        echo testpaths = ["tests"]
        echo python_files = "test_*.py"
        echo python_functions = "test_*"
        echo addopts = "-v --tb=short"
        echo asyncio_mode = "auto"
        echo.
        echo [build-system]
        echo requires = ["hatchling"]
        echo build-backend = "hatchling.build"
    ) > pyproject.toml
)

:: 가상환경 생성 및 패키지 설치
echo.
echo UV를 사용하여 가상환경 생성 및 의존성 설치 중...
uv sync
if %ERRORLEVEL% neq 0 (
    echo ERROR: 의존성 설치에 실패했습니다.
    pause
    exit /b 1
)

:: UV 환경 정보 출력
echo.
echo ============================================
echo ✅ UV 가상환경 설정이 완료되었습니다!
echo.
echo 📦 프로젝트 정보:
uv tree --depth 1

echo.
echo 🚀 사용법:
echo   uv run python main.py           # FastAPI 서버 실행
echo   uv run python test_runner.py    # 테스트 실행
echo   uv run pytest                   # pytest 직접 실행
echo   uv add [패키지명]               # 새 패키지 추가
echo   uv remove [패키지명]            # 패키지 제거
echo   uv sync                         # 의존성 동기화
echo.
echo 💡 UV는 자동으로 가상환경을 관리하므로 별도 활성화가 불필요합니다.
echo ============================================

pause