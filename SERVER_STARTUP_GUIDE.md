# 잡았다(JBD) 서버 구동 가이드

## 개요
잡았다 프로젝트는 3개의 서비스로 구성되어 있습니다:
- **AI Service (FastAPI)** - 포트 8001
- **Backend Service (Spring Boot)** - 포트 8081
- **Frontend Service (React + Vite)** - 포트 3000

## 사전 요구사항
- Python 3.8+ (AI Service용)
- Java 21+ (Backend용)
- Node.js 16+ (Frontend용)
- 필요 시 SSL 인증서 문제 해결을 위한 환경변수 설정

## 서버 구동 순서

### 1. 기존 서버 종료 (필요 시)

```bash
# 포트 사용 중인 프로세스 확인
netstat -ano | findstr ":8001"
netstat -ano | findstr ":8081"
netstat -ano | findstr ":3000"

# PowerShell로 프로세스 종료
powershell "Stop-Process -Id [PID] -Force"
```

### 2. AI Service (FastAPI) 구동

```bash
# 디렉토리 이동
cd "C:\Users\user\IdeaProjects\jbd\ai-service"

# SSL 인증서 문제 해결을 위한 환경변수 설정
set CURL_CA_BUNDLE=
set REQUESTS_CA_BUNDLE=

# 필요한 패키지 설치 (최초 한 번)
pip install aiofiles --trusted-host pypi.org --trusted-host pypi.python.org --trusted-host files.pythonhosted.org

# 서버 구동
python main.py
```

**확인**: http://localhost:8001/health → `{"status":"healthy"}`

### 3. Backend Service (Spring Boot) 구동

```bash
# 디렉토리 이동
cd "C:\Users\user\IdeaProjects\jbd\backend"

# Gradle로 서버 구동
./gradlew bootRun
```

**확인**: http://localhost:8081/api/actuator/health → `{"status":"UP"}`

### 4. Frontend Service (React + Vite) 구동

```bash
# 디렉토리 이동
cd "C:\Users\user\IdeaProjects\jbd\frontend"

# 개발 서버 구동
npm run dev
```

**확인**: http://localhost:3000 → React 애플리케이션 로드

## 백그라운드 실행 (권장)

각 서비스를 백그라운드에서 실행하려면 명령어 끝에 `&`를 추가하거나 별도의 터미널 창을 사용하세요.

### PowerShell에서 백그라운드 실행 예시:

```powershell
# AI Service
Start-Process -FilePath "python" -ArgumentList "main.py" -WorkingDirectory "C:\Users\user\IdeaProjects\jbd\ai-service"

# Backend Service
Start-Process -FilePath "cmd" -ArgumentList "/c", "gradlew bootRun" -WorkingDirectory "C:\Users\user\IdeaProjects\jbd\backend"

# Frontend Service
Start-Process -FilePath "cmd" -ArgumentList "/c", "npm run dev" -WorkingDirectory "C:\Users\user\IdeaProjects\jbd\frontend"
```

## 주요 엔드포인트

### AI Service (Port 8001)
- Health Check: `GET /health`
- API 문서: `GET /docs` (FastAPI 자동 생성)
- 면접: `/api/v1/interview/*`
- 자소서: `/api/v1/cover-letter/*`
- 챗봇: `/api/v1/chatbot/*`
- 이미지 생성: `/api/v1/image/*`

### Backend Service (Port 8081)
- Health Check: `GET /api/actuator/health`
- 인증: `/api/auth/*`
- 게시판: `/api/posts/*`
- 채용공고: `/api/job-postings/*`
- 사용자: `/api/users/*`

### Frontend Service (Port 3000)
- 메인 페이지: `http://localhost:3000`
- React Router 기반 SPA

## 문제 해결

### AI Service 관련
- **ImportError: No module named 'aiofiles'**: pip install aiofiles
- **SSL 인증서 오류**: 환경변수 CURL_CA_BUNDLE, REQUESTS_CA_BUNDLE 제거
- **OpenAI API 키 오류**: .env 파일의 OPENAI_API_KEY 확인

### Backend Service 관련
- **포트 충돌**: 기존 Java 프로세스 종료
- **Gradle 빌드 오류**: ./gradlew clean build
- **데이터베이스 연결 오류**: H2 인메모리 DB 설정 확인

### Frontend Service 관련
- **npm start 오류**: package.json에 start 스크립트가 없음, npm run dev 사용
- **포트 충돌**: Vite가 자동으로 다른 포트 할당
- **CORS 오류**: 백엔드 CORS 설정 확인

## 개발 팁

1. **개발 중 자동 재시작**:
   - AI Service: `uvicorn main:app --reload --host localhost --port 8001`
   - Backend: Spring Boot DevTools 활성화됨 (자동 재시작)
   - Frontend: Vite HMR 활성화됨 (핫 리로드)

2. **로그 확인**:
   - AI Service: 콘솔 출력
   - Backend: 콘솔 출력 + 로그 파일
   - Frontend: 브라우저 개발자 도구

3. **API 테스트**:
   - Postman, curl, 또는 FastAPI docs 활용
   - 인증이 필요한 API는 JWT 토큰 포함

## 마지막 업데이트
2025-09-15 - 모든 서비스 정상 구동 확인 완료