import pytest
from unittest.mock import Mock, patch, MagicMock
import sys
import os
from pathlib import Path

# 프로젝트 루트를 Python path에 추가
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Mock 환경 변수 설정
os.environ['OPENAI_API_KEY'] = 'test-api-key-for-testing'

class TestChatbotService:
    
    def setup_method(self):
        """각 테스트 메서드 실행 전 설정"""
        self.test_user_id = "test_user_123"
        self.test_message = "회원가입은 어떻게 하나요?"
    
    @patch('services.chatbot_service.OpenAI')
    @patch('services.chatbot_service.OpenAIEmbedding')
    @patch('services.chatbot_service.VectorStoreIndex')
    @patch('services.chatbot_service.SimpleDirectoryReader')
    def test_chatbot_service_initialization(self, mock_reader, mock_index, mock_embedding, mock_llm):
        """챗봇 서비스 초기화 테스트"""
        from services.chatbot_service import ChatbotService
        
        # Mock 설정
        mock_reader.return_value.load_data.return_value = [
            Mock(text="테스트 문서 내용")
        ]
        mock_index_instance = Mock()
        mock_index.from_documents.return_value = mock_index_instance
        mock_index_instance.storage_context.persist = Mock()
        
        # ChatbotService 인스턴스 생성
        service = ChatbotService()
        
        # 검증
        assert service.knowledge_base_path == "knowledge_base"
        assert service.index_path == "data/chatbot_index"
        assert service.user_sessions == {}
    
    @patch('services.chatbot_service.chatbot_service')
    def test_get_user_session_new_user(self, mock_service):
        """새 사용자 세션 생성 테스트"""
        from services.chatbot_service import ChatbotService
        
        service = ChatbotService.__new__(ChatbotService)
        service.user_sessions = {}
        
        # 새 사용자 세션 생성
        session = service.get_user_session(self.test_user_id)
        
        # 검증
        assert self.test_user_id in service.user_sessions
        assert "chat_history" in session
        assert session["chat_history"] == []
    
    @patch('services.chatbot_service.chatbot_service')
    def test_get_user_session_existing_user(self, mock_service):
        """기존 사용자 세션 조회 테스트"""
        from services.chatbot_service import ChatbotService
        
        service = ChatbotService.__new__(ChatbotService)
        service.user_sessions = {
            self.test_user_id: {
                "chat_history": [{"user": "test", "assistant": "test response"}],
                "created_at": "2024-01-01"
            }
        }
        
        # 기존 사용자 세션 조회
        session = service.get_user_session(self.test_user_id)
        
        # 검증
        assert len(session["chat_history"]) == 1
        assert session["chat_history"][0]["user"] == "test"
    
    @patch('services.chatbot_service.chatbot_service')
    def test_clear_user_session(self, mock_service):
        """사용자 세션 초기화 테스트"""
        from services.chatbot_service import ChatbotService
        
        service = ChatbotService.__new__(ChatbotService)
        service.user_sessions = {
            self.test_user_id: {"chat_history": ["test_data"]}
        }
        
        # 세션 초기화
        service.clear_user_session(self.test_user_id)
        
        # 검증
        assert self.test_user_id not in service.user_sessions
    
    @pytest.mark.asyncio
    @patch('services.chatbot_service.chatbot_service')
    async def test_chat_success(self, mock_service):
        """성공적인 채팅 응답 테스트"""
        from services.chatbot_service import ChatbotService
        
        service = ChatbotService.__new__(ChatbotService)
        service.user_sessions = {}
        
        # Mock chat engine
        mock_chat_engine = Mock()
        mock_response = Mock()
        mock_response.__str__ = Mock(return_value="회원가입은 메인 페이지에서 '회원가입' 버튼을 클릭하시면 됩니다.")
        mock_chat_engine.chat.return_value = mock_response
        service.chat_engine = mock_chat_engine
        service.get_user_session = Mock(return_value={"chat_history": []})
        
        # 채팅 호출
        result = await service.chat(self.test_user_id, self.test_message)
        
        # 검증
        assert result["success"] is True
        assert "회원가입은 메인 페이지에서" in result["response"]
        assert result["user_id"] == self.test_user_id
    
    @pytest.mark.asyncio
    @patch('services.chatbot_service.chatbot_service')
    async def test_chat_no_engine(self, mock_service):
        """채팅 엔진이 없을 때 테스트"""
        from services.chatbot_service import ChatbotService
        
        service = ChatbotService.__new__(ChatbotService)
        service.chat_engine = None
        
        # 채팅 호출
        result = await service.chat(self.test_user_id, self.test_message)
        
        # 검증
        assert result["success"] is False
        assert "챗봇 서비스를 이용할 수 없습니다" in result["response"]
        assert result["error"] == "chat_engine_not_available"
    
    @pytest.mark.asyncio
    @patch('services.chatbot_service.chatbot_service')
    async def test_chat_exception(self, mock_service):
        """채팅 중 예외 발생 테스트"""
        from services.chatbot_service import ChatbotService
        
        service = ChatbotService.__new__(ChatbotService)
        service.user_sessions = {}
        
        # Mock chat engine that raises exception
        mock_chat_engine = Mock()
        mock_chat_engine.chat.side_effect = Exception("Test exception")
        service.chat_engine = mock_chat_engine
        service.get_user_session = Mock(return_value={"chat_history": []})
        
        # 채팅 호출
        result = await service.chat(self.test_user_id, self.test_message)
        
        # 검증
        assert result["success"] is False
        assert "일시적인 오류가 발생했습니다" in result["response"]
        assert "Test exception" in result["error"]
    
    @pytest.mark.asyncio
    async def test_get_suggested_questions(self):
        """추천 질문 목록 테스트"""
        from services.chatbot_service import ChatbotService
        
        service = ChatbotService.__new__(ChatbotService)
        
        # 추천 질문 조회
        suggestions = await service.get_suggested_questions()
        
        # 검증
        assert isinstance(suggestions, list)
        assert len(suggestions) > 0
        assert "회원가입은 어떻게 하나요?" in suggestions
        assert "AI 면접 기능을 사용하려면 어떻게 해야 하나요?" in suggestions
    
    @pytest.mark.asyncio
    async def test_get_chat_categories(self):
        """채팅 카테고리 테스트"""
        from services.chatbot_service import ChatbotService
        
        service = ChatbotService.__new__(ChatbotService)
        
        # 카테고리 조회
        categories = await service.get_chat_categories()
        
        # 검증
        assert isinstance(categories, dict)
        assert "계정 관련" in categories
        assert "플랫폼 기능" in categories
        assert "증명서" in categories
        assert "기술 지원" in categories
        
        # 각 카테고리에 질문들이 있는지 확인
        for category, questions in categories.items():
            assert isinstance(questions, list)
            assert len(questions) > 0
    
    def test_get_chat_history_empty(self):
        """빈 채팅 히스토리 테스트"""
        from services.chatbot_service import ChatbotService
        
        service = ChatbotService.__new__(ChatbotService)
        service.user_sessions = {}
        service.get_user_session = Mock(return_value={"chat_history": []})
        
        # 히스토리 조회
        history = service.get_chat_history(self.test_user_id)
        
        # 검증
        assert isinstance(history, list)
        assert len(history) == 0
    
    def test_get_chat_history_with_data(self):
        """데이터가 있는 채팅 히스토리 테스트"""
        from services.chatbot_service import ChatbotService
        
        test_history = [
            {"user": "안녕하세요", "assistant": "안녕하세요! 무엇을 도와드릴까요?"},
            {"user": "회원가입 방법", "assistant": "회원가입은 메인 페이지에서..."}
        ]
        
        service = ChatbotService.__new__(ChatbotService)
        service.get_user_session = Mock(return_value={"chat_history": test_history})
        
        # 히스토리 조회
        history = service.get_chat_history(self.test_user_id)
        
        # 검증
        assert len(history) == 2
        assert history[0]["user"] == "안녕하세요"
        assert history[1]["user"] == "회원가입 방법"
    
    def test_get_health_status_healthy(self):
        """건강한 상태의 헬스체크 테스트"""
        from services.chatbot_service import ChatbotService
        
        service = ChatbotService.__new__(ChatbotService)
        service.chat_engine = Mock()  # 채팅 엔진이 있음
        service.index = Mock()  # 인덱스가 있음
        service.user_sessions = {"user1": {}, "user2": {}}
        service.knowledge_base_path = "knowledge_base"
        
        # 헬스 상태 조회
        health = service.get_health_status()
        
        # 검증
        assert health["chatbot_service"] == "healthy"
        assert health["index_status"] == "loaded"
        assert health["active_sessions"] == 2
        assert health["knowledge_base_path"] == "knowledge_base"
    
    def test_get_health_status_unhealthy(self):
        """비정상 상태의 헬스체크 테스트"""
        from services.chatbot_service import ChatbotService
        
        service = ChatbotService.__new__(ChatbotService)
        service.chat_engine = None  # 채팅 엔진이 없음
        service.index = None  # 인덱스가 없음
        service.user_sessions = {}
        service.knowledge_base_path = "knowledge_base"
        
        # 헬스 상태 조회
        health = service.get_health_status()
        
        # 검증
        assert health["chatbot_service"] == "unavailable"
        assert health["index_status"] == "not_loaded"
        assert health["active_sessions"] == 0
    
    @patch('services.chatbot_service.Path')
    @patch('services.chatbot_service.SimpleDirectoryReader')
    def test_create_index_no_documents(self, mock_reader, mock_path):
        """문서가 없을 때 인덱스 생성 테스트"""
        from services.chatbot_service import ChatbotService
        
        # Mock 설정
        mock_path.return_value.mkdir = Mock()
        mock_path.return_value.exists.return_value = True
        mock_reader.return_value.load_data.return_value = []
        
        service = ChatbotService.__new__(ChatbotService)
        service.knowledge_base_path = "test_kb"
        service.index_path = "test_index"
        
        # 모든 필수 메서드를 Mock으로 설정
        with patch('services.chatbot_service.VectorStoreIndex') as mock_index:
            mock_index_instance = Mock()
            mock_index_instance.storage_context.persist = Mock()
            mock_index.from_documents.return_value = mock_index_instance
            
            # 인덱스 생성
            service._create_index()
            
            # 검증: 빈 문서로도 인덱스가 생성되어야 함
            mock_index.from_documents.assert_called_once()

if __name__ == "__main__":
    pytest.main([__file__, "-v"])