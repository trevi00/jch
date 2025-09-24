#!/usr/bin/env python3
"""
Comprehensive test suite for all AI services
"""

import pytest
import asyncio
from unittest.mock import Mock, patch, AsyncMock
from datetime import datetime
import json

# Test fixtures
@pytest.fixture
def mock_openai():
    """Mock OpenAI API responses"""
    with patch('openai.OpenAI') as mock:
        mock_instance = Mock()
        mock_instance.chat.completions.create.return_value = Mock(
            choices=[Mock(message=Mock(content="Test response"))]
        )
        mock_instance.images.generate.return_value = Mock(
            data=[Mock(url="https://test-image.jpg")]
        )
        mock.return_value = mock_instance
        yield mock_instance

@pytest.fixture
def client():
    """Create test client"""
    from fastapi.testclient import TestClient
    from main import app
    return TestClient(app)

# ==================== CHATBOT TESTS ====================

class TestChatbotAPI:
    """Test chatbot endpoints"""
    
    def test_chat_endpoint(self, client):
        """Test basic chat functionality"""
        response = client.post(
            "/api/v1/chatbot/chat",
            json={"user_id": "test_user", "message": "Hello"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert "response" in data["data"]
    
    def test_chat_empty_message(self, client):
        """Test chat with empty message"""
        response = client.post(
            "/api/v1/chatbot/chat",
            json={"user_id": "test_user", "message": ""}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is False
    
    def test_suggestions(self, client):
        """Test suggestions endpoint"""
        response = client.get("/api/v1/chatbot/suggestions")
        assert response.status_code == 200
        data = response.json()
        assert "suggestions" in data["data"]
        assert len(data["data"]["suggestions"]) > 0
    
    def test_categories(self, client):
        """Test categories endpoint"""
        response = client.get("/api/v1/chatbot/categories")
        assert response.status_code == 200
        data = response.json()
        assert "categories" in data["data"]
        assert isinstance(data["data"]["categories"], dict)
    
    def test_history(self, client):
        """Test chat history"""
        user_id = "test_history_user"
        
        # Send a message first
        client.post(
            "/api/v1/chatbot/chat",
            json={"user_id": user_id, "message": "Test message"}
        )
        
        # Get history
        response = client.get(f"/api/v1/chatbot/history/{user_id}")
        assert response.status_code == 200
        data = response.json()
        assert data["data"]["user_id"] == user_id
        assert "history" in data["data"]
    
    def test_delete_history(self, client):
        """Test delete history"""
        user_id = "test_delete_user"
        
        # Delete history
        response = client.delete(f"/api/v1/chatbot/history/{user_id}")
        assert response.status_code == 200
        
        # Verify it's deleted
        response = client.get(f"/api/v1/chatbot/history/{user_id}")
        data = response.json()
        assert data["data"]["total_messages"] == 0

# ==================== INTERVIEW TESTS ====================

class TestInterviewAPI:
    """Test interview endpoints"""
    
    @patch('services.interview_service.InterviewService')
    def test_generate_questions(self, mock_service, client):
        """Test interview question generation"""
        mock_service.return_value.generate_questions.return_value = {
            "questions": ["Question 1", "Question 2"]
        }
        
        response = client.post(
            "/api/v1/interview/generate-questions",
            json={
                "position": "Backend Developer",
                "interview_type": "technical",
                "count": 2
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert "questions" in data["data"]
    
    @patch('services.interview_service.InterviewService')
    def test_evaluate_answer(self, mock_service, client):
        """Test answer evaluation"""
        mock_service.return_value.evaluate_answer.return_value = {
            "score": 85,
            "feedback": "Good answer"
        }
        
        response = client.post(
            "/api/v1/interview/evaluate-answer",
            json={
                "question": "What is REST API?",
                "answer": "REST is..."
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert "feedback" in data["data"]

# ==================== COVER LETTER TESTS ====================

class TestCoverLetterAPI:
    """Test cover letter endpoints"""
    
    @patch('services.rag_service.RAGService')
    def test_generate_complete(self, mock_service, client):
        """Test complete cover letter generation"""
        mock_service.return_value.generate_cover_letter.return_value = {
            "cover_letter": "Generated cover letter content"
        }
        
        response = client.post(
            "/api/v1/cover-letter/generate-complete",
            json={
                "company": "Google",
                "position": "Software Engineer",
                "user_experience": "5 years of experience"
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert "cover_letter" in data["data"]
    
    def test_search_context(self, client):
        """Test context search"""
        response = client.get(
            "/api/v1/cover-letter/search-context",
            params={"query": "python developer"}
        )
        assert response.status_code == 200

# ==================== TRANSLATION TESTS ====================

class TestTranslationAPI:
    """Test translation endpoints"""
    
    @patch('services.translation_service.TranslationService')
    def test_translate(self, mock_service, client):
        """Test text translation"""
        mock_service.return_value.translate.return_value = {
            "translated_text": "Hello"
        }
        
        response = client.post(
            "/api/v1/translation/translate",
            json={
                "text": "안녕하세요",
                "target_language": "en"
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert "translated_text" in data["data"]
    
    def test_get_templates(self, client):
        """Test template retrieval"""
        response = client.get("/api/v1/translation/templates")
        assert response.status_code == 200

# ==================== IMAGE GENERATION TESTS ====================

class TestImageAPI:
    """Test image generation endpoints"""
    
    @patch('services.image_service.ImageService')
    def test_generate_image(self, mock_service, client):
        """Test image generation"""
        mock_service.return_value.generate_image.return_value = {
            "image_url": "https://generated-image.jpg"
        }
        
        response = client.post(
            "/api/v1/image/generate",
            json={
                "prompt": "professional developer",
                "style": "professional"
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert "image_url" in data["data"]
    
    def test_get_styles(self, client):
        """Test style list"""
        response = client.get("/api/v1/image/styles")
        assert response.status_code == 200
        data = response.json()
        assert "styles" in data["data"]

# ==================== INTEGRATION TESTS ====================

class TestIntegration:
    """Integration tests for complete workflows"""
    
    def test_complete_chat_flow(self, client):
        """Test complete chat conversation flow"""
        user_id = "integration_test_user"
        
        # Start conversation
        response1 = client.post(
            "/api/v1/chatbot/chat",
            json={"user_id": user_id, "message": "Hello"}
        )
        assert response1.status_code == 200
        
        # Continue conversation
        response2 = client.post(
            "/api/v1/chatbot/chat",
            json={"user_id": user_id, "message": "How do I sign up?"}
        )
        assert response2.status_code == 200
        
        # Check history
        history_response = client.get(f"/api/v1/chatbot/history/{user_id}")
        assert history_response.status_code == 200
        data = history_response.json()
        assert data["data"]["total_messages"] >= 2
        
        # Clean up
        delete_response = client.delete(f"/api/v1/chatbot/history/{user_id}")
        assert delete_response.status_code == 200
    
    @pytest.mark.asyncio
    async def test_concurrent_requests(self, client):
        """Test handling concurrent requests"""
        tasks = []
        for i in range(5):
            task = asyncio.create_task(
                asyncio.to_thread(
                    client.post,
                    "/api/v1/chatbot/chat",
                    json={"user_id": f"concurrent_{i}", "message": "Test"}
                )
            )
            tasks.append(task)
        
        responses = await asyncio.gather(*tasks)
        for response in responses:
            assert response.status_code == 200

# ==================== ERROR HANDLING TESTS ====================

class TestErrorHandling:
    """Test error handling"""
    
    def test_invalid_endpoint(self, client):
        """Test 404 for invalid endpoint"""
        response = client.get("/api/v1/invalid/endpoint")
        assert response.status_code == 404
    
    def test_invalid_json(self, client):
        """Test invalid JSON handling"""
        response = client.post(
            "/api/v1/chatbot/chat",
            data="invalid json"
        )
        assert response.status_code == 422
    
    def test_missing_required_fields(self, client):
        """Test missing required fields"""
        response = client.post(
            "/api/v1/chatbot/chat",
            json={"user_id": "test"}  # missing 'message'
        )
        assert response.status_code == 422

# ==================== PERFORMANCE TESTS ====================

class TestPerformance:
    """Performance tests"""
    
    def test_response_time(self, client):
        """Test response time is under threshold"""
        import time
        
        start = time.time()
        response = client.get("/health")
        end = time.time()
        
        assert response.status_code == 200
        assert (end - start) < 1.0  # Should respond within 1 second
    
    def test_large_text_handling(self, client):
        """Test handling of large text input"""
        large_text = "Test " * 1000  # 5000 characters
        
        response = client.post(
            "/api/v1/chatbot/chat",
            json={"user_id": "test", "message": large_text}
        )
        assert response.status_code == 200

# ==================== SECURITY TESTS ====================

class TestSecurity:
    """Security tests"""
    
    def test_sql_injection_attempt(self, client):
        """Test SQL injection prevention"""
        response = client.post(
            "/api/v1/chatbot/chat",
            json={
                "user_id": "'; DROP TABLE users; --",
                "message": "test"
            }
        )
        assert response.status_code == 200  # Should handle safely
    
    def test_xss_prevention(self, client):
        """Test XSS prevention"""
        response = client.post(
            "/api/v1/chatbot/chat",
            json={
                "user_id": "test",
                "message": "<script>alert('XSS')</script>"
            }
        )
        assert response.status_code == 200
        data = response.json()
        # Check that script tags are not executed
        assert "<script>" not in str(data)

# ==================== UTILS ====================

def run_all_tests():
    """Run all tests and generate report"""
    pytest.main([
        __file__,
        "-v",  # verbose
        "--tb=short",  # short traceback
        "--cov=.",  # coverage report
        "--cov-report=html",  # HTML coverage report
        "--cov-report=term-missing",  # terminal coverage with missing lines
    ])

if __name__ == "__main__":
    print("Running comprehensive test suite...")
    run_all_tests()