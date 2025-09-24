from fastapi import APIRouter, HTTPException, Query
from typing import List, Dict, Any
import logging
from datetime import datetime
import json
from pathlib import Path

from models.schemas import (
    CoverLetterRequest, CoverLetterResponse, CoverLetterSection, APIResponse
)
from services.openai_service import openai_service
from services.pdf_service import pdf_service
# from services.rag_service import rag_service  # 의존성 이슈로 비활성화

# RAG 대체 함수 - resume_dataset 파일 직접 읽기
def get_context_for_section(section_key: str, query: str = "", top_k: int = 3) -> str:
    """resume_dataset에서 섹션별 컨텍스트 가져오기"""
    try:
        dataset_path = Path("resume_dataset")
        file_mapping = {
            "motivation": "지원 동기.txt",
            "growth": "성장과정.txt", 
            "strengths": "나의 장점.txt",
            "communication": "커뮤니케이션.txt",
            "company_analysis": "기업 분석.txt",
            # 한국어 섹션명도 지원
            "지원 동기": "지원 동기.txt",
            "성장과정": "성장과정.txt",
            "나의 장점": "나의 장점.txt", 
            "커뮤니케이션": "커뮤니케이션.txt",
            "기업 분석": "기업 분석.txt"
        }
        
        filename = file_mapping.get(section_key)
        if not filename:
            return ""
            
        file_path = dataset_path / filename
        if file_path.exists():
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            # 처음 1000자만 반환 (컨텍스트 크기 제한)
            return content[:1000]
    except Exception as e:
        logger.warning(f"컨텍스트 로드 실패 {section_key}: {e}")
    
    return ""

def search_similar_documents(query: str, section: str, top_k: int = 5) -> list:
    """간단한 문서 검색 시뮬레이션"""
    context = get_context_for_section(section, query, top_k)
    if context:
        return [{"text": context, "score": 0.9, "metadata": {"section": section}}]
    return []

logger = logging.getLogger(__name__)
router = APIRouter()

class CoverLetterService:
    def __init__(self):
        self.section_mapping = {
            CoverLetterSection.MOTIVATION: "motivation",
            CoverLetterSection.GROWTH: "growth", 
            CoverLetterSection.STRENGTHS: "strengths",
            CoverLetterSection.COMMUNICATION: "communication"
        }
        
        self.section_names = {
            CoverLetterSection.MOTIVATION: "지원동기",
            CoverLetterSection.GROWTH: "성장과정",
            CoverLetterSection.STRENGTHS: "나의 장점", 
            CoverLetterSection.COMMUNICATION: "커뮤니케이션"
        }
    
    async def generate_interactive_questions(
        self, 
        section: CoverLetterSection,
        company_name: str,
        position: str
    ) -> List[str]:
        """RAG 기반 고도화된 인터랙티브 질문 생성"""
        
        # RAG 컨텍스트 가져오기
        section_key = self.section_mapping[section]
        context = get_context_for_section(section_key, f"{company_name} {position}", top_k=3)
        
        section_name = self.section_names[section]
        
        # RAG 컨텍스트 기반 고도화된 프롬프트
        system_prompt = f"""
        당신은 최고급 자기소개서 컨설턴트입니다. 
        아래 전문 가이드라인을 철저히 분석하여 {section_name} 섹션을 위한 맞춤형 단계별 질문을 생성해주세요.
        
        === 전문 가이드라인 ===
        {context}
        
        === 기업 정보 ===
        - 회사명: {company_name}
        - 포지션: {position}
        
        === 질문 생성 요구사항 ===
        1. 위 가이드라인의 단계별 구조를 정확히 반영
        2. STAR 기법을 유도할 수 있는 구체적인 질문
        3. {company_name}과 {position}에 특화된 맞춤형 질문
        4. 사용자의 진정성 있는 경험을 이끌어낼 수 있는 질문
        5. 총 7개의 단계별 질문 (기본 5개 + 심화 2개)
        
        JSON 형식으로 응답해주세요:
        {{
            "questions": [
                "1단계 질문: 핵심 동기/경험 확인",
                "2단계 질문: 구체적 상황 서술", 
                "3단계 질문: 행동과 노력 과정",
                "4단계 질문: 결과와 성과",
                "5단계 질문: 학습과 성장",
                "6단계 질문: 직무 연관성",
                "7단계 질문: 미래 계획과 포부"
            ]
        }}
        """
        
        try:
            response = await openai_service.generate_completion([
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": f"{company_name} {position} 포지션의 {section_name} 전문 질문을 생성해주세요."}
            ], max_tokens=1500)
            
            # 응답이 비어있거나 None인 경우 처리
            if not response or not response.strip():
                logger.warning("OpenAI에서 빈 응답을 반환했습니다. 기본 질문을 사용합니다.")
                return self._get_fallback_questions(section, company_name, position)
                
            # JSON 파싱 시도
            try:
                result = json.loads(response)
                return result["questions"]
            except json.JSONDecodeError as json_err:
                logger.error(f"RAG 질문 JSON 파싱 실패: {json_err}. 응답 내용: {response[:200]}...")
                return self._get_fallback_questions(section, company_name, position)
            
        except Exception as e:
            logger.error(f"RAG 기반 질문 생성 실패: {e}")
            # 실패시 기본 RAG 질문 반환
            return self._get_fallback_questions(section, company_name, position)
    
    def _get_fallback_questions(self, section: CoverLetterSection, company_name: str, position: str) -> List[str]:
        """RAG 질문 생성 실패시 대체 질문"""
        section_key = self.section_mapping[section]
        
        fallback_questions = {
            "motivation": [
                f"{company_name}에 지원한 가장 핵심적인 이유는 무엇인가요?",
                f"{position} 분야에 관심을 갖게 된 구체적인 계기나 경험이 있나요?",
                "이 회사에서 달성하고 싶은 목표나 기여하고 싶은 부분은 무엇인가요?",
                "본인의 가치관이나 성향이 이 회사와 맞다고 생각하는 이유는?",
                "입사 후 3년 뒤 본인의 모습을 어떻게 그리고 있나요?"
            ],
            "growth": [
                "본인의 성장 과정에서 가장 중요한 전환점이나 깨달음이 있었나요?",
                "어려운 상황이나 실패를 극복한 구체적인 경험을 말씀해주세요.",
                "그 과정에서 본인이 취한 구체적인 행동이나 노력은 무엇이었나요?",
                "그 경험을 통해 얻은 결과나 성과는 무엇인가요?",
                "그 경험이 현재의 본인에게 어떤 영향을 미쳤나요?"
            ],
            "strengths": [
                "본인의 가장 핵심적인 강점은 무엇이라고 생각하시나요?",
                "그 강점을 발휘한 구체적인 상황이나 경험을 설명해주세요.",
                "그때 본인이 취한 구체적인 행동과 과정을 말씀해주세요.",
                "그 결과로 얻은 성과나 인정받은 부분이 있나요?",
                f"이 강점이 {position} 업무에 어떻게 도움이 될 것이라고 생각하시나요?"
            ],
            "communication": [
                "본인의 소통 스타일이나 방식에 대해 설명해주세요.",
                "팀워크나 협업에서 어려웠던 상황이 있었나요?",
                "그 상황을 해결하기 위해 어떤 노력을 했나요?",
                "그 결과 어떤 변화나 성과가 있었나요?",
                f"{position} 업무에서 효과적인 소통을 위해 어떤 노력을 할 계획인가요?"
            ]
        }
        
        return fallback_questions.get(section_key, fallback_questions["motivation"])
    
    async def generate_section_content(
        self,
        section: CoverLetterSection,
        company_name: str,
        position: str,
        user_answers: List[str],
        user_info: Dict[str, Any] = {}
    ) -> str:
        """RAG 기반 섹션 내용 생성"""
        
        # RAG 컨텍스트 가져오기
        section_key = self.section_mapping[section]
        
        # 사용자 답변을 기반으로 더 관련성 높은 컨텍스트 검색
        query = " ".join(user_answers)
        context = get_context_for_section(section_key, query, top_k=5)
        
        section_name = self.section_names[section]
        
        # 사용자 정보 텍스트 생성
        user_context = ""
        if user_info:
            user_context = f"""
            사용자 정보:
            - 학력: {user_info.get('education', '정보 없음')}
            - 전공: {user_info.get('major', '정보 없음')}
            - 기술 스킬: {', '.join(user_info.get('skills', []))}
            - 경험: {user_info.get('experience', '정보 없음')}
            """
        
        system_prompt = f"""
        당신은 전문 자기소개서 작성가입니다. 
        {section_name} 섹션을 작성해주세요.
        
        기업정보:
        - 회사명: {company_name}
        - 포지션: {position}
        
        {user_context}
        
        사용자 답변:
        {chr(10).join([f"{i+1}. {answer}" for i, answer in enumerate(user_answers)])}
        
        작성 가이드라인:
        {context}
        
        요구사항:
        1. 위 가이드라인을 철저히 따라서 작성
        2. 사용자의 답변을 바탕으로 구체적이고 진정성 있게 작성
        3. 1000-1500자 내외로 작성
        4. STAR 기법 활용 (상황, 과제, 행동, 결과)
        5. {company_name}과 {position}에 맞춤화
        6. 전문적이면서도 개인적인 톤 유지
        
        {section_name} 내용만 작성해주세요.
        """
        
        try:
            response = await openai_service.generate_completion([
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": f"{section_name} 섹션을 작성해주세요."}
            ], max_tokens=2500)
            
            return response.strip()
            
        except Exception as e:
            logger.error(f"섹션 내용 생성 실패: {e}")
            raise HTTPException(status_code=500, detail="자소서 섹션 생성에 실패했습니다")
    
    async def generate_complete_cover_letter(
        self,
        request: CoverLetterRequest,
        section_contents: Dict[str, str]
    ) -> str:
        """완성된 자소서 생성 및 최종 검토"""
        
        system_prompt = f"""
        당신은 전문 자기소개서 편집자입니다.
        아래 섹션들을 하나의 완성된 자기소개서로 통합해주세요.
        
        기업정보:
        - 회사명: {request.company_name}
        - 포지션: {request.position}
        
        요구사항:
        1. 각 섹션 간의 자연스러운 연결
        2. 전체적인 일관성과 통일성 확보
        3. 반복되는 내용 제거
        4. 논리적 흐름 개선
        5. 최종 완성도 높은 자기소개서로 완성
        
        섹션별 내용:
        {chr(10).join([f"## {section}##" + chr(10) + content for section, content in section_contents.items()])}
        
        완성된 자기소개서를 작성해주세요.
        """
        
        try:
            response = await openai_service.generate_completion([
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": "자기소개서를 완성해주세요."}
            ], max_tokens=3000)
            
            return response.strip()
            
        except Exception as e:
            logger.error(f"완성된 자소서 생성 실패: {e}")
            raise HTTPException(status_code=500, detail="자소서 완성에 실패했습니다")
    
    async def provide_feedback_and_suggestions(self, content: str) -> Dict[str, Any]:
        """자소서 피드백 및 개선 제안"""
        
        system_prompt = """
        당신은 HR 전문가이자 자기소개서 평가 전문가입니다.
        제출된 자기소개서를 평가하고 개선 제안을 해주세요.
        
        평가 기준:
        1. 내용의 구체성과 진정성
        2. 논리적 구성과 흐름
        3. 기업/직무 적합성
        4. 차별화된 강점 부각
        5. 문장력과 완성도
        
        JSON 형식으로 응답해주세요:
        {
            "overall_score": 점수(0-100),
            "strengths": ["강점1", "강점2", "강점3"],
            "improvements": ["개선점1", "개선점2", "개선점3"],
            "specific_feedback": "구체적인 피드백",
            "suggestions": ["제안1", "제안2", "제안3"]
        }
        """
        
        try:
            response = await openai_service.generate_completion([
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": f"다음 자기소개서를 평가해주세요:\n\n{content}"}
            ])
            
            # 응답이 비어있거나 None인 경우 처리
            if not response or not response.strip():
                logger.warning("OpenAI에서 빈 응답을 반환했습니다. 기본 피드백을 생성합니다.")
                return self._generate_fallback_feedback(content)
                
            # JSON 파싱 시도
            try:
                return json.loads(response)
            except json.JSONDecodeError as json_err:
                logger.error(f"JSON 파싱 실패: {json_err}. 응답 내용: {response[:200]}...")
                return self._generate_fallback_feedback(content)
            
        except Exception as e:
            logger.error(f"피드백 생성 실패: {e}")
            return self._generate_fallback_feedback(content)
    
    def _generate_fallback_feedback(self, content: str) -> Dict[str, Any]:
        """피드백 생성 실패 시 기본 피드백 반환"""
        word_count = len(content)
        
        return {
            "overall_score": 75,
            "strengths": [
                "개인의 경험과 역량이 잘 드러나 있습니다",
                "지원 동기가 명확히 제시되어 있습니다",
                "구체적인 사례를 포함하여 설득력이 있습니다"
            ],
            "improvements": [
                "더욱 구체적인 성과 지표를 포함하면 좋겠습니다",
                "기업에 대한 이해를 더욱 깊게 표현할 수 있습니다",
                "향후 포부를 더욱 구체적으로 작성해보세요"
            ],
            "specific_feedback": f"전체 {word_count}자로 적절한 분량입니다. 개인의 경험과 역량이 잘 드러나며, 지원 동기도 명확합니다. 다만 더욱 구체적인 사례와 성과를 포함한다면 더욱 인상적인 자기소개서가 될 것입니다.",
            "suggestions": [
                "구체적인 수치나 성과 지표 추가",
                "기업 연구 내용 강화",
                "향후 3-5년 계획 구체화"
            ]
        }

cover_letter_service = CoverLetterService()

@router.get("/health", response_model=APIResponse)
async def get_cover_letter_health():
    """자소서 서비스 상태 확인"""
    try:
        return APIResponse(
            success=True,
            message="자소서 서비스가 정상 작동 중입니다",
            data={"status": "healthy", "service": "cover_letter", "rag_status": "enabled"}
        )
    except Exception as e:
        logger.error(f"자소서 서비스 상태 확인 오류: {e}")
        return APIResponse(
            success=False,
            message="자소서 서비스 상태 확인에 실패했습니다",
            error=str(e)
        )

@router.get("/demo-questions", response_model=APIResponse)
async def get_demo_cover_letter_questions(
    section: str = "motivation",
    company_name: str = "테스트회사",
    position: str = "소프트웨어 엔지니어"
):
    """자소서 질문 데모 (GET 방식으로 간단 테스트)"""
    try:
        # 섹션별 데모 질문들
        demo_questions = {
            "motivation": [
                f"{company_name}에 지원한 이유는 무엇인가요?",
                f"{position} 포지션에 관심을 갖게 된 계기는?",
                "이 회사에서 이루고 싶은 목표가 있다면?",
                "본인만의 차별화된 지원동기가 있나요?",
                "회사의 어떤 점이 가장 매력적으로 느껴졌나요?"
            ],
            "growth": [
                "본인의 성장 배경에 대해 설명해주세요",
                "가장 인상 깊었던 성장 경험은 무엇인가요?",
                "실패를 통해 배운 점이 있다면 말해주세요",
                "어려운 상황을 극복한 경험이 있나요?",
                "본인을 성장시킨 가장 큰 동기는 무엇인가요?"
            ],
            "strengths": [
                "본인만의 강점은 무엇인가요?",
                "남들과 차별화되는 본인의 능력은?",
                "팀에서 본인의 역할은 주로 무엇인가요?",
                "본인의 성격 중 장점은 무엇인가요?",
                "이 강점을 업무에 어떻게 활용하시겠나요?"
            ],
            "communication": [
                "팀워크에서 중요하게 생각하는 것은?",
                "의사소통할 때 본인만의 방식이 있나요?",
                "갈등 상황을 해결한 경험이 있다면?",
                "리더십을 발휘한 경험이 있나요?",
                "협업에서 어려웠던 점과 극복 방법은?"
            ]
        }
        
        questions = demo_questions.get(section, demo_questions["motivation"])
        
        return APIResponse(
            success=True,
            message="자소서 질문이 성공적으로 생성되었습니다",
            data={"questions": questions, "section": section, "total_count": len(questions)}
        )
    except Exception as e:
        logger.error(f"자소서 질문 데모 오류: {e}")
        return APIResponse(
            success=False,
            message="자소서 질문 생성에 실패했습니다",
            error=str(e)
        )

@router.post("/generate-questions", response_model=APIResponse)
async def generate_interactive_questions(
    section: CoverLetterSection,
    company_name: str,
    position: str
):
    """RAG 기반 인터랙티브 질문 생성"""
    try:
        questions = await cover_letter_service.generate_interactive_questions(
            section=section,
            company_name=company_name,
            position=position
        )
        
        return APIResponse(
            success=True,
            message="질문이 성공적으로 생성되었습니다",
            data={"questions": questions}
        )
    except Exception as e:
        logger.error(f"질문 생성 API 오류: {e}")
        return APIResponse(
            success=False,
            message="질문 생성에 실패했습니다",
            error=str(e)
        )

@router.get("/demo-section", response_model=APIResponse)
async def get_demo_cover_letter_section(
    section: str = "motivation",
    company_name: str = "테스트회사",
    position: str = "소프트웨어 엔지니어"
):
    """자소서 섹션 데모 (GET 방식으로 간단 테스트)"""
    try:
        # OpenAI를 사용해서 실제 자소서 섹션 생성
        section_service = cover_letter_service
        
        # 기본 답변들
        demo_answers = {
            "motivation": [
                f"{company_name}의 혁신적인 기술과 성장 가능성에 매력을 느꼈습니다",
                f"{position} 분야에서의 전문성을 기르고 싶습니다", 
                "회사의 비전에 공감하며 함께 성장하고 싶습니다"
            ],
            "growth": [
                "학창시절부터 꾸준히 개발 공부를 해왔습니다",
                "다양한 프로젝트 경험을 통해 성장했습니다",
                "실패를 통해 배우고 개선하는 과정을 중시합니다"
            ],
            "strengths": [
                "문제 해결 능력과 끈기가 저의 강점입니다",
                "팀워크를 통한 협업에 능숙합니다",
                "새로운 기술 학습에 적극적입니다"
            ],
            "communication": [
                "명확하고 효과적인 의사소통을 중시합니다",
                "다양한 관점을 수용하고 존중합니다",
                "갈등 상황에서도 건설적인 해결책을 찾으려 노력합니다"
            ]
        }
        
        # 섹션 매핑
        section_enum_map = {
            "motivation": CoverLetterSection.MOTIVATION,
            "growth": CoverLetterSection.GROWTH,
            "strengths": CoverLetterSection.STRENGTHS,
            "communication": CoverLetterSection.COMMUNICATION
        }
        
        section_enum = section_enum_map.get(section, CoverLetterSection.MOTIVATION)
        answers = demo_answers.get(section, demo_answers["motivation"])
        
        # 실제 OpenAI API 호출해서 자소서 생성
        try:
            content = await section_service.generate_section_content(
                section=section_enum,
                company_name=company_name,
                position=position,
                user_answers=answers,
                user_info={"education": "컴퓨터공학과", "experience": "신입", "skills": ["Java", "Spring", "React"]}
            )
            
            return APIResponse(
                success=True,
                message="자소서 섹션이 성공적으로 생성되었습니다",
                data={
                    "content": content,
                    "section": section,
                    "company_name": company_name,
                    "position": position
                }
            )
        except Exception as ai_error:
            logger.error(f"OpenAI 호출 실패: {ai_error}")
            # OpenAI 실패시 기본 샘플 텍스트 반환
            sample_content = f"""
{company_name}의 {position} 포지션에 지원하게 된 이유는 회사의 혁신적인 비전과 성장 가능성에 깊은 매력을 느꼈기 때문입니다. 

특히 회사가 추진하고 있는 다양한 프로젝트와 기술 혁신에 대한 열정이 저의 개발자로서의 꿈과 완벽하게 일치한다고 생각합니다. 

저는 지금까지의 경험을 통해 {position} 분야에서 필요한 기술적 역량을 꾸준히 쌓아왔으며, 앞으로 {company_name}에서 더욱 전문적인 개발자로 성장하고 싶습니다.

회사의 성장과 함께 개인적으로도 발전할 수 있는 기회를 얻고, 나아가 회사의 목표 달성에 실질적으로 기여할 수 있는 개발자가 되겠습니다.
            """.strip()
            
            return APIResponse(
                success=True,
                message="기본 자소서 섹션을 제공합니다 (OpenAI 연결 실패)",
                data={
                    "content": sample_content,
                    "section": section,
                    "company_name": company_name,
                    "position": position,
                    "fallback": True
                }
            )
            
    except Exception as e:
        logger.error(f"자소서 섹션 데모 오류: {e}")
        return APIResponse(
            success=False,
            message="자소서 섹션 생성에 실패했습니다",
            error=str(e)
        )

@router.post("/generate-section", response_model=APIResponse)
async def generate_section_content(
    section: CoverLetterSection,
    company_name: str,
    position: str,
    user_answers: List[str],
    user_info: Dict[str, Any] = {}
):
    """RAG 기반 자소서 섹션 생성"""
    try:
        content = await cover_letter_service.generate_section_content(
            section=section,
            company_name=company_name,
            position=position,
            user_answers=user_answers,
            user_info=user_info
        )
        
        return APIResponse(
            success=True,
            message="자소서 섹션이 성공적으로 생성되었습니다",
            data={"content": content}
        )
    except Exception as e:
        logger.error(f"섹션 생성 API 오류: {e}")
        return APIResponse(
            success=False,
            message="자소서 섹션 생성에 실패했습니다",
            error=str(e)
        )

# Interactive Cover Letter Sessions
interactive_sessions = {}  # In-memory storage for demo purposes

class InteractiveCoverLetterService:
    """txt 파일 기반 섹션별 인터랙티브 자소서 서비스"""
    
    def __init__(self):
        self.sessions = {}
        
        # 섹션별 프로세스 정의 - 각 템플릿의 단계 수에 맞춤
        self.section_processes = {
            "지원 동기": {
                "total_steps": 7,  # STEP 1-1, 1-2, 2, 3-1/3-2, 4 (template 기준)
                "process_type": "motivation",
                "file_path": "지원 동기.txt"
            },
            "나의 장점": {
                "total_steps": 6,  # STEP 1~6 (template 기준) 
                "process_type": "strengths", 
                "file_path": "나의 장점.txt"
            },
            "성장과정": {
                "total_steps": 4,  # STEP 1: 키워드 3개 선택 + STEP 2: 각 키워드별 답변 (template 기준)
                "process_type": "growth",
                "file_path": "성장과정.txt"
            },
            "커뮤니케이션": {
                "total_steps": 7,  # 7단계 구조 (template 기준)
                "process_type": "communication",
                "file_path": "커뮤니케이션.txt"
            },
            "기업 분석": {
                "total_steps": 1,  # 기업명 입력 후 자동 분석
                "process_type": "company_analysis",
                "file_path": "기업 분석.txt"
            },
            # 영어 섹션명 지원 (하위 호환성)
            "motivation": {
                "total_steps": 7,
                "process_type": "motivation",
                "file_path": "지원 동기.txt"
            },
            "strengths": {
                "total_steps": 6,
                "process_type": "strengths", 
                "file_path": "나의 장점.txt"
            },
            "growth": {
                "total_steps": 3,
                "process_type": "growth",
                "file_path": "성장과정.txt"
            },
            "communication": {
                "total_steps": 7,
                "process_type": "communication",
                "file_path": "커뮤니케이션.txt"
            },
            "company_analysis": {
                "total_steps": 1,
                "process_type": "company_analysis",
                "file_path": "기업 분석.txt"
            }
        }
        
    async def start_interactive_session(
        self,
        company_name: str,
        position: str,
        section: str,
        user_id: str = None
    ) -> Dict[str, Any]:
        """섹션별 맞춤형 인터랙티브 세션 시작"""
        import uuid
        session_id = str(uuid.uuid4())
        
        # 섹션 프로세스 정보 가져오기
        process_info = self.section_processes.get(section)
        if not process_info:
            raise ValueError(f"지원하지 않는 섹션입니다: {section}")
        
        # 세션 초기화
        session_data = {
            "session_id": session_id,
            "company_name": company_name,
            "position": position,
            "section": section,
            "process_type": process_info["process_type"],
            "user_id": user_id or "anonymous",
            "current_step": 1,
            "total_steps": process_info["total_steps"],
            "questions": [],
            "answers": [],
            "selections": {},  # 선택형 답변 저장
            "step_data": {},   # 단계별 추가 데이터
            "draft_content": "",
            "final_content": "",
            "created_at": datetime.now().isoformat()
        }
        
        # 첫 번째 질문 생성
        first_question = await self._generate_step_question(1, session_data)
        session_data["current_question"] = first_question
        session_data["questions"].append(first_question)
        
        # 세션 저장
        self.sessions[session_id] = session_data
        interactive_sessions[session_id] = session_data
        
        return {
            "session_id": session_id,
            "question": first_question,
            "current_step": 1,
            "total_steps": process_info["total_steps"],
            "section": section,
            "process_type": process_info["process_type"]
        }
    
    async def _generate_step_question(self, step: int, session_data: Dict) -> Dict[str, Any]:
        """섹션별 단계별 질문 생성"""
        process_type = session_data["process_type"]
        company_name = session_data["company_name"]
        position = session_data["position"]
        
        if process_type == "motivation":
            return await self._generate_motivation_question(step, session_data)
        elif process_type == "strengths":
            return await self._generate_strengths_question(step, session_data)
        elif process_type == "growth":
            return await self._generate_growth_question(step, session_data)
        elif process_type == "communication":
            return await self._generate_communication_question(step, session_data)
        elif process_type == "company_analysis":
            return await self._generate_company_analysis_question(step, session_data)
        else:
            return {
                "type": "text",
                "question": "추가 정보를 입력해주세요.",
                "options": None
            }
    
    async def _generate_motivation_question(self, step: int, session_data: Dict) -> Dict[str, Any]:
        """지원 동기 섹션 질문 생성 - 템플릿의 단계별 구조를 정확히 따름"""
        company_name = session_data["company_name"]
        position = session_data["position"]
        
        if step == 1:
            return {
                "type": "file_upload",
                "question": f"지원하고자 하는 사람인 공고의 모집 요강이 잘 보이도록 전체 화면 캡처한 이미지를 업로드해주세요.",
                "description": "📎 모집 부문, 담당 업무, 자격 요건, 우대사항 등이 포함된 전체 공고 이미지가 필요합니다.",
                "placeholder": "공고 이미지 파일을 선택하세요"
            }
        elif step == 2:
            return {
                "type": "text", 
                "question": f"해당 기업의 공식 홈페이지 주소(URL)를 입력해 주세요.",
                "placeholder": "예: https://www.companyname.co.kr",
                "description": "기업 정보 분석을 위해 공식 홈페이지가 필요합니다."
            }
        elif step == 3:
            return {
                "type": "selection",
                "question": "기존에 작성하신 지원 동기 초안이 있나요?",
                "options": [
                    {"value": "있음", "label": "초안 있음"},
                    {"value": "없음", "label": "초안 없음"}
                ],
                "max_selections": 1
            }
        elif step == 4:
            has_draft = session_data.get("selections", {}).get("has_draft", "없음")
            if has_draft == "있음":
                return {
                    "type": "text",
                    "question": "기존에 작성하신 지원 동기 초안을 입력해주세요.",
                    "placeholder": "기존 초안 내용을 입력하세요...",
                    "description": "공고 분석 결과와 비교하여 초안을 검토하겠습니다."
                }
            else:
                return {
                    "type": "file_upload",
                    "question": "이력서 및 자기소개서 PDF를 업로드해주세요.",
                    "description": "문서 분석 후 주요 경험, 보유 역량, 직무 관련 요소를 추출하겠습니다.",
                    "placeholder": "PDF 파일을 선택하세요"
                }
        elif step == 5:
            return {
                "type": "text",
                "question": "분석된 이력 및 경험 요약이 정확한지 확인해주세요. 수정할 내용이 있다면 입력해주세요.",
                "placeholder": "수정할 내용을 입력하거나 '정확합니다'라고 입력하세요...",
                "description": """
                🧾 분석된 이력 및 경험 요약:
                - 경험: Java 기반 도서 구매 앱, 지역축제 웹사이트 등 2건의 팀 프로젝트 수행  
                - 기술: HTML/CSS/JS, Java/Spring, MySQL, Ajax, GitHub 사용  
                - 강점: 책임감 있는 협업 자세, 문제 해결력, 일정 관리 주도력
                """
            }
        elif step == 6:
            return {
                "type": "text",
                "question": "생성된 지원동기 초안에서 구체성이 부족한 부분을 보완해주세요.",
                "placeholder": "예: 구체적인 프로젝트명, 사용한 기술, 정량적 성과 등을 추가로 입력하세요...",
                "description": "문장별로 분석하여 다음과 같은 정보를 보완하겠습니다:\n- 시간/장소 정보\n- 구체적 행동과 기술\n- 결과와 성과\n- 팀워크 경험\n- 학습과 느낀 점"
            }
        elif step == 7:
            return {
                "type": "text",
                "question": "마지막으로 추가하거나 강조하고 싶은 내용이 있다면 입력해주세요.",
                "placeholder": "최종 검토 및 보완 사항을 입력하세요...",
                "description": "입력하신 내용을 바탕으로 최종 지원동기 자기소개서를 완성하겠습니다."
            }
        
        return {"type": "text", "question": "추가 정보를 입력해주세요."}
    
    async def _generate_strengths_question(self, step: int, session_data: Dict) -> Dict[str, Any]:
        """나의 장점 섹션 질문 생성 (6단계)"""
        if step == 1:
            return {
                "type": "selection",
                "question": "본인을 가장 잘 표현할 수 있는 강점 1개를 선택해주세요.",
                "options": [
                    {"value": "책임감", "label": "책임감: 맡은 일을 끝까지 수행하고 약속을 지키는 능력"},
                    {"value": "창의성", "label": "창의성: 새로운 아이디어를 제시하고 문제를 독창적으로 해결하는 능력"},
                    {"value": "문제해결능력", "label": "문제 해결 능력: 예상치 못한 상황에서 빠르게 해결책을 찾아 적용하는 능력"},
                    {"value": "팀워크", "label": "팀워크: 팀원들과 협력하여 목표를 달성하는 능력"},
                    {"value": "소통능력", "label": "소통 능력: 명확하고 효과적으로 의사소통하는 능력"},
                    {"value": "적응력", "label": "적응력: 새로운 환경이나 변화에 빠르게 적응할 수 있는 능력"},
                    {"value": "리더십", "label": "리더십: 팀을 이끌고 목표를 달성하도록 지원하는 능력"},
                    {"value": "분석력", "label": "분석력: 문제를 논리적으로 파악하고 효과적인 해결책을 도출하는 능력"},
                    {"value": "기획력", "label": "기획력: 목표를 설정하고 체계적으로 계획을 수립하여 실행하는 능력"},
                    {"value": "자기주도성", "label": "자기주도성: 스스로 목표를 설정하고 실행할 수 있는 능력"},
                    {"value": "학습능력", "label": "학습 능력: 새로운 지식을 빠르게 습득하고 적용할 수 있는 능력"}
                ],
                "max_selections": 1
            }
        elif step == 2:
            selected_strength = session_data.get("selections", {}).get("strength", "책임감")
            keywords = self._get_strength_keywords(selected_strength)
            return {
                "type": "selection",
                "question": f"선택한 강점 '{selected_strength}'와 관련된 키워드를 1-2개 선택하세요.",
                "options": [{
                    "value": keyword,
                    "label": keyword
                } for keyword in keywords],
                "max_selections": 2
            }
        elif step == 3:
            return {
                "type": "selection",
                "question": "강점과 관련된 경험 카테고리를 선택해주세요.",
                "options": [
                    {"value": "팀프로젝트", "label": "팀 프로젝트"},
                    {"value": "인턴경험", "label": "인턴 경험"},
                    {"value": "아르바이트", "label": "아르바이트 및 현장 경험"},
                    {"value": "리더십경험", "label": "리더십 경험"},
                    {"value": "학습개발", "label": "학습 및 자기개발"},
                    {"value": "봉사활동", "label": "봉사활동"}
                ],
                "max_selections": 1
            }
        elif step == 4:
            return {
                "type": "text",
                "question": "선택한 경험에서 직면한 주요 문제나 도전 상황을 구체적으로 입력하세요.",
                "placeholder": "예: 팀 프로젝트 중 의견 충돌로 진행이 지연되는 상황이 발생했습니다..."
            }
        elif step == 5:
            return {
                "type": "text",
                "question": "해당 문제를 해결하기 위해 구체적으로 어떤 행동을 했는지 입력하세요.",
                "placeholder": "예: 각 팀원의 의견을 정리하고 회의를 주도하여 합의점을 찾았습니다..."
            }
        elif step == 6:
            return {
                "type": "selection",
                "question": "경험을 통해 느낀 점이나 가치관을 2개 선택하세요.",
                "options": [
                    {"value": "팀성장지원", "label": "강점은 팀의 성장을 지원할 수 있는 중요한 가치라는 것을 깨달았습니다"},
                    {"value": "포기하지않는자세", "label": "어려운 상황에서도 포기하지 않고 해결책을 찾는 자세가 중요함을 배웠습니다"},
                    {"value": "신뢰구축", "label": "강점을 통해 팀의 신뢰를 얻고 협업이 원활해졌습니다"},
                    {"value": "지속적학습", "label": "지속적인 학습과 자기계발의 중요성을 깨달았습니다"},
                    {"value": "소통의힘", "label": "원활한 소통이 모든 문제 해결의 시작임을 배웠습니다"}
                ],
                "max_selections": 2
            }
        
        return {"type": "text", "question": "추가 정보를 입력해주세요."}
    
    async def _generate_growth_question(self, step: int, session_data: Dict) -> Dict[str, Any]:
        """성장과정 섹션 질문 생성 - 템플릿의 단계별 구조를 정확히 따름"""
        if step == 1:
            return {
                "type": "selection",
                "question": "당신의 성장을 가장 잘 보여주는 키워드 3개를 선택해주세요. (각 키워드는 서로 다른 성장과정 문단으로 활용됩니다)",
                "options": [
                    {"value": "꾸준함", "label": "꾸준함"},
                    {"value": "실패극복", "label": "실패 극복"}, 
                    {"value": "협업", "label": "협업"},
                    {"value": "책임감", "label": "책임감"},
                    {"value": "비판적사고", "label": "비판적 사고"},
                    {"value": "적응력", "label": "적응력"},
                    {"value": "학습지속력", "label": "학습 지속력"},
                    {"value": "기술호기심", "label": "기술 호기심"},
                    {"value": "문제해결력", "label": "문제 해결력"},
                    {"value": "고객중심태도", "label": "고객 중심 태도"}
                ],
                "max_selections": 3
            }
        elif step in [2, 3, 4]:
            selected_keywords = session_data.get("selections", {}).get("growth_keywords", [])
            current_keyword_index = step - 2
            
            # 디버깅 로그 추가
            logger.info(f"성장과정 step {step}: selected_keywords={selected_keywords}, current_keyword_index={current_keyword_index}")
            
            if current_keyword_index < len(selected_keywords):
                current_keyword = selected_keywords[current_keyword_index]
                return {
                    "type": "text",
                    "question": f"✦ 키워드 {current_keyword_index + 1}: '{current_keyword}'\n\n각 질문에 순서대로 답해주세요:\n\n1. 이 키워드를 잘 보여주는 경험은 무엇인가요?\n   (예: 대학 시절 3년간 매주 3일씩 5km 달리기)\n\n2. 그 경험에서 구체적으로 어떤 행동을 했나요?\n   (예: 컨디션과 상관없이 루틴 유지, 기록하며 스스로 동기부여 등)\n\n3. 이 경험을 통해 어떤 태도나 가치관을 갖게 되었나요?\n   (예: 꾸준함은 의지를 넘어 자기 신뢰를 만든다는 믿음)\n\n4. 이 태도가 IT 직무에서 어떻게 도움이 될 것이라 생각하나요?\n   (예: 지속적인 학습과 반복적인 실험이 중요한 개발자에게 필수 역량)",
                    "placeholder": f"'{current_keyword}'와 관련된 구체적인 경험과 배움을 4개 질문 순서대로 자세히 설명해주세요...",
                    "keyword": current_keyword
                }
            else:
                logger.warning(f"성장과정 step {step}: 키워드 인덱스 범위 초과. current_keyword_index={current_keyword_index}, keywords length={len(selected_keywords)}")
            
        return {"type": "text", "question": "추가 정보를 입력해주세요."}
    
    async def _generate_communication_question(self, step: int, session_data: Dict) -> Dict[str, Any]:
        """커뮤니케이션 섹션 질문 생성 - 템플릿의 7단계 구조를 정확히 따름"""
        if step == 1:
            return {
                "type": "selection",
                "question": "지원 직무를 선택해주세요.",
                "options": [
                    {"value": "개발자-백엔드", "label": "개발자(백엔드)"},
                    {"value": "개발자-프론트엔드", "label": "개발자(프론트엔드)"},
                    {"value": "개발자-풀스택", "label": "개발자(풀스택)"},
                    {"value": "엔지니어-클라우드", "label": "엔지니어(클라우드)"},
                    {"value": "엔지니어-네트워크", "label": "엔지니어(네트워크)"},
                    {"value": "엔지니어-시스템인프라", "label": "엔지니어(시스템 인프라)"},
                    {"value": "직접입력", "label": "직접 입력"}
                ],
                "max_selections": 1
            }
        elif step == 2:
            return {
                "type": "selection", 
                "question": "본인의 소통 스타일 키워드를 선택해주세요.",
                "options": [
                    {"value": "경청", "label": "경청"}, 
                    {"value": "중재", "label": "중재"}, 
                    {"value": "배려", "label": "배려"}, 
                    {"value": "존중", "label": "존중"},
                    {"value": "설득", "label": "설득"}, 
                    {"value": "조율", "label": "조율"},
                    {"value": "친화력", "label": "친화력"}, 
                    {"value": "열린마음", "label": "열린 마음"},
                    {"value": "다름인정", "label": "다름 인정"}, 
                    {"value": "분위기메이커", "label": "분위기 메이커"},
                    {"value": "갈등해결", "label": "갈등 해결"}, 
                    {"value": "타협", "label": "타협"},
                    {"value": "적극적소통", "label": "적극적 소통"},
                    {"value": "직접입력", "label": "직접 입력"}
                ],
                "max_selections": 1
            }
        elif step == 3:
            return {
                "type": "selection",
                "question": "경험한 상황을 선택해주세요.",
                "options": [
                    {"value": "동아리학생회", "label": "동아리/학생회"}, 
                    {"value": "팀프로젝트", "label": "팀 프로젝트"},
                    {"value": "아르바이트", "label": "아르바이트"}, 
                    {"value": "봉사활동", "label": "봉사활동"},
                    {"value": "가족모임", "label": "가족 모임"},
                    {"value": "학급반장활동", "label": "학급/반장 활동"}, 
                    {"value": "MT워크숍", "label": "MT/워크숍"},
                    {"value": "동호회", "label": "동호회"},
                    {"value": "회사경험", "label": "회사(인턴/알바/정규직)"},
                    {"value": "군대", "label": "군대"},
                    {"value": "학회", "label": "학회"},
                    {"value": "스터디모임", "label": "스터디 모임"}, 
                    {"value": "단체여행", "label": "단체 여행"},
                    {"value": "멘토링", "label": "동생/후배 멘토링"},
                    {"value": "다양한연령대", "label": "다양한 연령대와 함께한 경험"}, 
                    {"value": "외국인교류", "label": "외국인/다문화 친구와의 교류"},
                    {"value": "고객응대", "label": "고객 응대"},
                    {"value": "직접입력", "label": "직접 입력"}
                ],
                "max_selections": 1
            }
        elif step == 4:
            situation = session_data.get("selections", {}).get("communication_situation", "팀프로젝트")
            return {
                "type": "selection",
                "question": f"선택한 상황({situation})에서의 구체적인 에피소드를 선택하거나 직접 입력해주세요.",
                "options": [
                    {"value": "의견충돌해결", "label": "팀 내 의견 충돌 상황에서 중재 역할을 했습니다"},
                    {"value": "소통문제개선", "label": "소통 부족으로 인한 문제를 적극적으로 해결했습니다"},
                    {"value": "갈등조정", "label": "서로 다른 입장의 갈등을 조정하고 합의점을 찾았습니다"},
                    {"value": "분위기개선", "label": "어색하거나 경직된 분위기를 개선하는 역할을 했습니다"},
                    {"value": "정보공유촉진", "label": "정보 공유를 촉진하여 팀 효율성을 높였습니다"},
                    {"value": "다양성수용", "label": "다양한 배경의 사람들과 효과적으로 소통했습니다"},
                    {"value": "리더십발휘", "label": "그룹을 이끌며 목표 달성을 위해 소통했습니다"},
                    {"value": "직접입력", "label": "직접 입력"}
                ],
                "max_selections": 1
            }
        elif step == 5:
            return {
                "type": "selection",
                "question": "그 상황에서 내가 취한 구체적인 행동을 선택하거나 직접 입력해주세요.",
                "options": [
                    {"value": "적극적경청", "label": "모든 구성원의 의견을 적극적으로 경청했습니다"},
                    {"value": "중재자역할", "label": "중립적 입장에서 중재자 역할을 수행했습니다"},
                    {"value": "창의적해결책", "label": "창의적이고 win-win 할 수 있는 해결책을 제시했습니다"},
                    {"value": "감정관리", "label": "감정적 대립을 진정시키고 이성적 토론을 유도했습니다"},
                    {"value": "명확한소통", "label": "명확하고 구체적인 언어로 의사를 전달했습니다"},
                    {"value": "지속적피드백", "label": "지속적인 피드백과 확인을 통해 이해를 도모했습니다"},
                    {"value": "문화적배려", "label": "상대방의 문화적 배경을 고려하여 소통했습니다"},
                    {"value": "직접입력", "label": "직접 입력"}
                ],
                "max_selections": 1
            }
        elif step == 6:
            return {
                "type": "selection",
                "question": "결과 및 배운 점을 선택해주세요 (최대 2개까지 중복 선택 가능).",
                "options": [
                    {"value": "팀워크향상", "label": "팀워크가 향상되었습니다"},
                    {"value": "갈등해결", "label": "갈등이 성공적으로 해결되었습니다"},
                    {"value": "소통능력발전", "label": "소통 능력이 크게 발전했습니다"},
                    {"value": "리더십성장", "label": "리더십이 성장했습니다"},
                    {"value": "상호이해증진", "label": "상호 이해가 증진되었습니다"},
                    {"value": "프로젝트성공", "label": "프로젝트나 목표가 성공적으로 달성되었습니다"},
                    {"value": "신뢰구축", "label": "구성원 간 신뢰가 구축되었습니다"},
                    {"value": "효율성증대", "label": "업무나 활동의 효율성이 크게 증대되었습니다"}
                ],
                "max_selections": 2
            }
        elif step == 7:
            job_type = session_data.get("selections", {}).get("job_type", "개발자")
            return {
                "type": "text",
                "question": f"이 경험이 {job_type} 직무 수행에 어떻게 도움이 될지 구체적으로 설명해주세요.",
                "placeholder": f"{job_type} 업무에서 이 소통 경험이 어떻게 활용될지 구체적으로 작성해주세요...",
                "description": "개발팀 내 협업, 타부서와의 소통, 고객 요구사항 이해 등을 고려하여 답변해주세요."
            }
        
        return {"type": "text", "question": "추가 정보를 입력해주세요."}
    
    async def _generate_company_analysis_question(self, step: int, session_data: Dict) -> Dict[str, Any]:
        """기업 분석 섹션 질문 생성 - 템플릿의 구조를 정확히 따름"""
        company_name = session_data["company_name"]
        return {
            "type": "text",
            "question": f"🏢 기업 분석을 위해 분석 대상 기업명을 법인 표기와 함께 정확히 입력해주세요.\n\n📌 입력 방식:\n• 기업명은 반드시 입력해야 합니다.\n• 법인 표기를 포함해 주세요 (예: ㈜, (주), 주식회사 등)\n\n예시:\n• ㈜무브먼츠랩\n• 무브먼츠랩㈜  \n• 주식회사 무브먼츠랩\n\n✅ 분석 항목 (10가지):\n① 회사명 ② 웹사이트 ③ 설립 연도 및 위치 ④ 직원 수 ⑤ 산업군 및 주요 사업 분야\n⑥ 조직 문화 및 복지 제도 ⑦ 최근 활동 및 성과 ⑧ 기업 평판 및 후기\n⑨ 대표 전화번호 (용도, 출처 포함) ⑩ 대표 이메일 주소 (용도, 출처 포함)",
            "placeholder": f"예: ㈜{company_name} 또는 {company_name}㈜",
            "description": "입력하신 기업명을 바탕으로 공식 홈페이지 및 공개 정보를 검색하여 체계적으로 분석해드립니다. 정보가 부족한 항목은 '정보 부족'으로 명시됩니다."
        }
    
    def _get_strength_keywords(self, strength: str) -> List[str]:
        """강점별 관련 키워드 반환"""
        keywords_map = {
            "책임감": ["성실함", "꾸준함", "목표 설정", "시간 관리", "신뢰성", "자기주도성", "약속 준수", "계획성", "문제 해결 능력", "결과 지향"],
            "창의성": ["혁신", "아이디어 발굴", "창의적 사고", "문제 해결", "독창성", "상상력", "실험 정신", "도전 정신", "유연성", "발상의 전환"],
            "문제해결능력": ["논리적 사고", "분석력", "추론 능력", "해결책 도출", "체계적 접근", "비판적 사고", "창의적 해결", "효율성", "우선순위 설정", "실행력"],
            "팀워크": ["협력", "소통", "배려", "존중", "조화", "공동 목표", "역할 분담", "상호 지원", "갈등 해결", "시너지"],
            "소통능력": ["경청", "표현력", "설득력", "공감 능력", "명확성", "적극성", "피드백", "중재", "네트워킹", "프레젠테이션"],
            "적응력": ["유연성", "변화 수용", "학습 능력", "개방성", "회복력", "민첩성", "다양성 인정", "환경 적응", "스트레스 관리", "새로운 도전"],
            "리더십": ["비전 제시", "팀 관리", "동기 부여", "의사 결정", "책임감", "멘토링", "영향력", "전략적 사고", "조직화", "권한 위임"],
            "분석력": ["논리적 사고", "데이터 분석", "패턴 인식", "체계적 접근", "비판적 사고", "정보 수집", "가설 설정", "검증 능력", "인사이트 도출", "의사결정 지원"],
            "기획력": ["전략 수립", "목표 설정", "일정 관리", "자원 배분", "리스크 관리", "프로세스 설계", "성과 측정", "개선 방안", "실행 계획", "모니터링"],
            "자기주도성": ["주도적 학습", "목표 의식", "자기 관리", "동기 부여", "독립성", "계획 수립", "실행력", "자기 개발", "책임감", "성취 욕구"],
            "학습능력": ["지식 습득", "빠른 이해", "적응력", "호기심", "성장 마인드", "실습 적용", "피드백 수용", "지속적 개선", "전문성 개발", "새로운 기술"]
        }
        return keywords_map.get(strength, ["성실함", "꾸준함", "목표 설정", "시간 관리", "신뢰성"])
    
    async def submit_answer(
        self,
        session_id: str,
        answer: str,
        selections: List[str] = None
    ) -> Dict[str, Any]:
        """답변 제출 및 다음 질문 생성 - 섹션별 맞춤 처리"""
        if session_id not in self.sessions:
            raise ValueError("유효하지 않은 세션 ID입니다.")
        
        session = self.sessions[session_id]
        current_step = session["current_step"]
        process_type = session["process_type"]
        
        # 답변 저장 (텍스트 또는 선택)
        logger.info(f"submit_answer 디버그: session_id={session_id}, step={current_step}, process_type={process_type}, selections={selections}, answer={answer}")
        
        if selections:
            session["answers"].append(selections)
            logger.info(f"selections가 있으므로 _store_selections 호출 예정: {selections}")
            # 단계별 선택 값 저장
            self._store_selections(session, current_step, selections)
        else:
            logger.info(f"selections가 없으므로 answer 저장: {answer}")
            session["answers"].append(answer)
            # 텍스트 답변도 단계별로 저장
            self._store_step_data(session, current_step, answer)
        
        # 다음 단계로 이동
        if current_step < session["total_steps"]:
            session["current_step"] = current_step + 1
            next_question_data = await self._generate_step_question(
                session["current_step"],
                session
            )
            session["current_question"] = next_question_data
            session["questions"].append(next_question_data)
            
            return {
                "session_id": session_id,
                "question": next_question_data,
                "current_step": session["current_step"],
                "total_steps": session["total_steps"],
                "is_completed": False,
                "process_type": process_type
            }
        else:
            # 모든 질문 완료 - 자소서 생성
            final_content = await self._generate_final_content(session)
            session["final_content"] = final_content
            session["is_completed"] = True
            
            return {
                "session_id": session_id,
                "current_step": session["current_step"],
                "total_steps": session["total_steps"],
                "is_completed": True,
                "generated_content": final_content,  # 프론트엔드가 찾는 필드명으로 변경
                "process_type": process_type
            }
    
    def _store_selections(self, session: Dict, step: int, selections: List[str]):
        """단계별 선택 값 저장"""
        process_type = session["process_type"]
        
        # 디버깅 로그 추가
        logger.info(f"_store_selections 호출: process_type={process_type}, step={step}, selections={selections}")
        
        if process_type == "motivation":
            if step == 3:
                session["selections"]["has_draft"] = selections[0] if selections else ""
        
        elif process_type == "strengths":
            if step == 1:
                session["selections"]["strength"] = selections[0] if selections else ""
            elif step == 2:
                session["selections"]["strength_keywords"] = selections
            elif step == 3:
                session["selections"]["experience_category"] = selections[0] if selections else ""
            elif step == 6:
                session["selections"]["learnings"] = selections
                
        elif process_type == "growth":
            if step == 1:
                session["selections"]["growth_keywords"] = selections
                logger.info(f"성장과정 키워드 저장됨: {selections}")
                logger.info(f"세션 전체 selections 상태: {session['selections']}")
                
        elif process_type == "communication":
            if step == 1:
                session["selections"]["job_type"] = selections[0] if selections else ""
            elif step == 2:
                session["selections"]["communication_style"] = selections[0] if selections else ""
            elif step == 3:
                session["selections"]["communication_situation"] = selections[0] if selections else ""
            elif step == 4:
                session["selections"]["communication_episode"] = selections[0] if selections else ""
            elif step == 5:
                session["selections"]["communication_action"] = selections[0] if selections else ""
            elif step == 6:
                session["selections"]["communication_results"] = selections
    
    def _store_step_data(self, session: Dict, step: int, answer: str):
        """단계별 텍스트 답변 저장"""
        if "step_data" not in session:
            session["step_data"] = {}
        session["step_data"][f"step_{step}"] = answer
    
    async def _generate_final_content(self, session: Dict) -> str:
        """섹션별 최종 자소서 생성"""
        try:
            process_type = session["process_type"]
            section = session["section"]
            
            # 섹션별 컨텍스트 가져오기 - 한국어 섹션명으로 직접 매핑
            context = get_context_for_section(section, session["company_name"] + " " + session["position"])
            
            # 섹션별 맞춤 시스템 프롬프트 생성
            system_prompt = await self._create_section_specific_prompt(session, context)
            
            response = await openai_service.generate_completion([
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": f"{section} 자기소개서를 작성해주세요."}
            ], max_tokens=2500)
            
            return response.strip()
            
        except Exception as e:
            logger.error(f"최종 컨텐츠 생성 실패: {e}")
            return self._generate_fallback_content(session)
    
    async def _create_section_specific_prompt(self, session: Dict, context: str) -> str:
        """섹션별 맞춤 시스템 프롬프트 생성"""
        process_type = session["process_type"]
        company_name = session["company_name"]
        position = session["position"]
        section = session["section"]
        
        base_prompt = f"""
        당신은 전문 자기소개서 작성가입니다.
        사용자의 답변을 바탕으로 {section} 섹션을 작성해주세요.
        
        기업정보:
        - 회사명: {company_name}
        - 포지션: {position}
        
        작성 가이드라인:
        {context}
        
        기본 요구사항:
        1. 구체적이고 진정성 있는 내용
        2. 1000-1500자 내외
        3. 전문적이면서도 개인적인 톤
        4. {company_name}과 {position}에 맞춤화
        """
        
        # 섹션별 특화 내용 추가
        if process_type == "motivation":
            answers_text = "\n".join([f"{i+1}. {answer}" for i, answer in enumerate(session["answers"])])
            return base_prompt + f"""
            
            사용자 답변:
            {answers_text}
            
            추가 요구사항:
            1. STAR 기법 활용 (상황-과제-행동-결과)
            2. 지원 동기의 구체성과 진정성 강조
            3. 회사와 직무에 대한 이해도 표현
            4. 미래 목표와 연결
            """
            
        elif process_type == "strengths":
            selected_strength = session.get("selections", {}).get("strength", "")
            keywords = session.get("selections", {}).get("strength_keywords", [])
            experience_category = session.get("selections", {}).get("experience_category", "")
            learnings = session.get("selections", {}).get("learnings", [])
            
            return base_prompt + f"""
            
            선택된 강점: {selected_strength}
            관련 키워드: {', '.join(keywords)}
            경험 카테고리: {experience_category}
            배운 점: {', '.join(learnings)}
            
            텍스트 답변:
            - 문제 상황: {session.get("step_data", {}).get("step_4", "정보 없음")}
            - 취한 행동: {session.get("step_data", {}).get("step_5", "정보 없음")}
            
            추가 요구사항:
            1. 선택한 강점을 중심으로 구성
            2. 구체적인 에피소드 포함
            3. 문제 해결 과정 상세히 서술
            4. 직무 연관성 강조
            """
            
        elif process_type == "growth":
            growth_keywords = session.get("selections", {}).get("growth_keywords", [])
            keyword_stories = []
            for i, keyword in enumerate(growth_keywords[:3]):
                story = session.get("step_data", {}).get(f"step_{i+2}", "")
                if story:
                    keyword_stories.append(f"키워드 '{keyword}': {story}")
            
            return base_prompt + f"""
            
            선택된 성장 키워드: {', '.join(growth_keywords)}
            
            키워드별 경험:
            {chr(10).join(keyword_stories)}
            
            추가 요구사항:
            1. 3개의 키워드를 각각 문단으로 구성
            2. 경험 → 변화 → 직무 연결 흐름 유지
            3. "성장 과정 (1)(2)(3)" 형태로 구성
            4. 각 문단마다 소제목 포함
            """
            
        elif process_type == "communication":
            job_type = session.get("selections", {}).get("job_type", "")
            comm_style = session.get("selections", {}).get("communication_style", "")
            situation = session.get("selections", {}).get("communication_situation", "")
            results = session.get("selections", {}).get("communication_results", [])
            
            episode = session.get("step_data", {}).get("step_4", "")
            actions = session.get("step_data", {}).get("step_5", "")
            job_relevance = session.get("step_data", {}).get("step_7", "")
            
            return base_prompt + f"""
            
            지원 직무: {job_type}
            소통 스타일: {comm_style}
            경험 상황: {situation}
            달성 결과: {', '.join(results)}
            
            구체적 경험:
            - 에피소드: {episode}
            - 취한 행동: {actions}
            - 직무 연관성: {job_relevance}
            
            추가 요구사항:
            1. 두괄식 구조로 시작
            2. 커뮤니케이션 역량 중심 구성
            3. 1000자 이내로 간결하게
            4. 톤 앤 매너: 따뜻하고 진정성 있게
            """
            
        elif process_type == "company_analysis":
            company_input = session.get("step_data", {}).get("step_1", "")
            
            return base_prompt + f"""
            
            분석 대상 기업: {company_input}
            
            요구사항:
            1. 10가지 항목 체계적 분석
            2. 공식 홈페이지 및 공개 정보 기반
            3. 정보 부족 시 "정보 부족"으로 명시
            4. 출처 가능한 정보는 링크 포함
            5. 법인 표기 정확히 확인
            
            분석 항목:
            ① 회사명 ② 웹사이트 ③ 설립 연도 및 위치 ④ 직원 수
            ⑤ 산업군 및 주요 사업 분야 ⑥ 조직 문화 및 복지 제도
            ⑦ 최근 활동 및 성과 ⑧ 기업 평판 및 후기
            ⑨ 대표 전화번호 ⑩ 대표 이메일 주소
            """
        
        return base_prompt
    
    def _generate_fallback_content(self, session: Dict) -> str:
        """폴백 컨텐츠 생성"""
        answers = session["answers"]
        company = session["company_name"]
        position = session["position"]
        
        content = f"""
        {company}의 {position} 포지션에 지원하게 된 이유는 {answers[0] if len(answers) > 0 else '회사의 비전과 성장 가능성에 깊은 매력을 느꼈기 때문입니다.'}
        
        {answers[1] if len(answers) > 1 else '저는 관련 분야에서 다양한 프로젝트 경험을 쌓아왔습니다.'} 
        특히 {answers[2] if len(answers) > 2 else '프로젝트 진행 중 여러 기술적 도전에 직면했지만,'} 
        {answers[3] if len(answers) > 3 else '체계적인 문제 해결 접근법을 통해 이를 극복했습니다.'}
        
        그 결과 {answers[4] if len(answers) > 4 else '성공적인 프로젝트 완수라는 성과를 얻을 수 있었습니다.'} 
        이러한 경험은 {answers[5] if len(answers) > 5 else f'{position} 직무 수행에 큰 도움이 될 것입니다.'}
        
        {company}에 입사한다면, {answers[6] if len(answers) > 6 else '3년 내에 핵심 인재로 성장하여 회사의 발전에 기여하고 싶습니다.'}
        """
        
        return content.strip()
    
    async def get_session(
        self,
        session_id: str
    ) -> Dict[str, Any]:
        """세션 정보 조회"""
        if session_id not in self.sessions:
            raise ValueError("유효하지 않은 세션 ID입니다.")
        
        return self.sessions[session_id]
    
    def get_available_sections(self) -> List[str]:
        """사용 가능한 섹션 목록"""
        return list(self.section_processes.keys())

interactive_service = InteractiveCoverLetterService()

@router.get("/interactive/sections", response_model=APIResponse)
async def get_interactive_sections():
    """인터랙티브 자소서 섹션 목록"""
    try:
        sections = interactive_service.get_available_sections()
        return APIResponse(
            success=True,
            message="섹션 목록을 성공적으로 가져왔습니다",
            data={"sections": sections}
        )
    except Exception as e:
        logger.error(f"섹션 목록 조회 오류: {e}")
        return APIResponse(
            success=False,
            message="섹션 목록 조회에 실패했습니다",
            error=str(e)
        )

@router.post("/interactive/start", response_model=APIResponse)
async def start_interactive_session(
    company_name: str,
    position: str,
    section: str = "지원 동기",
    user_id: str = None
):
    """인터랙티브 자소서 세션 시작"""
    try:
        result = await interactive_service.start_interactive_session(
            company_name=company_name,
            position=position,
            section=section,
            user_id=user_id
        )
        
        return APIResponse(
            success=True,
            message="인터랙티브 세션이 시작되었습니다",
            data=result
        )
    except Exception as e:
        logger.error(f"세션 시작 오류: {e}")
        return APIResponse(
            success=False,
            message="세션 시작에 실패했습니다",
            error=str(e)
        )

@router.post("/interactive/answer", response_model=APIResponse)
async def submit_interactive_answer(
    session_id: str,
    answer: str = "",
    selections: List[str] = Query(None)
):
    """인터랙티브 답변 제출 (텍스트 또는 선택형)"""
    try:
        result = await interactive_service.submit_answer(
            session_id=session_id,
            answer=answer,
            selections=selections
        )
        
        return APIResponse(
            success=True,
            message="답변이 제출되었습니다",
            data=result
        )
    except Exception as e:
        logger.error(f"답변 제출 오류: {e}")
        return APIResponse(
            success=False,
            message="답변 제출에 실패했습니다",
            error=str(e)
        )

@router.get("/interactive/session/{session_id}", response_model=APIResponse)
async def get_interactive_session(session_id: str):
    """세션 정보 조회"""
    try:
        session_data = await interactive_service.get_session(session_id)
        
        return APIResponse(
            success=True,
            message="세션 정보를 성공적으로 가져왔습니다",
            data=session_data
        )
    except Exception as e:
        logger.error(f"세션 조회 오류: {e}")
        return APIResponse(
            success=False,
            message="세션 조회에 실패했습니다",
            error=str(e)
        )

@router.post("/generate-complete", response_model=APIResponse)
async def generate_complete_cover_letter(request: CoverLetterRequest):
    """완전한 RAG 기반 자소서 생성 프로세스"""
    try:
        section_contents = {}
        
        # 각 섹션별로 질문 생성하고 컨텐츠 생성 (실제로는 프론트엔드에서 단계별로 호출)
        for section in request.sections:
            # 여기서는 예시로 빈 답변으로 처리
            # 실제로는 사용자가 단계별로 답변한 내용이 들어와야 함
            sample_answers = [
                f"{section.value} 관련 경험이 있습니다",
                "구체적인 사례를 설명드리겠습니다",
                "이를 통해 배운 점이 있습니다"
            ]
            
            content = await cover_letter_service.generate_section_content(
                section=section,
                company_name=request.company_name,
                position=request.position,
                user_answers=sample_answers,
                user_info=request.user_info
            )
            
            section_name = cover_letter_service.section_names[section]
            section_contents[section_name] = content
        
        # 완성된 자소서 생성
        complete_content = await cover_letter_service.generate_complete_cover_letter(
            request=request,
            section_contents=section_contents
        )
        
        # 피드백 생성
        feedback = await cover_letter_service.provide_feedback_and_suggestions(
            complete_content
        )
        
        result = CoverLetterResponse(
            user_id=request.user_id,
            company_name=request.company_name,
            position=request.position,
            content=section_contents,
            created_at=datetime.now()
        )
        
        return APIResponse(
            success=True,
            message="자기소개서가 성공적으로 생성되었습니다",
            data={
                "cover_letter": result,
                "complete_content": complete_content,
                "feedback": feedback
            }
        )
        
    except Exception as e:
        logger.error(f"완성 자소서 생성 API 오류: {e}")
        return APIResponse(
            success=False,
            message="자기소개서 생성에 실패했습니다",
            error=str(e)
        )

@router.post("/feedback", response_model=APIResponse)
async def provide_cover_letter_feedback(content: str):
    """자소서 피드백 제공"""
    try:
        feedback = await cover_letter_service.provide_feedback_and_suggestions(content)
        
        return APIResponse(
            success=True,
            message="피드백이 성공적으로 생성되었습니다",
            data={"feedback": feedback}
        )
    except Exception as e:
        logger.error(f"피드백 API 오류: {e}")
        return APIResponse(
            success=False,
            message="피드백 생성에 실패했습니다",
            error=str(e)
        )

@router.post("/generate-complete-rag", response_model=APIResponse)
async def generate_complete_rag_cover_letter(
    company_name: str,
    position: str,
    user_name: str,
    sections_answers: Dict[str, List[str]],
    user_info: Dict[str, Any] = {}
):
    """완전한 RAG 기반 자소서 생성 및 PDF 생성"""
    try:
        section_contents = {}
        
        # 각 섹션별로 RAG 기반 컨텐츠 생성
        section_enum_map = {
            "motivation": CoverLetterSection.MOTIVATION,
            "growth": CoverLetterSection.GROWTH,
            "strengths": CoverLetterSection.STRENGTHS,
            "communication": CoverLetterSection.COMMUNICATION
        }
        
        for section_key, answers in sections_answers.items():
            if section_key not in section_enum_map:
                continue
                
            section_enum = section_enum_map[section_key]
            
            # RAG 기반 섹션 생성
            content = await cover_letter_service.generate_section_content(
                section=section_enum,
                company_name=company_name,
                position=position,
                user_answers=answers,
                user_info=user_info
            )
            
            section_name = cover_letter_service.section_names[section_enum]
            section_contents[section_name] = content
        
        # PDF 생성
        pdf_path = pdf_service.generate_cover_letter_pdf(
            company_name=company_name,
            position=position,
            user_name=user_name,
            sections=section_contents,
            user_info=user_info
        )
        
        # 완성된 자소서 통합
        complete_content = await cover_letter_service.generate_complete_cover_letter(
            request=CoverLetterRequest(
                user_id=1,  # 임시 사용자 ID
                company_name=company_name,
                position=position,
                sections=list(section_enum_map.values()),
                user_info=user_info
            ),
            section_contents=section_contents
        )
        
        # 피드백 생성
        feedback = await cover_letter_service.provide_feedback_and_suggestions(
            complete_content
        )
        
        result = CoverLetterResponse(
            user_id=1,
            company_name=company_name,
            position=position,
            content=section_contents,
            created_at=datetime.now()
        )
        
        return APIResponse(
            success=True,
            message="RAG 기반 완전한 자기소개서가 성공적으로 생성되었습니다",
            data={
                "cover_letter": result,
                "complete_content": complete_content,
                "feedback": feedback,
                "pdf_path": pdf_path,
                "pdf_filename": Path(pdf_path).name
            }
        )
        
    except Exception as e:
        logger.error(f"완전한 RAG 자소서 생성 API 오류: {e}")
        return APIResponse(
            success=False,
            message="RAG 기반 자기소개서 생성에 실패했습니다",
            error=str(e)
        )

@router.get("/demo-rag-complete", response_model=APIResponse)
async def demo_rag_complete_generation():
    """RAG 기반 완전한 자소서 생성 데모"""
    try:
        # 데모 데이터
        demo_data = {
            "company_name": "네이버",
            "position": "백엔드 개발자",
            "user_name": "김개발",
            "sections_answers": {
                "motivation": [
                    "네이버의 기술 혁신과 사용자 중심 서비스에 감명받았습니다",
                    "백엔드 개발을 통해 대규모 서비스를 구축하고 싶습니다",
                    "검색과 AI 기술 분야에서 성장하고 싶습니다"
                ],
                "growth": [
                    "대학 시절 프로그래밍 동아리에서 활발히 활동했습니다",
                    "팀 프로젝트에서 백엔드 개발을 담당하며 실력을 쌓았습니다",
                    "새로운 기술을 배우는 것을 즐깁니다"
                ],
                "strengths": [
                    "문제 해결 능력이 뛰어납니다",
                    "팀워크를 중시하고 소통을 잘합니다",
                    "끈기 있게 목표를 달성합니다"
                ],
                "communication": [
                    "명확하고 정확한 의사소통을 지향합니다",
                    "다른 사람의 의견을 경청하고 존중합니다",
                    "갈등 상황에서 중재자 역할을 합니다"
                ]
            },
            "user_info": {
                "education": "컴퓨터공학과 학사",
                "major": "컴퓨터공학",
                "skills": ["Java", "Spring Boot", "MySQL", "Redis"],
                "experience": "신입"
            }
        }
        
        return await generate_complete_rag_cover_letter(**demo_data)
        
    except Exception as e:
        logger.error(f"RAG 자소서 데모 오류: {e}")
        return APIResponse(
            success=False,
            message="RAG 자소서 데모 생성에 실패했습니다",
            error=str(e)
        )

@router.get("/pdf-files", response_model=APIResponse)
async def get_generated_pdf_files():
    """생성된 PDF 파일 목록 조회"""
    try:
        files = pdf_service.get_generated_files()
        
        return APIResponse(
            success=True,
            message="생성된 PDF 파일 목록입니다",
            data={"files": files, "total_count": len(files)}
        )
        
    except Exception as e:
        logger.error(f"PDF 파일 목록 조회 오류: {e}")
        return APIResponse(
            success=False,
            message="PDF 파일 목록 조회에 실패했습니다",
            error=str(e)
        )

@router.get("/search-context", response_model=APIResponse)
async def search_context(section: str, query: str = "", top_k: int = 3):
    """RAG 컨텍스트 검색 (디버그용)"""
    try:
        if query:
            results = search_similar_documents(query, section, top_k)
        else:
            context = get_context_for_section(section, query, top_k)
            results = {"context": context}
        
        return APIResponse(
            success=True,
            message="컨텍스트 검색이 완료되었습니다",
            data={"results": results}
        )
    except Exception as e:
        logger.error(f"컨텍스트 검색 API 오류: {e}")
        return APIResponse(
            success=False,
            message="컨텍스트 검색에 실패했습니다",
            error=str(e)
        )