# 잡았다(JOBATTA) AI Service API Documentation

## 📋 목차
1. [개요](#개요)
2. [인증](#인증)
3. [기본 정보](#기본-정보)
4. [API 엔드포인트](#api-엔드포인트)
   - [챗봇 API](#챗봇-api)
   - [AI 면접 API](#ai-면접-api)
   - [자소서 생성 API](#자소서-생성-api)
   - [번역 API](#번역-api)
   - [이미지 생성 API](#이미지-생성-api)
5. [에러 처리](#에러-처리)
6. [사용 예제](#사용-예제)

---

## 개요

**잡았다 AI Service**는 구직자를 위한 종합 AI 지원 서비스입니다.

### 주요 기능
- 🤖 **AI 챗봇**: 24/7 고객 지원 및 플랫폼 안내
- 💼 **AI 면접**: 기술/인성 면접 준비 및 피드백
- 📝 **자소서 생성**: RAG 기반 맞춤형 자기소개서 작성
- 🌐 **문서 번역**: 이력서 및 자소서 다국어 번역
- 🎨 **이미지 생성**: AI 기반 프로필 및 커뮤니티 이미지 생성

### 기술 스택
- **Framework**: FastAPI 0.104.1
- **AI Models**: OpenAI GPT-3.5-turbo, DALL-E-3
- **Vector DB**: LlamaIndex, FAISS
- **Embeddings**: OpenAI text-embedding-ada-002, sentence-transformers

---

## 인증

현재 버전은 인증이 필요하지 않습니다. Spring Boot 백엔드에서 JWT 토큰 기반 인증을 처리합니다.

---

## 기본 정보

### Base URL
```
http://localhost:8001
```

### Response Format
모든 응답은 다음 형식을 따릅니다:

```json
{
  "success": true,
  "message": "처리 완료 메시지",
  "data": {
    // 응답 데이터
  },
  "error": null
}
```

### Health Check
```http
GET /health
```

**Response:**
```json
{
  "status": "healthy"
}
```

---

## API 엔드포인트

## 챗봇 API

### 1. 챗봇과 대화하기
AI 챗봇과 대화를 나눕니다. 사용자별 대화 히스토리를 유지합니다.

```http
POST /api/v1/chatbot/chat
```

**Request Body:**
```json
{
  "user_id": "string",
  "message": "string"
}
```

**Response:**
```json
{
  "success": true,
  "message": "응답이 생성되었습니다",
  "data": {
    "user_id": "test_user",
    "message": "회원가입은 어떻게 하나요?",
    "response": "잡았다 플랫폼의 회원가입은 두 가지 방법이 있습니다...",
    "timestamp": "2025-09-01T12:00:00"
  }
}
```

### 2. 추천 질문 조회
자주 묻는 질문 목록을 조회합니다.

```http
GET /api/v1/chatbot/suggestions
```

**Response:**
```json
{
  "success": true,
  "message": "추천 질문을 조회했습니다",
  "data": {
    "suggestions": [
      "회원가입은 어떻게 하나요?",
      "AI 면접 기능을 사용하려면 어떻게 해야 하나요?",
      "자소서 생성 기능에 대해 알려주세요"
    ]
  }
}
```

### 3. 문의 카테고리 조회
문의 카테고리별 예시 질문을 조회합니다.

```http
GET /api/v1/chatbot/categories
```

**Response:**
```json
{
  "success": true,
  "message": "문의 카테고리를 조회했습니다",
  "data": {
    "categories": {
      "계정 관련": ["회원가입 방법", "비밀번호 찾기"],
      "플랫폼 기능": ["AI 면접 이용 방법", "자소서 생성 기능"],
      "증명서": ["증명서 종류", "신청 방법"],
      "기술 지원": ["로그인 문제", "파일 업로드 오류"]
    }
  }
}
```

### 4. 채팅 히스토리 조회
특정 사용자의 채팅 히스토리를 조회합니다.

```http
GET /api/v1/chatbot/history/{user_id}
```

**Response:**
```json
{
  "success": true,
  "message": "채팅 히스토리를 조회했습니다",
  "data": {
    "user_id": "test_user",
    "history": [
      {
        "user": "회원가입 방법이 궁금해요",
        "assistant": "회원가입은 두 가지 방법으로...",
        "timestamp": "2025-09-01T12:00:00"
      }
    ],
    "total_messages": 1
  }
}
```

### 5. 채팅 히스토리 삭제
특정 사용자의 채팅 히스토리를 삭제합니다.

```http
DELETE /api/v1/chatbot/history/{user_id}
```

**Response:**
```json
{
  "success": true,
  "message": "채팅 히스토리가 초기화되었습니다",
  "data": {
    "user_id": "test_user"
  }
}
```

### 6. 챗봇 서비스 상태 확인
챗봇 서비스의 상태를 확인합니다.

```http
GET /api/v1/chatbot/health
```

**Response:**
```json
{
  "success": true,
  "message": "챗봇 서비스가 정상 작동 중입니다",
  "data": {
    "chatbot_service": "healthy",
    "index_status": "loaded",
    "knowledge_base_size": 100
  }
}
```

### 7. 빠른 응답
미리 정의된 질문에 대한 빠른 응답을 제공합니다.

```http
POST /api/v1/chatbot/quick-response
```

**Request Body:**
```json
{
  "question_type": "회원가입"
}
```

---

## AI 면접 API

### 1. 면접 질문 생성
직무와 유형에 맞는 면접 질문을 생성합니다.

```http
POST /api/v1/interview/generate-questions
```

**Request Body:**
```json
{
  "position": "백엔드 개발자",
  "interview_type": "technical",
  "difficulty": "intermediate",
  "count": 5
}
```

**Response:**
```json
{
  "success": true,
  "message": "면접 질문이 생성되었습니다",
  "data": {
    "questions": [
      {
        "id": 1,
        "question": "REST API와 GraphQL의 차이점을 설명해주세요",
        "category": "웹 개발",
        "difficulty": "intermediate"
      }
    ]
  }
}
```

### 2. 답변 평가
면접 답변에 대한 AI 피드백을 제공합니다.

```http
POST /api/v1/interview/evaluate-answer
```

**Request Body:**
```json
{
  "question": "REST API와 GraphQL의 차이점을 설명해주세요",
  "answer": "REST API는 리소스 기반으로 설계되고...",
  "position": "백엔드 개발자"
}
```

**Response:**
```json
{
  "success": true,
  "message": "답변 평가가 완료되었습니다",
  "data": {
    "score": 85,
    "feedback": "전반적으로 좋은 답변입니다. 다만...",
    "strengths": ["명확한 설명", "적절한 예시"],
    "improvements": ["더 구체적인 사례 추가"]
  }
}
```

### 3. 면접 세션 완료
전체 면접 세션에 대한 종합 평가를 제공합니다.

```http
POST /api/v1/interview/complete-interview
```

**Request Body:**
```json
{
  "session_id": "session_123",
  "answers": [
    {
      "question_id": 1,
      "answer": "답변 내용..."
    }
  ]
}
```

---

## 자소서 생성 API

### 1. 자소서 질문 생성
기업과 직무에 맞는 자소서 질문을 생성합니다.

```http
POST /api/v1/cover-letter/generate-questions
```

**Request Body:**
```json
{
  "company": "네이버",
  "position": "백엔드 개발자"
}
```

**Response:**
```json
{
  "success": true,
  "message": "자소서 질문이 생성되었습니다",
  "data": {
    "questions": [
      "네이버에 지원하게 된 동기는 무엇인가요?",
      "백엔드 개발자로서 가장 자신있는 기술은?"
    ]
  }
}
```

### 2. 자소서 섹션 생성
특정 질문에 대한 자소서 답변을 생성합니다.

```http
POST /api/v1/cover-letter/generate-section
```

**Request Body:**
```json
{
  "question": "지원 동기를 작성해주세요",
  "user_input": "저는 네이버의 기술력에 매력을 느껴...",
  "company": "네이버",
  "position": "백엔드 개발자"
}
```

### 3. 전체 자소서 생성
완전한 자기소개서를 생성합니다.

```http
POST /api/v1/cover-letter/generate-complete
```

**Request Body:**
```json
{
  "company": "카카오",
  "position": "데이터 엔지니어",
  "user_experience": "3년간 데이터 분석 경험이 있습니다"
}
```

### 4. 자소서 피드백
작성된 자소서에 대한 개선 피드백을 제공합니다.

```http
POST /api/v1/cover-letter/feedback
```

**Request Body:**
```json
{
  "content": "저는 열정적인 개발자입니다...",
  "company": "토스",
  "position": "프론트엔드 개발자"
}
```

### 5. 컨텍스트 검색
자소서 작성에 참고할 수 있는 관련 정보를 검색합니다.

```http
GET /api/v1/cover-letter/search-context
```

**Query Parameters:**
- `query`: 검색 키워드
- `limit`: 결과 개수 (기본값: 5)

---

## 번역 API

### 1. 텍스트 번역
일반 텍스트를 번역합니다.

```http
POST /api/v1/translation/translate
```

**Request Body:**
```json
{
  "text": "안녕하세요. 저는 3년 경력의 개발자입니다.",
  "source_language": "ko",
  "target_language": "en"
}
```

**Response:**
```json
{
  "success": true,
  "message": "번역이 완료되었습니다",
  "data": {
    "original_text": "안녕하세요. 저는 3년 경력의 개발자입니다.",
    "translated_text": "Hello. I am a developer with 3 years of experience.",
    "source_language": "ko",
    "target_language": "en"
  }
}
```

### 2. 번역 템플릿 조회
문서 유형별 번역 템플릿을 조회합니다.

```http
GET /api/v1/translation/templates
```

**Response:**
```json
{
  "success": true,
  "message": "번역 템플릿을 조회했습니다",
  "data": {
    "templates": {
      "resume": "이력서 번역 템플릿",
      "cover_letter": "자소서 번역 템플릿"
    }
  }
}
```

### 3. 일괄 번역
여러 텍스트를 한 번에 번역합니다.

```http
POST /api/v1/translation/batch-translate
```

**Request Body:**
```json
{
  "texts": [
    "첫 번째 문장",
    "두 번째 문장"
  ],
  "target_language": "en"
}
```

---

## 이미지 생성 API

### 1. 이미지 생성
프롬프트 기반 이미지를 생성합니다.

```http
POST /api/v1/image/generate
```

**Request Body:**
```json
{
  "prompt": "전문적인 비즈니스 프로필 사진",
  "style": "professional",
  "size": "1024x1024"
}
```

**Response:**
```json
{
  "success": true,
  "message": "이미지가 생성되었습니다",
  "data": {
    "image_url": "https://generated-image-url.com/image.png",
    "prompt": "전문적인 비즈니스 프로필 사진",
    "style": "professional"
  }
}
```

### 2. 감정 분석 기반 이미지 생성
텍스트의 감정을 분석하여 적절한 이미지를 생성합니다.

```http
POST /api/v1/image/generate-with-sentiment
```

**Request Body:**
```json
{
  "text": "오늘 드디어 취업에 성공했습니다!",
  "style_preference": "celebration"
}
```

### 3. 이미지 변형 생성
기존 프롬프트의 변형 이미지를 생성합니다.

```http
POST /api/v1/image/generate-variations
```

**Request Body:**
```json
{
  "base_prompt": "프로페셔널한 개발자",
  "variation_count": 3
}
```

### 4. 감정 분석
텍스트의 감정을 분석합니다.

```http
POST /api/v1/image/analyze-sentiment
```

**Request Body:**
```json
{
  "text": "정말 기쁜 하루였습니다!"
}
```

### 5. 스타일 목록 조회
사용 가능한 이미지 스타일을 조회합니다.

```http
GET /api/v1/image/styles
```

**Response:**
```json
{
  "success": true,
  "message": "스타일 목록을 조회했습니다",
  "data": {
    "styles": [
      {
        "id": "professional",
        "name": "프로페셔널",
        "description": "비즈니스 및 전문적인 스타일"
      },
      {
        "id": "creative",
        "name": "크리에이티브",
        "description": "창의적이고 예술적인 스타일"
      }
    ]
  }
}
```

---

## 에러 처리

### HTTP 상태 코드
- `200 OK`: 성공
- `400 Bad Request`: 잘못된 요청
- `404 Not Found`: 리소스를 찾을 수 없음
- `422 Unprocessable Entity`: 유효성 검사 실패
- `500 Internal Server Error`: 서버 내부 오류

### 에러 응답 형식
```json
{
  "success": false,
  "message": "오류 메시지",
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "details": "상세 오류 정보"
  }
}
```

---

## 사용 예제

### Python
```python
import requests

# 챗봇과 대화
response = requests.post(
    "http://localhost:8001/api/v1/chatbot/chat",
    json={
        "user_id": "user123",
        "message": "AI 면접 준비는 어떻게 하나요?"
    }
)
print(response.json())
```

### JavaScript (Fetch API)
```javascript
// 자소서 생성
fetch('http://localhost:8001/api/v1/cover-letter/generate-complete', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    company: '삼성전자',
    position: '소프트웨어 엔지니어',
    user_experience: '컴퓨터공학 전공, 인턴 경험'
  })
})
.then(response => response.json())
.then(data => console.log(data));
```

### cURL
```bash
# 번역 요청
curl -X POST http://localhost:8001/api/v1/translation/translate \
  -H "Content-Type: application/json" \
  -d '{
    "text": "안녕하세요",
    "target_language": "en"
  }'
```

---

## 추가 정보

### Swagger UI
대화형 API 문서는 다음 URL에서 확인할 수 있습니다:
```
http://localhost:8001/docs
```

### ReDoc
더 자세한 API 스펙은 다음 URL에서 확인할 수 있습니다:
```
http://localhost:8001/redoc
```

### OpenAPI Specification
```
http://localhost:8001/openapi.json
```

### 지원 및 문의
- GitHub Issues: [github.com/jbd-team/ai-service/issues](https://github.com/jbd-team/ai-service/issues)
- Email: support@jobatta.com

---

## 버전 히스토리

### v1.0.0 (2025-09-01)
- 초기 릴리스
- 챗봇, AI 면접, 자소서 생성, 번역, 이미지 생성 기능 구현
- LlamaIndex 기반 RAG 시스템 적용
- OpenAI GPT-3.5-turbo 및 DALL-E-3 통합

---

**© 2025 JOBATTA. All Rights Reserved.**