import pytest
import json
from fastapi.testclient import TestClient
from unittest.mock import patch, AsyncMock
import asyncio

from main import app
from models.schemas import CoverLetterSection

client = TestClient(app)

class TestCoverLetterIntegration:
    
    def setup_method(self):
        """각 테스트 메서드 실행 전 설정"""
        self.company_name = "네이버"
        self.position = "백엔드 개발자"
        self.user_info = {
            "education": "컴퓨터공학과",
            "major": "컴퓨터공학",
            "skills": ["Java", "Spring", "MySQL", "Redis"],
            "experience": "웹 개발 3년"
        }

    @patch('services.openai_service.openai_service.generate_completion')
    @patch('services.rag_service.rag_service.get_context_for_section')
    def test_generate_interactive_questions_success(self, mock_rag, mock_openai):
        """RAG 기반 인터랙티브 질문 생성 성공 테스트"""
        # Given
        mock_rag.return_value = "지원동기 작성 가이드라인..."
        mock_openai.return_value = json.dumps({
            "questions": [
                "네이버에 지원하게 된 구체적인 계기는 무엇인가요?",
                "백엔드 개발자로서 네이버에서 이루고 싶은 목표는 무엇인가요?",
                "네이버의 어떤 기술이나 서비스에 관심이 있나요?",
                "이 포지션에서 본인만의 차별화된 가치는 무엇인가요?",
                "5년 후 네이버에서의 본인 모습을 어떻게 그리고 있나요?"
            ]
        })

        # When
        response = client.post(
            "/cover-letter/generate-questions",
            params={
                "section": CoverLetterSection.MOTIVATION.value,
                "company_name": self.company_name,
                "position": self.position
            }
        )

        # Then
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["message"] == "질문이 성공적으로 생성되었습니다"
        assert len(data["data"]["questions"]) == 5
        assert "네이버" in data["data"]["questions"][0]
        
        # Mock 호출 확인
        mock_rag.assert_called_once_with("motivation")
        mock_openai.assert_called_once()

    @patch('services.openai_service.openai_service.generate_completion')
    @patch('services.rag_service.rag_service.get_context_for_section')
    def test_generate_section_content_success(self, mock_rag, mock_openai):
        """RAG 기반 자소서 섹션 생성 성공 테스트"""
        # Given
        user_answers = [
            "네이버의 검색 기술에 관심이 있어서 지원했습니다.",
            "백엔드 개발자로서 대용량 트래픽 처리 경험을 쌓고 싶습니다.",
            "네이버 클라우드 플랫폼 개발에 기여하고 싶습니다."
        ]
        
        mock_rag.return_value = "지원동기 작성 시 STAR 기법을 활용하여..."
        mock_section_content = """
        네이버 백엔드 개발자 지원동기
        
        저는 대한민국 최고의 IT 기업인 네이버의 백엔드 개발자로 지원하게 되었습니다.
        
        특히 네이버의 검색 기술과 클라우드 플랫폼 개발에 깊은 관심을 가지고 있습니다.
        3년간의 웹 개발 경험을 통해 대용량 트래픽 처리와 시스템 최적화에 대한 
        실무 경험을 쌓았으며, 이를 바탕으로 네이버의 글로벌 서비스 안정성과 
        성능 향상에 기여하고 싶습니다.
        
        앞으로 네이버에서 혁신적인 기술을 통해 사용자에게 더 나은 경험을 
        제공하는 개발자로 성장하겠습니다.
        """
        mock_openai.return_value = mock_section_content

        # When
        response = client.post(
            "/cover-letter/generate-section",
            params={
                "section": CoverLetterSection.MOTIVATION.value,
                "company_name": self.company_name,
                "position": self.position
            },
            json={
                "user_answers": user_answers,
                "user_info": self.user_info
            }
        )

        # Then
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["message"] == "자소서 섹션이 성공적으로 생성되었습니다"
        assert "네이버" in data["data"]["content"]
        assert "백엔드 개발자" in data["data"]["content"]
        assert len(data["data"]["content"]) > 500  # 충분한 길이의 내용
        
        # Mock 호출 확인
        mock_rag.assert_called_once()
        mock_openai.assert_called_once()

    @patch('services.openai_service.openai_service.generate_completion')
    @patch('routers.cover_letter.cover_letter_service.generate_section_content')
    @patch('routers.cover_letter.cover_letter_service.generate_complete_cover_letter')
    @patch('routers.cover_letter.cover_letter_service.provide_feedback_and_suggestions')
    def test_generate_complete_cover_letter_success(self, mock_feedback, mock_complete, mock_section, mock_openai):
        """완전한 RAG 기반 자소서 생성 프로세스 테스트"""
        # Given
        request_data = {
            "user_id": "test@example.com",
            "company_name": self.company_name,
            "position": self.position,
            "sections": [
                CoverLetterSection.MOTIVATION.value,
                CoverLetterSection.STRENGTHS.value
            ],
            "user_info": self.user_info
        }

        # Mock 섹션 내용들
        mock_section.side_effect = [
            "네이버 백엔드 개발자 지원동기 내용...",  # motivation 섹션
            "백엔드 개발자로서의 핵심 역량과 장점..."   # strengths 섹션
        ]
        
        # Mock 완성된 자소서
        complete_content = """
        네이버 백엔드 개발자 자기소개서
        
        [지원동기]
        네이버 백엔드 개발자 지원동기 내용...
        
        [나의 장점]  
        백엔드 개발자로서의 핵심 역량과 장점...
        
        감사합니다.
        """
        mock_complete.return_value = complete_content
        
        # Mock 피드백
        feedback = {
            "overall_score": 88,
            "strengths": ["구체적인 기술 경험", "회사 적합성", "성장 의지"],
            "improvements": ["정량적 성과 지표 추가", "프로젝트 상세 설명"],
            "specific_feedback": "전반적으로 우수한 자기소개서입니다.",
            "suggestions": ["STAR 기법 활용", "개인 프로젝트 경험 추가"]
        }
        mock_feedback.return_value = feedback

        # When
        response = client.post("/cover-letter/generate-complete", json=request_data)

        # Then
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["message"] == "자기소개서가 성공적으로 생성되었습니다"
        
        # 자소서 응답 구조 확인
        cover_letter_data = data["data"]["cover_letter"]
        assert cover_letter_data["company_name"] == self.company_name
        assert cover_letter_data["position"] == self.position
        assert "지원동기" in cover_letter_data["content"]
        assert "나의 장점" in cover_letter_data["content"]
        
        # 완성된 내용 확인
        assert "complete_content" in data["data"]
        assert "네이버 백엔드 개발자" in data["data"]["complete_content"]
        
        # 피드백 확인
        assert "feedback" in data["data"]
        assert data["data"]["feedback"]["overall_score"] == 88
        assert len(data["data"]["feedback"]["strengths"]) == 3

        # Mock 호출 확인
        assert mock_section.call_count == 2  # motivation, strengths 섹션
        mock_complete.assert_called_once()
        mock_feedback.assert_called_once()

    @patch('services.openai_service.openai_service.generate_completion')
    def test_provide_cover_letter_feedback_success(self, mock_openai):
        """자소서 피드백 제공 성공 테스트"""
        # Given
        cover_letter_content = """
        저는 네이버의 백엔드 개발자로 지원합니다.
        Java와 Spring을 활용한 3년간의 개발 경험이 있으며,
        대용량 트래픽 처리와 시스템 최적화에 관심이 있습니다.
        네이버의 혁신적인 기술력과 글로벌 서비스에 기여하고 싶습니다.
        """
        
        feedback_response = {
            "overall_score": 78,
            "strengths": [
                "관련 기술 스택 언급",
                "실무 경험 기반 서술",
                "회사 비전 연결"
            ],
            "improvements": [
                "구체적인 프로젝트 성과 추가",
                "개인적 특성 더 부각",
                "차별화 포인트 강화"
            ],
            "specific_feedback": "기본기는 잘 갖춰져 있으나 더 구체적인 경험과 성과를 추가하면 좋겠습니다.",
            "suggestions": [
                "STAR 기법으로 경험 구체화",
                "정량적 성과 지표 포함",
                "개인 프로젝트 경험 추가"
            ]
        }
        
        mock_openai.return_value = json.dumps(feedback_response)

        # When
        response = client.post(
            "/cover-letter/feedback",
            json={"content": cover_letter_content}
        )

        # Then
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["message"] == "피드백이 성공적으로 생성되었습니다"
        
        feedback = data["data"]["feedback"]
        assert feedback["overall_score"] == 78
        assert 0 <= feedback["overall_score"] <= 100
        assert len(feedback["strengths"]) == 3
        assert len(feedback["improvements"]) == 3
        assert len(feedback["suggestions"]) == 3
        assert "기본기" in feedback["specific_feedback"]

        # Mock 호출 확인
        mock_openai.assert_called_once()

    @patch('services.rag_service.rag_service.search_similar_documents')
    @patch('services.rag_service.rag_service.get_context_for_section')
    def test_search_context_success(self, mock_context, mock_search):
        """RAG 컨텍스트 검색 성공 테스트"""
        # Given - 쿼리 있는 경우
        section = "motivation"
        query = "지원동기 작성법"
        top_k = 5
        
        mock_search_results = {
            "documents": [
                "지원동기 작성 가이드 1",
                "지원동기 작성 가이드 2",
                "지원동기 작성 가이드 3"
            ],
            "scores": [0.95, 0.87, 0.82]
        }
        mock_search.return_value = mock_search_results

        # When
        response = client.get(
            "/cover-letter/search-context",
            params={
                "section": section,
                "query": query,
                "top_k": top_k
            }
        )

        # Then
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["message"] == "컨텍스트 검색이 완료되었습니다"
        assert "results" in data["data"]
        assert len(data["data"]["results"]["documents"]) == 3

        # Mock 호출 확인
        mock_search.assert_called_once_with(query, section, top_k)

    @patch('services.rag_service.rag_service.get_context_for_section')
    def test_search_context_without_query(self, mock_context):
        """쿼리 없는 RAG 컨텍스트 검색 테스트"""
        # Given
        section = "strengths"
        context_text = "장점 작성 시 STAR 기법을 활용하여 구체적인 경험을 서술하세요..."
        mock_context.return_value = context_text

        # When
        response = client.get(
            "/cover-letter/search-context",
            params={"section": section}
        )

        # Then
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["data"]["results"]["context"] == context_text

        # Mock 호출 확인
        mock_context.assert_called_once_with(section, "", 3)

    def test_generate_questions_invalid_section(self):
        """잘못된 섹션으로 질문 생성 요청 시 오류 테스트"""
        # When
        response = client.post(
            "/cover-letter/generate-questions",
            params={
                "section": "invalid_section",
                "company_name": self.company_name,
                "position": self.position
            }
        )

        # Then
        assert response.status_code == 422  # Validation error

    @patch('services.openai_service.openai_service.generate_completion')
    def test_generate_questions_openai_error(self, mock_openai):
        """OpenAI 서비스 오류 시 처리 테스트"""
        # Given
        mock_openai.side_effect = Exception("OpenAI API 오류")

        # When
        response = client.post(
            "/cover-letter/generate-questions",
            params={
                "section": CoverLetterSection.MOTIVATION.value,
                "company_name": self.company_name,
                "position": self.position
            }
        )

        # Then
        assert response.status_code == 200  # APIResponse 구조상 200 반환
        data = response.json()
        assert data["success"] is False
        assert "질문 생성에 실패했습니다" in data["message"]

    @patch('services.openai_service.openai_service.generate_completion')
    def test_section_generation_with_empty_answers(self, mock_openai):
        """빈 답변으로 섹션 생성 요청 시 처리 테스트"""
        # Given
        mock_openai.return_value = "간단한 섹션 내용"

        # When
        response = client.post(
            "/cover-letter/generate-section",
            params={
                "section": CoverLetterSection.COMMUNICATION.value,
                "company_name": self.company_name,
                "position": self.position
            },
            json={
                "user_answers": [],
                "user_info": {}
            }
        )

        # Then
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["data"]["content"] == "간단한 섹션 내용"

    def test_feedback_with_empty_content(self):
        """빈 내용으로 피드백 요청 시 처리 테스트"""
        # When
        response = client.post(
            "/cover-letter/feedback",
            json={"content": ""}
        )

        # Then
        assert response.status_code == 422  # Validation error

    @pytest.mark.asyncio
    async def test_concurrent_section_generation(self):
        """동시 다발적 섹션 생성 요청 처리 테스트"""
        # Given
        sections = [
            CoverLetterSection.MOTIVATION.value,
            CoverLetterSection.GROWTH.value,
            CoverLetterSection.STRENGTHS.value,
            CoverLetterSection.COMMUNICATION.value
        ]
        
        user_answers = [
            "관련 경험이 있습니다",
            "구체적인 사례를 말씀드리겠습니다",
            "이를 통해 배운 점이 있습니다"
        ]

        with patch('services.openai_service.openai_service.generate_completion') as mock_openai:
            mock_openai.return_value = "생성된 섹션 내용"

            # When - 동시 요청
            tasks = []
            for section in sections:
                task = asyncio.create_task(
                    asyncio.to_thread(
                        client.post,
                        "/cover-letter/generate-section",
                        params={
                            "section": section,
                            "company_name": self.company_name,
                            "position": self.position
                        },
                        json={
                            "user_answers": user_answers,
                            "user_info": self.user_info
                        }
                    )
                )
                tasks.append(task)

            responses = await asyncio.gather(*tasks)

            # Then
            for response in responses:
                assert response.status_code == 200
                data = response.json()
                assert data["success"] is True
                assert "생성된 섹션 내용" in data["data"]["content"]

            # 모든 섹션에 대해 OpenAI 호출이 발생했는지 확인
            assert mock_openai.call_count == len(sections)

    @patch('services.openai_service.openai_service.generate_completion')
    def test_large_user_info_handling(self, mock_openai):
        """대용량 사용자 정보 처리 테스트"""
        # Given
        large_user_info = {
            "education": "컴퓨터공학과 학사, 소프트웨어공학 석사",
            "major": "컴퓨터공학",
            "skills": ["Java", "Spring", "MySQL", "Redis", "Docker", "Kubernetes", 
                      "AWS", "Jenkins", "Git", "Python", "JavaScript", "React"],
            "experience": "대기업 SI 회사에서 3년, 스타트업에서 2년, " * 50,  # 긴 텍스트
            "projects": ["프로젝트 1 상세 설명" * 100, "프로젝트 2 상세 설명" * 100],
            "certifications": ["정보처리기사", "SQLD", "AWS SAA", "Kubernetes CKA"]
        }
        
        mock_openai.return_value = "대용량 정보 기반 생성 내용"

        # When
        response = client.post(
            "/cover-letter/generate-section",
            params={
                "section": CoverLetterSection.STRENGTHS.value,
                "company_name": self.company_name,
                "position": self.position
            },
            json={
                "user_answers": ["저의 강점은 다양한 기술 경험입니다"],
                "user_info": large_user_info
            }
        )

        # Then
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["data"]["content"] is not None

        # OpenAI 호출 시 대용량 정보가 전달되었는지 확인
        mock_openai.assert_called_once()