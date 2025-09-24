# 개발 환경 설정 가이드

## 필수 요구사항
- Java 21
- Node.js 18+
- Docker & Docker Compose
- Python 3.10+

## 1. 환경변수 설정

`.env` 파일을 프로젝트 루트에 생성:

```bash
# Database
MYSQL_ROOT_PASSWORD=root1234
MYSQL_DATABASE=jbd_db
MYSQL_USER=jbd_user
MYSQL_PASSWORD=jbd1234

# OAuth
GOOGLE_CLIENT_ID=190740779393-1g5j5bmjkdvogl6135iitbbg84h6dp86.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=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

# OpenAI
OPENAI_API_KEY=your_openai_api_key

# SendGrid
SENDGRID_API_KEY=your_sendgrid_api_key

# JWT
JWT_SECRET=your_jwt_secret_key_here_at_least_256_bits
```

## 2. Docker 컨테이너 실행

```bash
docker-compose up -d
```

## 3. Backend 실행

```bash
cd backend
./gradlew bootRun
```

## 4. Frontend 실행

```bash
cd frontend
npm install
npm start
```

## 5. AI Service 실행

```bash
cd ai-service
pip install -r requirements.txt
uvicorn main:app --reload
```

## 테스트 실행

### Backend 테스트
```bash
cd backend
./gradlew test
```

### 통합 테스트
```bash
./gradlew integrationTest
```

## 접속 정보
- Backend API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:3000
- AI Service: http://localhost:8000
- MySQL: localhost:3306
- Redis: localhost:6379