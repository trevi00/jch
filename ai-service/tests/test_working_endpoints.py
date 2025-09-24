#!/usr/bin/env python3
"""
실제 작동하는 엔드포인트들의 테스트
"""

import pytest
from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

# ==================== BASIC TESTS ====================

class TestBasicEndpoints:
    """기본 엔드포인트 테스트"""
    
    def test_root_endpoint(self):
        """루트 엔드포인트 테스트"""
        response = client.get("/")
        assert response.status_code == 200
        data = response.json()
        assert "message" in data
    
    def test_health_endpoint(self):
        """헬스체크 엔드포인트 테스트"""
        response = client.get("/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "healthy"

# ==================== CHATBOT TESTS ====================

class TestChatbotEndpoints:
    """챗봇 엔드포인트 테스트"""
    
    def test_chat_success(self):
        """정상 챗봇 대화 테스트"""
        response = client.post(
            "/api/v1/chatbot/chat",
            json={"user_id": "test_user", "message": "Hello"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "response" in data["data"]
    
    def test_chat_empty_message(self):
        """빈 메시지 테스트"""
        response = client.post(
            "/api/v1/chatbot/chat",
            json={"user_id": "test_user", "message": ""}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is False
    
    def test_chat_xss_prevention(self):
        """XSS 방지 테스트"""
        response = client.post(
            "/api/v1/chatbot/chat",
            json={"user_id": "test_user", "message": "<script>alert('XSS')</script>Test"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        # XSS 태그가 제거되었는지 확인
        assert "<script>" not in data["data"]["message"]
        assert "<script>" not in data["data"]["response"]
    
    def test_suggestions(self):
        """추천 질문 조회 테스트"""
        response = client.get("/api/v1/chatbot/suggestions")
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "suggestions" in data["data"]
        assert len(data["data"]["suggestions"]) > 0
    
    def test_categories(self):
        """카테고리 조회 테스트"""
        response = client.get("/api/v1/chatbot/categories")
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "categories" in data["data"]
        assert isinstance(data["data"]["categories"], dict)
    
    def test_history_and_delete(self):
        """히스토리 조회 및 삭제 테스트"""
        user_id = "test_history_user"
        
        # 먼저 대화 생성
        client.post(
            "/api/v1/chatbot/chat",
            json={"user_id": user_id, "message": "Test message"}
        )
        
        # 히스토리 조회
        response = client.get(f"/api/v1/chatbot/history/{user_id}")
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["data"]["user_id"] == user_id
        
        # 히스토리 삭제
        response = client.delete(f"/api/v1/chatbot/history/{user_id}")
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
    
    def test_chatbot_health(self):
        """챗봇 헬스체크 테스트"""
        response = client.get("/api/v1/chatbot/health")
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True

# ==================== INTERVIEW TESTS ====================

class TestInterviewEndpoints:
    """면접 엔드포인트 테스트"""
    
    def test_generate_questions(self):
        """면접 질문 생성 테스트"""
        response = client.post(
            "/api/v1/interview/generate-questions",
            json={
                "position": "Backend Developer",
                "interview_type": "technical",
                "count": 3
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "questions" in data["data"]
    
    def test_evaluate_answer(self):
        """답변 평가 테스트"""
        response = client.post(
            "/api/v1/interview/evaluate-answer",
            json={
                "question": "What is REST API?",
                "answer": "REST is an architectural style for web services",
                "position": "Backend Developer"
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "evaluation" in data["data"]
    
    def test_complete_interview(self):
        """면접 완료 테스트"""
        response = client.post(
            "/api/v1/interview/complete-interview",
            json={
                "session_id": "test_session",
                "answers": [
                    {
                        "question": "What is Python?",
                        "answer": "Python is a programming language"
                    }
                ]
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True

# ==================== COVER LETTER TESTS ====================

class TestCoverLetterEndpoints:
    """자소서 엔드포인트 테스트"""
    
    def test_generate_questions(self):
        """자소서 질문 생성 테스트"""
        response = client.post(
            "/api/v1/cover-letter/generate-questions",
            json={
                "company": "Google",
                "position": "Software Engineer"
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
    
    def test_generate_complete(self):
        """완전한 자소서 생성 테스트"""
        response = client.post(
            "/api/v1/cover-letter/generate-complete",
            json={
                "company": "Microsoft",
                "position": "Data Engineer",
                "user_experience": "3 years of experience in data analysis"
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
    
    def test_search_context(self):
        """컨텍스트 검색 테스트"""
        response = client.get(
            "/api/v1/cover-letter/search-context",
            params={"query": "python developer", "limit": 3}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True

# ==================== TRANSLATION TESTS ====================

class TestTranslationEndpoints:
    """번역 엔드포인트 테스트"""
    
    def test_translate(self):
        """번역 테스트"""
        response = client.post(
            "/api/v1/translation/translate",
            json={
                "text": "Hello world",
                "target_language": "ko"
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        # 실제 응답 구조에 맞춰 수정
        assert "translation" in data["data"]
        assert "translated_text" in data["data"]["translation"]
    
    def test_get_templates(self):
        """템플릿 조회 테스트"""
        response = client.get("/api/v1/translation/templates")
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "templates" in data["data"]
    
    def test_batch_translate(self):
        """일괄 번역 테스트"""
        response = client.post(
            "/api/v1/translation/batch-translate",
            json={
                "texts": ["Hello", "World"],
                "target_language": "ko"
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True

# ==================== IMAGE TESTS ====================

class TestImageEndpoints:
    """이미지 엔드포인트 테스트"""
    
    def test_generate_image(self):
        """이미지 생성 테스트"""
        response = client.post(
            "/api/v1/image/generate",
            json={
                "prompt": "professional developer portrait",
                "style": "professional"
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "image_url" in data["data"]
    
    def test_generate_with_sentiment(self):
        """감정 기반 이미지 생성 테스트"""
        response = client.post(
            "/api/v1/image/generate-with-sentiment",
            json={
                "text": "I got a new job! So excited!",
                "style_preference": "celebration"
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
    
    def test_analyze_sentiment(self):
        """감정 분석 테스트"""
        response = client.post(
            "/api/v1/image/analyze-sentiment",
            json={
                "text": "I'm so happy today!"
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "primary_sentiment" in data["data"]
    
    def test_get_styles(self):
        """스타일 목록 조회 테스트"""
        response = client.get("/api/v1/image/styles")
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "styles" in data["data"]

# ==================== INTEGRATION TESTS ====================

class TestIntegration:
    """통합 테스트"""
    
    def test_complete_user_flow(self):
        """완전한 사용자 플로우 테스트"""
        user_id = "integration_user"
        
        # 1. 챗봇과 대화
        response = client.post(
            "/api/v1/chatbot/chat",
            json={"user_id": user_id, "message": "Hello, I need help"}
        )
        assert response.status_code == 200
        
        # 2. 추천 질문 확인
        response = client.get("/api/v1/chatbot/suggestions")
        assert response.status_code == 200
        
        # 3. 이미지 스타일 확인
        response = client.get("/api/v1/image/styles")
        assert response.status_code == 200
        
        # 4. 히스토리 확인
        response = client.get(f"/api/v1/chatbot/history/{user_id}")
        assert response.status_code == 200
        assert response.json()["data"]["total_messages"] >= 1

# ==================== ERROR HANDLING TESTS ====================

class TestErrorHandling:
    """에러 처리 테스트"""
    
    def test_invalid_endpoint(self):
        """잘못된 엔드포인트 테스트"""
        response = client.get("/api/v1/invalid/endpoint")
        assert response.status_code == 404
    
    def test_invalid_json(self):
        """잘못된 JSON 테스트"""
        response = client.post(
            "/api/v1/chatbot/chat",
            content="invalid json",
            headers={"Content-Type": "application/json"}
        )
        assert response.status_code == 422
    
    def test_missing_required_fields(self):
        """필수 필드 누락 테스트"""
        response = client.post(
            "/api/v1/chatbot/chat",
            json={"user_id": "test"}  # message 필드 누락
        )
        assert response.status_code == 422

# ==================== PERFORMANCE TESTS ====================

class TestPerformance:
    """성능 테스트"""
    
    def test_response_time(self):
        """응답 시간 테스트"""
        import time
        
        start = time.time()
        response = client.get("/health")
        end = time.time()
        
        assert response.status_code == 200
        assert (end - start) < 2.0  # 2초 이내 응답

if __name__ == "__main__":
    print("Running tests for working endpoints...")
    pytest.main([__file__, "-v"])