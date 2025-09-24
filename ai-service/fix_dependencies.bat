@echo off
echo 의존성 충돌 해결 및 환경 재설정
echo ================================

:: 기존 가상환경 삭제
if exist ".venv" (
    echo 기존 가상환경 삭제 중...
    rmdir /s /q .venv
)

:: 캐시 정리
if exist "__pycache__" (
    rmdir /s /q __pycache__
)

:: UV 의존성 동기화 (최신 호환 버전으로)
echo.
echo UV로 호환되는 의존성 설치 중...
uv sync
if %ERRORLEVEL% neq 0 (
    echo ERROR: 의존성 설치에 실패했습니다.
    echo.
    echo 수동으로 문제 해결을 시도합니다...
    echo.
    
    :: 문제가 되는 패키지들을 하나씩 설치
    echo 기본 패키지부터 설치 중...
    uv add "fastapi==0.104.1"
    uv add "uvicorn[standard]==0.24.0"
    uv add "openai>=1.6.1"
    uv add "pydantic>=2.5.0"
    uv add "python-dotenv==1.0.0"
    
    echo LlamaIndex 패키지 설치 중...
    uv add "llama-index>=0.9.0"
    uv add "llama-index-llms-openai"
    uv add "llama-index-embeddings-openai"
    
    echo 테스트 패키지 설치 중...
    uv add "pytest>=7.0.0" --group test
    uv add "pytest-asyncio>=0.21.0" --group test
    uv add "httpx>=0.25.0" --group test
    
    echo 추가 패키지 설치 중...
    uv add "requests>=2.31.0"
    uv add "aiofiles>=23.0.0"
    uv add "python-multipart>=0.0.6"
    
    if %ERRORLEVEL% neq 0 (
        echo 수동 설치도 실패했습니다. 개별적으로 확인이 필요합니다.
        pause
        exit /b 1
    )
)

:: 환경 테스트
echo.
echo 환경 테스트 중...
uv run python -c "import fastapi, openai, llama_index; print('✅ 주요 패키지 import 성공')"
if %ERRORLEVEL% neq 0 (
    echo WARNING: 패키지 import에 문제가 있을 수 있습니다.
)

:: 설치된 패키지 확인
echo.
echo 설치된 주요 패키지 버전:
uv run python -c "import fastapi; print(f'FastAPI: {fastapi.__version__}')" 2>nul
uv run python -c "import openai; print(f'OpenAI: {openai.__version__}')" 2>nul
uv run python -c "import llama_index; print(f'LlamaIndex: {llama_index.__version__}')" 2>nul

echo.
echo ================================
echo ✅ 의존성 문제 해결 완료!
echo.
echo 다음 명령어로 서버를 실행해보세요:
echo   .\uv_commands.bat serve
echo   또는
echo   uv run uvicorn main:app --reload --host localhost --port 8001
echo ================================

pause