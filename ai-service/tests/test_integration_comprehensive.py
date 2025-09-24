"""
JBD AI Service 통합 테스트
전체 워크플로우와 실제 사용 시나리오를 테스트합니다.
"""

import pytest
import asyncio
from fastapi.testclient import TestClient
from unittest.mock import Mock, patch, AsyncMock
import json
from datetime import datetime

# 테스트용 모의 응답 설정
MOCK_OPENAI_RESPONSES = {
    "sentiment_analysis": {
        "positive": {
            "choices": [{
                "message": {
                    "content": json.dumps({
                        "sentiment": "positive",
                        "score": 0.8,
                        "label": "긍정적",
                        "emotions": ["기쁨", "만족", "희망"]
                    })
                }
            }]
        },
        "negative": {
            "choices": [{
                "message": {
                    "content": json.dumps({
                        "sentiment": "negative", 
                        "score": -0.6,
                        "label": "부정적",
                        "emotions": ["슬픔", "걱정", "불안"]
                    })
                }
            }]
        }
    },
    "image_generation": {
        "data": [{
            "url": "https://example-azure-blob.com/generated-image-123.jpg"
        }]
    },
    "cover_letter_questions": {
        "choices": [{
            "message": {
                "content": json.dumps({
                    "questions": [
                        "네이버의 백엔드 포지션에 지원한 이유는 무엇인가요?",
                        "Spring Boot 프레임워크 경험에 대해 설명해주세요.",
                        "대용량 트래픽 처리 경험이 있다면 소개해주세요.",
                        "네이버의 기술 철학에 대해 어떻게 생각하시나요?",
                        "입사 후 어떤 기여를 하고 싶으신가요?"
                    ]
                })
            }
        }]
    },
    "cover_letter_content": {
        "choices": [{
            "message": {
                "content": """저는 네이버의 백엔드 개발자 포지션에 지원하게 되어 매우 기쁩니다. 
                
3년간의 Spring Boot 개발 경험을 통해 확장 가능한 마이크로서비스 아키텍처를 구축해왔으며, 
특히 대용량 트래픽 처리와 성능 최적화에 깊은 관심을 가지고 있습니다.

네이버의 글로벌 서비스 플랫폼에서 수억 명의 사용자에게 안정적인 서비스를 제공하는 것이 
저의 꿈이며, 이를 통해 개발자로서 한 단계 더 성장하고 싶습니다.

기술적 역량뿐만 아니라 사용자 중심의 사고와 팀워크를 바탕으로 네이버의 혁신에 
기여하겠습니다."""
            }
        }]
    }
}


@pytest.fixture
def mock_openai_client():
    """OpenAI 클라이언트 모킹"""
    with patch('services.openai_service.client') as mock_client:
        yield mock_client


@pytest.fixture  
def mock_dalle_client():
    """DALL-E 이미지 생성 클라이언트 모킹"""
    with patch('services.openai_service.dalle_client') as mock_client:
        yield mock_client


class TestSentimentAnalysisWorkflow:
    """감정 분석 워크플로우 테스트"""
    
    def test_positive_sentiment_analysis(self, client, mock_openai_client):
        """긍정적인 감정 분석 테스트"""
        # Given
        mock_openai_client.chat.completions.create.return_value = \
            MOCK_OPENAI_RESPONSES["sentiment_analysis"]["positive"]
            
        positive_text = "오늘 정말 기쁜 일이 있었어요! 드디어 취업에 성공했습니다."
        
        # When
        response = client.post(
            f"/api/v1/image/analyze-sentiment?text={positive_text}"
        )
        
        # Then
        assert response.status_code == 200
        data = response.json()
        
        assert data["success"] is True
        assert data["data"]["sentiment"] == "positive"
        assert data["data"]["score"] == 0.8
        assert data["data"]["label"] == "긍정적"
        assert "기쁨" in data["data"]["emotions"]
    
    def test_negative_sentiment_analysis(self, client, mock_openai_client):
        """부정적인 감정 분석 테스트"""
        # Given
        mock_openai_client.chat.completions.create.return_value = \
            MOCK_OPENAI_RESPONSES["sentiment_analysis"]["negative"]
            
        negative_text = "오늘 정말 힘든 하루였어요. 면접에서 떨어져서 너무 속상해요."
        
        # When
        response = client.post(
            f"/api/v1/image/analyze-sentiment?text={negative_text}"
        )
        
        # Then
        assert response.status_code == 200
        data = response.json()
        
        assert data["success"] is True
        assert data["data"]["sentiment"] == "negative"
        assert data["data"]["score"] == -0.6
        assert data["data"]["label"] == "부정적"
        assert "슬픔" in data["data"]["emotions"]


class TestImageGenerationWorkflow:
    """이미지 생성 워크플로우 테스트"""
    
    def test_basic_image_generation(self, client, mock_dalle_client):
        """기본 이미지 생성 테스트"""
        # Given
        mock_dalle_client.images.generate.return_value = \
            MOCK_OPENAI_RESPONSES["image_generation"]
        
        request_data = {
            "prompt": "A beautiful landscape with mountains and lake",
            "user_id": 1,
            "style": "realistic",
            "size": "1024x1024"
        }
        
        # When
        response = client.post("/api/v1/image/generate", json=request_data)
        
        # Then
        assert response.status_code == 200
        data = response.json()
        
        assert data["success"] is True
        assert data["data"]["image_url"] is not None
        assert "azure-blob" in data["data"]["image_url"]
    
    def test_sentiment_based_image_generation(self, client, mock_openai_client, mock_dalle_client):
        """감정 기반 이미지 생성 테스트"""
        # Given
        mock_openai_client.chat.completions.create.return_value = \
            MOCK_OPENAI_RESPONSES["sentiment_analysis"]["positive"]
        mock_dalle_client.images.generate.return_value = \
            MOCK_OPENAI_RESPONSES["image_generation"]
        
        # When
        response = client.post(
            "/api/v1/image/generate-with-sentiment",
            params={
                "prompt": "창의적인 프로젝트",
                "user_id": 1,
                "post_text": "오늘 멋진 작품을 완성했어요! 정말 뿌듯하네요.",
                "style": "digital-art",
                "size": "1024x1024"
            }
        )
        
        # Then
        assert response.status_code == 200
        data = response.json()
        
        assert data["success"] is True
        assert data["data"]["image_url"] is not None
        assert data["data"]["sentiment_analysis"] is not None
        assert data["data"]["sentiment_analysis"]["sentiment"] == "positive"
    
    def test_image_variations_generation(self, client, mock_dalle_client):
        """이미지 변형 생성 테스트"""
        # Given
        variations_response = {
            "data": [
                {"url": "https://example.com/variation1.jpg"},
                {"url": "https://example.com/variation2.jpg"},
                {"url": "https://example.com/variation3.jpg"}
            ]
        }
        mock_dalle_client.images.generate.return_value = variations_response
        
        # When
        response = client.post(
            "/api/v1/image/generate-variations",
            params={
                "prompt": "미래의 도시",
                "user_id": 1,
                "count": 3,
                "style": "futuristic"
            }
        )
        
        # Then
        assert response.status_code == 200
        data = response.json()
        
        assert data["success"] is True
        assert len(data["data"]["variations"]) == 3


class TestCoverLetterWorkflow:
    """자소서 생성 워크플로우 테스트"""
    
    def test_cover_letter_question_generation(self, client, mock_openai_client):
        """자소서 질문 생성 테스트"""
        # Given
        mock_openai_client.chat.completions.create.return_value = \
            MOCK_OPENAI_RESPONSES["cover_letter_questions"]
        
        # When
        response = client.post(
            "/api/v1/cover-letter/generate-questions",
            params={
                "section": "motivation",
                "company_name": "네이버",
                "position": "백엔드 개발자"
            }
        )
        
        # Then
        assert response.status_code == 200
        data = response.json()
        
        assert data["success"] is True
        assert len(data["data"]["questions"]) == 5
        assert "네이버" in data["data"]["questions"][0]
        assert "백엔드" in " ".join(data["data"]["questions"])
    
    def test_cover_letter_section_generation(self, client, mock_openai_client):
        """자소서 섹션 생성 테스트"""
        # Given
        mock_openai_client.chat.completions.create.return_value = \
            MOCK_OPENAI_RESPONSES["cover_letter_content"]
        
        request_data = {
            "user_answers": [
                "네이버의 글로벌 서비스에 매력을 느꼈습니다",
                "Spring Boot 프레임워크 경험이 풍부합니다",
                "대용량 트래픽 처리에 관심이 많습니다"
            ],
            "user_info": {
                "education": "컴퓨터공학과",
                "skills": ["Java", "Spring Boot", "MySQL"],
                "experience": "3년차 백엔드 개발자"
            }
        }
        
        # When
        response = client.post(
            "/api/v1/cover-letter/generate-section",
            params={
                "section": "motivation",
                "company_name": "네이버",
                "position": "백엔드 개발자"
            },
            json=request_data
        )
        
        # Then
        assert response.status_code == 200
        data = response.json()
        
        assert data["success"] is True
        assert data["data"]["content"] is not None
        assert "네이버" in data["data"]["content"]
        assert "Spring Boot" in data["data"]["content"]
    
    def test_complete_cover_letter_generation(self, client, mock_openai_client):
        """완전한 자소서 생성 테스트"""
        # Given
        mock_openai_client.chat.completions.create.side_effect = [
            MOCK_OPENAI_RESPONSES["cover_letter_content"],  # 각 섹션별 생성
            MOCK_OPENAI_RESPONSES["cover_letter_content"],
            MOCK_OPENAI_RESPONSES["cover_letter_content"],
            MOCK_OPENAI_RESPONSES["cover_letter_content"],
            MOCK_OPENAI_RESPONSES["cover_letter_content"],  # 완성본 통합
            {  # 피드백 생성
                "choices": [{
                    "message": {
                        "content": json.dumps({
                            "overall_score": 85,
                            "strengths": ["구체적인 경험 서술", "기업 맞춤 내용", "전문성 어필"],
                            "improvements": ["더 구체적인 수치 제시", "개인적 경험 추가"],
                            "specific_feedback": "전반적으로 우수한 자기소개서입니다.",
                            "suggestions": ["STAR 기법 활용", "구체적 성과 언급"]
                        })
                    }
                }]
            }
        ]
        
        request_data = {
            "user_id": 1,
            "company_name": "네이버",
            "position": "백엔드 개발자",
            "sections": ["motivation", "growth", "strengths", "communication"],
            "user_info": {
                "education": "컴퓨터공학과",
                "skills": ["Java", "Spring Boot"],
                "experience": "3년차 개발자"
            }
        }
        
        # When
        response = client.post("/api/v1/cover-letter/generate-complete", json=request_data)
        
        # Then
        assert response.status_code == 200
        data = response.json()
        
        assert data["success"] is True
        assert data["data"]["cover_letter"] is not None
        assert data["data"]["complete_content"] is not None
        assert data["data"]["feedback"] is not None
        assert data["data"]["feedback"]["overall_score"] == 85


class TestEndToEndScenarios:
    """전체 시나리오 테스트"""
    
    def test_community_post_creation_scenario(self, client, mock_openai_client, mock_dalle_client):
        """커뮤니티 게시글 생성 전체 시나리오"""
        # Given - 커뮤니티 게시글 내용
        post_content = "오늘 정말 멋진 프로젝트를 완성했어요! AI를 활용한 창의적인 작품입니다."
        
        # Mock 설정
        mock_openai_client.chat.completions.create.return_value = \
            MOCK_OPENAI_RESPONSES["sentiment_analysis"]["positive"]
        mock_dalle_client.images.generate.return_value = \
            MOCK_OPENAI_RESPONSES["image_generation"]
        
        # When 1: 감정 분석
        sentiment_response = client.post(
            f"/api/v1/image/analyze-sentiment?text={post_content}"
        )
        
        # Then 1: 감정 분석 검증
        assert sentiment_response.status_code == 200
        sentiment_data = sentiment_response.json()["data"]
        assert sentiment_data["sentiment"] == "positive"
        
        # When 2: 감정 기반 이미지 생성
        image_response = client.post(
            "/api/v1/image/generate-with-sentiment",
            params={
                "prompt": "창의적인 AI 프로젝트 완성",
                "user_id": 1,
                "post_text": post_content,
                "style": "digital-art"
            }
        )
        
        # Then 2: 이미지 생성 검증
        assert image_response.status_code == 200
        image_data = image_response.json()["data"]
        assert image_data["image_url"] is not None
        assert image_data["sentiment_analysis"]["sentiment"] == "positive"
        
        # 전체 워크플로우 검증
        assert sentiment_data["emotions"] is not None
        assert image_data["enhanced_prompt"] is not None
    
    def test_job_application_scenario(self, client, mock_openai_client):
        """취업 지원 전체 시나리오 (자소서 + 면접)"""
        # Given
        company = "카카오"
        position = "프론트엔드 개발자"
        
        mock_openai_client.chat.completions.create.side_effect = [
            MOCK_OPENAI_RESPONSES["cover_letter_questions"],  # 질문 생성
            MOCK_OPENAI_RESPONSES["cover_letter_content"],    # 섹션 생성  
            {  # 면접 질문 생성
                "choices": [{
                    "message": {
                        "content": json.dumps({
                            "questions": [
                                "React 프레임워크 경험에 대해 설명해주세요",
                                "상태 관리 라이브러리 사용 경험은 어떠신가요?",
                                "카카오의 UI/UX 철학에 대해 어떻게 생각하시나요?"
                            ]
                        })
                    }
                }]
            }
        ]
        
        # When 1: 자소서 질문 생성
        questions_response = client.post(
            "/api/v1/cover-letter/generate-questions",
            params={
                "section": "motivation",
                "company_name": company,
                "position": position
            }
        )
        
        # When 2: 자소서 섹션 생성
        section_response = client.post(
            "/api/v1/cover-letter/generate-section",
            params={
                "section": "motivation", 
                "company_name": company,
                "position": position
            },
            json={
                "user_answers": ["카카오톡의 혁신에 기여하고 싶습니다", "React 개발 경험이 풍부합니다"],
                "user_info": {"skills": ["React", "TypeScript", "Redux"]}
            }
        )
        
        # When 3: 면접 질문 생성
        interview_response = client.post(
            "/api/v1/interview/generate-questions",
            json={
                "interview_type": "technical",
                "user_id": 1,
                "job_position": position,
                "skills": ["React", "JavaScript"]
            }
        )
        
        # Then: 전체 워크플로우 검증
        assert questions_response.status_code == 200
        assert section_response.status_code == 200  
        assert interview_response.status_code == 200
        
        questions_data = questions_response.json()["data"]
        section_data = section_response.json()["data"]
        interview_data = interview_response.json()["data"]
        
        assert len(questions_data["questions"]) >= 3
        assert company in section_data["content"]
        assert len(interview_data["questions"]) >= 3
        assert "React" in str(interview_data["questions"])


class TestErrorHandlingAndEdgeCases:
    """오류 처리 및 엣지 케이스 테스트"""
    
    def test_invalid_sentiment_text(self, client):
        """유효하지 않은 감정 분석 텍스트 처리"""
        # When
        response = client.post("/api/v1/image/analyze-sentiment?text=")
        
        # Then
        assert response.status_code == 422  # Validation error
    
    def test_invalid_image_generation_params(self, client):
        """유효하지 않은 이미지 생성 파라미터 처리"""
        # When
        response = client.post(
            "/api/v1/image/generate",
            json={
                "prompt": "",  # 빈 프롬프트
                "user_id": -1,  # 유효하지 않은 사용자 ID
                "size": "invalid_size"  # 유효하지 않은 크기
            }
        )
        
        # Then
        assert response.status_code == 422
    
    def test_service_unavailable_handling(self, client, mock_openai_client):
        """외부 서비스 불가용 상황 처리"""
        # Given
        mock_openai_client.chat.completions.create.side_effect = Exception("Service unavailable")
        
        # When
        response = client.post(
            "/api/v1/image/analyze-sentiment?text=테스트 텍스트"
        )
        
        # Then
        assert response.status_code == 200  # 우리는 에러를 catch하고 성공=false로 응답
        data = response.json()
        assert data["success"] is False
        assert "error" in data


@pytest.fixture
def client():
    """테스트 클라이언트 설정"""
    from main import app
    return TestClient(app)


if __name__ == "__main__":
    pytest.main([__file__, "-v", "--tb=short"])