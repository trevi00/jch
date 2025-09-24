# 잡았다 (JBD) - 국비 학원 원생 취업 지원 솔루션

## 📋 프로젝트 개요
국비 학원 원생들의 취업을 종합적으로 지원하는 플랫폼

## 🚀 주요 기능

### 사용자 관리
- 일반 유저 / 기업 유저 / 관리자 유저 구분
- OAuth 2.0 (Google) 및 전통적 회원가입 지원
- 사내 이메일 인증을 통한 기업 유저 전환

### 채용 서비스
- 채용공고 등록 및 관리
- 맞춤형 채용공고 추천
- 지원 현황 관리

### AI 서비스
- AI 면접 (기술/인성)
- RAG 기반 자기소개서 생성
- 감정 분석 및 이미지 생성
- LlamaIndex 기반 고객 지원 챗봇

### 커뮤니티
- 취업정보, 면접정보, Q&A 게시판
- 감정 분석 기반 게시글 분류
- 기업 게시판 (인증 유저만)

### 부가 서비스
- 문서 번역 (맞춤형 번역)
- SendGrid 기반 웹메일
- 증명서 발급 시스템

## 🛠 기술 스택

### Backend
- Spring Boot 3.3.4
- Java 21
- MySQL 8.0
- Redis
- Spring Security + JWT
- QueryDSL

### AI/ML
- FastAPI
- OpenAI GPT-4
- LangChain
- LlamaIndex

### Infrastructure
- Docker & Docker Compose
- AWS S3
- SendGrid

## 📁 프로젝트 구조
```
jbd/
├── backend/          # Spring Boot 백엔드
├── ai-service/       # FastAPI AI 서비스  
├── frontend/         # React 프론트엔드
├── docs/            # 프로젝트 문서
└── docker-compose.yml
```

## 🚀 Quick Start

### 1. 환경변수 설정
`.env.example`을 참고하여 `.env` 파일 생성

### 2. Docker 컨테이너 실행
```bash
docker-compose up -d
```

### 3. Backend 실행
```bash
cd backend
./gradlew bootRun
```

### 4. 접속
- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html

## 📚 문서
- [개발 환경 설정](./docs/SETUP.md)
- [API 문서](./docs/API.md)
- [데이터베이스 설계](./docs/DATABASE.md)

## 👥 팀 정보
국비 학원 취업 지원 프로젝트 팀