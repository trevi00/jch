# 잡았다 (JBD) - 국비 학원 원생 취업 지원 솔루션

## 프로젝트 개요
국비 학원 원생들의 취업을 지원하는 종합 플랫폼

## 주요 기능
- 사용자 관리 (일반/기업/관리자)
- OAuth 2.0 인증 (Google)
- 채용공고 관리
- AI 면접 시스템
- 자기소개서 생성 (RAG 기반)
- 커뮤니티
- 문서 번역
- 웹메일
- 고객 지원 챗봇

## 기술 스택
### Backend
- Spring Boot 3.3.4
- Java 21
- MySQL 8.0
- Redis
- QueryDSL
- Spring Security + JWT

### AI/ML
- FastAPI
- OpenAI GPT-4
- LangChain
- LlamaIndex

### Frontend
- React
- TypeScript

## 프로젝트 구조
```
jbd/
├── backend/          # Spring Boot 백엔드
├── ai-service/       # FastAPI AI 서비스
├── frontend/         # React 프론트엔드
└── docs/            # 프로젝트 문서
```

## 개발 환경 설정
[SETUP.md](./SETUP.md) 참조

## API 문서
[API.md](./API.md) 참조

## 데이터베이스 설계
[DATABASE.md](./DATABASE.md) 참조