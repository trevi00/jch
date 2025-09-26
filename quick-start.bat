@echo off
REM JBD (잡았다) Quick Start Script
REM This script sets up and runs the complete JBD platform

echo ========================================
echo JBD (잡았다) Platform Quick Start
echo ========================================

if "%1"=="--check-only" (
    echo Checking system requirements only...
    goto CHECK_REQUIREMENTS
)

:CHECK_REQUIREMENTS
echo.
echo [1/6] Checking system requirements...

REM Check Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    exit /b 1
) else (
    echo ✓ Java is available
)

REM Check Node.js
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Node.js is not installed or not in PATH
    echo Please install Node.js 18 or higher
    exit /b 1
) else (
    echo ✓ Node.js is available
)

REM Check Python
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: Python is not installed or not in PATH
    echo Python is required for AI services
) else (
    echo ✓ Python is available
)

REM Check UV
uv --version >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: UV is not installed
    echo UV is required for Python dependency management
) else (
    echo ✓ UV is available
)

REM Check MySQL
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: MySQL client is not in PATH
    echo Make sure MySQL server is running on localhost:3306
) else (
    echo ✓ MySQL client is available
)

if "%1"=="--check-only" (
    echo.
    echo System check completed.
    exit /b 0
)

echo.
echo [2/6] Setting up database...
REM Import initial database schema and data
if exist "initial-data.sql" (
    echo Importing initial database data...
    mysql -u root -p12345 -h localhost -P 3306 jobplatform < initial-data.sql
    if %errorlevel% neq 0 (
        echo WARNING: Failed to import initial data
    ) else (
        echo ✓ Database setup completed
    )
) else (
    echo WARNING: initial-data.sql not found, skipping database setup
)

echo.
echo [3/6] Setting up backend dependencies...
cd backend
call .\gradlew build --no-daemon
if %errorlevel% neq 0 (
    echo ERROR: Backend build failed
    cd ..
    exit /b 1
)
cd ..
echo ✓ Backend dependencies ready

echo.
echo [4/6] Setting up frontend dependencies...
cd frontend
call npm install
if %errorlevel% neq 0 (
    echo ERROR: Frontend dependency installation failed
    cd ..
    exit /b 1
)
cd ..
echo ✓ Frontend dependencies ready

echo.
echo [5/6] Setting up AI service dependencies...
cd ai-service
if exist "requirements.txt" (
    uv sync
    if %errorlevel% neq 0 (
        echo WARNING: AI service setup failed
    ) else (
        echo ✓ AI service dependencies ready
    )
) else (
    echo WARNING: AI service requirements.txt not found
)
cd ..

echo.
echo [6/6] Starting all services...

echo Starting MySQL server (if not already running)...
net start MySQL80 >nul 2>&1

echo.
echo Starting backend server (Spring Boot on port 8081)...
start "JBD Backend" cmd /k "cd backend && .\gradlew bootRun --no-daemon"

echo.
echo Waiting for backend to start...
timeout /t 15 /nobreak >nul

echo.
echo Starting AI service (FastAPI on port 8001)...
start "JBD AI Service" cmd /k "cd ai-service && uv run uvicorn main:app --host 127.0.0.1 --port 8001"

echo.
echo Waiting for AI service to start...
timeout /t 10 /nobreak >nul

echo.
echo Starting frontend (React on port 3000)...
start "JBD Frontend" cmd /k "cd frontend && npm run dev"

echo.
echo ========================================
echo 🚀 JBD Platform is starting up!
echo ========================================
echo.
echo Services will be available at:
echo   Frontend: http://localhost:3000
echo   Backend:  http://localhost:8081
echo   AI Service: http://localhost:8001
echo.
echo Please wait a moment for all services to fully start.
echo Check the opened terminal windows for any errors.
echo.
echo To stop all services, close the terminal windows or press Ctrl+C in each.
echo ========================================

pause