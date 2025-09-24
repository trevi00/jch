# 잡았다 AI Service - API Quick Reference

## 🚀 빠른 시작

### 서버 실행
```bash
cd ai-service
uv run python main.py
```

### Health Check
```bash
curl http://localhost:8001/health
```

### API 문서
- Swagger UI: http://localhost:8001/docs
- ReDoc: http://localhost:8001/redoc

---

## 📌 주요 엔드포인트 (복사용)

### 챗봇 대화
```bash
curl -X POST http://localhost:8001/api/v1/chatbot/chat \
  -H "Content-Type: application/json" \
  -d '{"user_id": "test", "message": "안녕하세요"}'
```

### 면접 질문 생성
```bash
curl -X POST http://localhost:8001/api/v1/interview/generate-questions \
  -H "Content-Type: application/json" \
  -d '{"position": "백엔드 개발자", "interview_type": "technical", "count": 5}'
```

### 자소서 생성
```bash
curl -X POST http://localhost:8001/api/v1/cover-letter/generate-complete \
  -H "Content-Type: application/json" \
  -d '{"company": "네이버", "position": "개발자", "user_experience": "3년 경력"}'
```

### 번역
```bash
curl -X POST http://localhost:8001/api/v1/translation/translate \
  -H "Content-Type: application/json" \
  -d '{"text": "안녕하세요", "target_language": "en"}'
```

### 이미지 생성
```bash
curl -X POST http://localhost:8001/api/v1/image/generate \
  -H "Content-Type: application/json" \
  -d '{"prompt": "professional developer profile photo", "style": "professional"}'
```

---

## 📊 전체 엔드포인트 목록

| Method | Endpoint | Description |
|--------|----------|-------------|
| **기본** |
| GET | `/` | 루트 (API 정보) |
| GET | `/health` | 서버 상태 확인 |
| **챗봇** |
| POST | `/api/v1/chatbot/chat` | AI 챗봇과 대화 |
| GET | `/api/v1/chatbot/suggestions` | 추천 질문 조회 |
| GET | `/api/v1/chatbot/categories` | 문의 카테고리 조회 |
| GET | `/api/v1/chatbot/history/{user_id}` | 채팅 히스토리 조회 |
| DELETE | `/api/v1/chatbot/history/{user_id}` | 채팅 히스토리 삭제 |
| GET | `/api/v1/chatbot/health` | 챗봇 서비스 상태 |
| POST | `/api/v1/chatbot/quick-response` | 빠른 응답 |
| **AI 면접** |
| POST | `/api/v1/interview/generate-questions` | 면접 질문 생성 |
| POST | `/api/v1/interview/evaluate-answer` | 답변 평가 |
| POST | `/api/v1/interview/complete-interview` | 면접 세션 완료 |
| **자소서** |
| POST | `/api/v1/cover-letter/generate-questions` | 자소서 질문 생성 |
| POST | `/api/v1/cover-letter/generate-section` | 섹션별 생성 |
| POST | `/api/v1/cover-letter/generate-complete` | 전체 자소서 생성 |
| POST | `/api/v1/cover-letter/feedback` | 자소서 피드백 |
| GET | `/api/v1/cover-letter/search-context` | 컨텍스트 검색 |
| **번역** |
| POST | `/api/v1/translation/translate` | 텍스트 번역 |
| GET | `/api/v1/translation/templates` | 번역 템플릿 조회 |
| POST | `/api/v1/translation/batch-translate` | 일괄 번역 |
| **이미지** |
| POST | `/api/v1/image/generate` | 이미지 생성 |
| POST | `/api/v1/image/generate-with-sentiment` | 감정 기반 생성 |
| POST | `/api/v1/image/generate-variations` | 변형 이미지 생성 |
| POST | `/api/v1/image/analyze-sentiment` | 감정 분석 |
| GET | `/api/v1/image/styles` | 스타일 목록 조회 |

---

## 🔧 Python 테스트 스크립트

```python
import requests
import json

BASE_URL = "http://localhost:8001"

# 1. 챗봇 테스트
def test_chatbot():
    response = requests.post(
        f"{BASE_URL}/api/v1/chatbot/chat",
        json={"user_id": "test", "message": "회원가입 방법"}
    )
    print("Chatbot:", response.json()['data']['response'])

# 2. 면접 질문 테스트
def test_interview():
    response = requests.post(
        f"{BASE_URL}/api/v1/interview/generate-questions",
        json={
            "position": "백엔드 개발자",
            "interview_type": "technical",
            "count": 3
        }
    )
    print("Interview:", response.json())

# 3. 자소서 테스트
def test_cover_letter():
    response = requests.post(
        f"{BASE_URL}/api/v1/cover-letter/generate-complete",
        json={
            "company": "카카오",
            "position": "데이터 엔지니어",
            "user_experience": "빅데이터 처리 경험"
        }
    )
    print("Cover Letter:", response.json())

# 실행
if __name__ == "__main__":
    test_chatbot()
    test_interview()
    test_cover_letter()
```

---

## 🎯 Spring Boot 통합 예제

```java
@RestController
@RequestMapping("/api/ai")
public class AIServiceController {
    
    @Value("${ai.service.base-url}")
    private String aiServiceUrl;
    
    private final RestTemplate restTemplate;
    
    // 챗봇 호출
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        String url = aiServiceUrl + "/api/v1/chatbot/chat";
        
        Map<String, Object> aiRequest = new HashMap<>();
        aiRequest.put("user_id", request.getUserId());
        aiRequest.put("message", request.getMessage());
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            url, aiRequest, Map.class
        );
        
        return ResponseEntity.ok(response.getBody());
    }
}
```

---

## 📝 환경 변수 (.env)

```env
# OpenAI
OPENAI_API_KEY=your-api-key-here

# Server
FASTAPI_HOST=localhost
FASTAPI_PORT=8001

# Models
LLM_MODEL=gpt-3.5-turbo
EMBEDDING_MODEL=text-embedding-ada-002
IMAGE_MODEL=dall-e-3

# Database
VECTOR_DB_PATH=./data/vectorstore
CHAT_HISTORY_PATH=./data/chat_history
```

---

## 🐛 문제 해결

### SSL 인증서 오류
```python
# main.py 상단에 추가
import ssl
ssl._create_default_https_context = ssl._create_unverified_context
```

### 인코딩 오류 (Windows)
```python
# UTF-8 인코딩 설정
import sys
sys.stdout.reconfigure(encoding='utf-8')
```

### UV 환경 실행
```bash
# 패키지 설치
uv add package-name

# 서버 실행
uv run python main.py

# 테스트 실행
uv run pytest
```

---

## 📊 성능 최적화

### 권장 설정
- **동시 요청**: 100개까지
- **타임아웃**: 30초
- **캐시 TTL**: 300초 (5분)
- **벡터 검색 Top-K**: 5

### 모니터링
```python
# 응답 시간 측정
import time

start = time.time()
response = requests.post(url, json=data)
print(f"Response time: {time.time() - start:.2f}s")
```

---

## 🔐 보안 고려사항

1. **API Key 관리**: 환경 변수 사용
2. **Rate Limiting**: 분당 60 요청 제한
3. **Input Validation**: Pydantic 모델 사용
4. **CORS 설정**: 프로덕션에서 특정 도메인만 허용
5. **JWT 인증**: Spring Boot와 통합 시 적용

---

## 📚 추가 리소스

- [FastAPI 공식 문서](https://fastapi.tiangolo.com)
- [OpenAI API Reference](https://platform.openai.com/docs)
- [LlamaIndex Documentation](https://docs.llamaindex.ai)
- [프로젝트 GitHub](https://github.com/jbd-team/ai-service)

---

**Last Updated**: 2025-09-01
**Version**: 1.0.0