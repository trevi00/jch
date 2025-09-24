import pytest
from fastapi.testclient import TestClient
from unittest.mock import Mock, patch, AsyncMock
import sys
import os

# 프로젝트 루트를 Python path에 추가
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from main import app
from services.chatbot_service import chatbot_service

client = TestClient(app)

class TestChatbotAPI:
    
    def setup_method(self):
        """각 테스트 메서드 실행 전 설정"""
        # 테스트용 사용자 ID
        self.test_user_id = "test_user_123"
        self.test_message = "안녕하세요, 회원가입 방법을 알려주세요"
    
    def test_health_check(self):
        """기본 헬스체크 테스트"""
        response = client.get("/health")
        assert response.status_code == 200
        assert response.json()["status"] == "healthy"
    
    def test_root_endpoint(self):
        """루트 엔드포인트 테스트"""
        response = client.get("/")
        assert response.status_code == 200
        assert "잡았다 AI Service API" in response.json()["message"]
    
    @patch('services.chatbot_service.chatbot_service.chat')
    def test_chat_with_bot_success(self, mock_chat):
        """챗봇 대화 성공 테스트"""
        # Mock 응답 설정
        mock_chat.return_value = {
            "success": True,
            "response": "회원가입은 구글 OAuth 또는 이메일 회원가입 두 가지 방법이 있습니다.",
            "user_id": self.test_user_id,
            "message": self.test_message
        }
        
        # API 호출
        response = client.post("/api/v1/chatbot/chat", json={
            "user_id": self.test_user_id,
            "message": self.test_message
        })
        
        # 검증
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "응답이 생성되었습니다" in data["message"]
        assert "user_id" in data["data"]
        assert data["data"]["response"] == "회원가입은 구글 OAuth 또는 이메일 회원가입 두 가지 방법이 있습니다."
    
    @patch('services.chatbot_service.chatbot_service.chat')
    def test_chat_with_bot_failure(self, mock_chat):
        """챗봇 대화 실패 테스트"""
        # Mock 실패 응답 설정
        mock_chat.return_value = {
            "success": False,
            "response": "죄송합니다. 일시적인 오류가 발생했습니다.",
            "error": "service_error"
        }
        
        # API 호출
        response = client.post("/api/v1/chatbot/chat", json={
            "user_id": self.test_user_id,
            "message": self.test_message
        })
        
        # 검증
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is False
        assert "챗봇 응답 생성에 실패했습니다" in data["message"]
    
    def test_chat_empty_message(self):
        """빈 메시지 테스트"""
        response = client.post("/api/v1/chatbot/chat", json={
            "user_id": self.test_user_id,
            "message": ""
        })
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is False
        assert "메시지를 입력해주세요" in data["message"]
    
    def test_chat_whitespace_message(self):
        """공백만 있는 메시지 테스트"""
        response = client.post("/api/v1/chatbot/chat", json={
            "user_id": self.test_user_id,
            "message": "   "
        })
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is False
        assert "메시지를 입력해주세요" in data["message"]
    
    @patch('services.chatbot_service.chatbot_service.get_suggested_questions')
    def test_get_suggestions(self, mock_suggestions):
        """추천 질문 조회 테스트"""
        mock_suggestions.return_value = [
            "회원가입은 어떻게 하나요?",
            "AI 면접 기능을 사용하려면 어떻게 해야 하나요?",
            "자소서 생성 기능에 대해 알려주세요"
        ]
        
        response = client.get("/api/v1/chatbot/suggestions")
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert len(data["data"]["suggestions"]) == 3
        assert "회원가입은 어떻게 하나요?" in data["data"]["suggestions"]
    
    @patch('services.chatbot_service.chatbot_service.get_chat_categories')
    def test_get_categories(self, mock_categories):
        """카테고리 조회 테스트"""
        mock_categories.return_value = {
            "계정 관련": ["회원가입 방법", "비밀번호 찾기"],
            "플랫폼 기능": ["AI 면접 이용 방법", "자소서 생성 기능"]
        }
        
        response = client.get("/api/v1/chatbot/categories")
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "계정 관련" in data["data"]["categories"]
        assert "플랫폼 기능" in data["data"]["categories"]
    
    @patch('services.chatbot_service.chatbot_service.get_chat_history')
    def test_get_chat_history(self, mock_history):
        """채팅 히스토리 조회 테스트"""
        mock_history.return_value = [
            {
                "user": "안녕하세요",
                "assistant": "안녕하세요! 무엇을 도와드릴까요?",
                "timestamp": "2024-01-01 12:00:00"
            }
        ]
        
        response = client.get(f"/api/v1/chatbot/history/{self.test_user_id}")
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["data"]["user_id"] == self.test_user_id
        assert data["data"]["total_messages"] == 1
    
    @patch('services.chatbot_service.chatbot_service.clear_user_session')
    def test_clear_chat_history(self, mock_clear):
        """채팅 히스토리 삭제 테스트"""
        mock_clear.return_value = None
        
        response = client.delete(f"/api/v1/chatbot/history/{self.test_user_id}")
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "채팅 히스토리가 초기화되었습니다" in data["message"]
        assert data["data"]["user_id"] == self.test_user_id
    
    @patch('services.chatbot_service.chatbot_service.get_health_status')
    def test_chatbot_health(self, mock_health):
        """챗봇 헬스체크 테스트"""
        mock_health.return_value = {
            "chatbot_service": "healthy",
            "index_status": "loaded",
            "active_sessions": 0,
            "knowledge_base_path": "knowledge_base"
        }
        
        response = client.get("/api/v1/chatbot/health")
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["data"]["chatbot_service"] == "healthy"
    
    def test_quick_response_account(self):
        """빠른 응답 - 계정 관련 테스트"""
        response = client.post("/api/v1/chatbot/quick-response", params={
            "category": "account",
            "question_type": "signup",
            "user_id": self.test_user_id
        })
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "구글 OAuth" in data["data"]["response"]
    
    def test_quick_response_platform(self):
        """빠른 응답 - 플랫폼 기능 테스트"""
        response = client.post("/api/v1/chatbot/quick-response", params={
            "category": "platform",
            "question_type": "interview",
            "user_id": self.test_user_id
        })
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "AI 면접" in data["data"]["response"]
    
    def test_quick_response_technical(self):
        """빠른 응답 - 기술 지원 테스트"""
        response = client.post("/api/v1/chatbot/quick-response", params={
            "category": "technical",
            "question_type": "login",
            "user_id": self.test_user_id
        })
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "로그인 문제" in data["data"]["response"]
    
    def test_quick_response_unknown_category(self):
        """빠른 응답 - 알 수 없는 카테고리 테스트"""
        response = client.post("/api/v1/chatbot/quick-response", params={
            "category": "unknown",
            "question_type": "unknown",
            "user_id": self.test_user_id
        })
        
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "빠른 응답을 찾을 수 없습니다" in data["data"]["response"]

if __name__ == "__main__":
    pytest.main([__file__, "-v"])