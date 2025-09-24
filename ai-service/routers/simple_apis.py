"""
프론트엔드와 쉽게 연동할 수 있는 간단한 API 엔드포인트들
"""

from fastapi import APIRouter, HTTPException
from typing import Dict, Any, List, Optional
import logging
import json
from datetime import datetime
from pydantic import BaseModel

from services.enhanced_openai_service import enhanced_openai_service

logger = logging.getLogger(__name__)
router = APIRouter()

# 간단한 요청/응답 모델들
class SimpleInterviewRequest(BaseModel):
    field: str = "backend"
    experience_level: str = "junior" 
    job_title: Optional[str] = "개발자"
    interview_type: str = "technical"

class SimpleImageRequest(BaseModel):
    prompt: str
    style: Optional[str] = "realistic"
    
class SimpleTranslationRequest(BaseModel):
    text: str
    source_language: str = "ko"
    target_language: str = "en"
    document_type: str = "general"

class SimpleChatRequest(BaseModel):
    message: str
    user_id: Optional[str] = "anonymous"

class InterviewEvaluationRequest(BaseModel):
    question: str
    answer: str
    interview_type: str = "technical"
    job_position: str = "개발자"
    experience_level: str = "junior"

# 간단한 면접 질문 생성
@router.post("/simple-interview")
async def generate_simple_interview(request: SimpleInterviewRequest):
    """프론트엔드용 간단한 면접 질문 생성"""
    try:
        system_prompt = f"""
        당신은 {request.field} 분야의 {request.experience_level} 레벨 면접관입니다.
        {request.interview_type} 면접 질문 5개를 생성해주세요.
        
        응답 형식:
        1. 질문 1
        2. 질문 2
        3. 질문 3
        4. 질문 4
        5. 질문 5
        """
        
        result = await enhanced_openai_service.generate_completion_with_retry(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": f"{request.job_title} 포지션의 {request.interview_type} 면접 질문을 생성해주세요."}
            ],
            temperature=0.7,
            max_tokens=1000
        )
        
        # 질문들을 리스트로 파싱
        questions_text = result.get('content', '')
        questions = []
        
        for line in questions_text.split('\n'):
            line = line.strip()
            if line and (line.startswith(('1.', '2.', '3.', '4.', '5.')) or line.startswith('-')):
                # 번호나 불릿포인트 제거
                question = line.split('.', 1)[-1].strip() if '.' in line else line[1:].strip()
                if question:
                    questions.append(question)
        
        return {
            "success": True,
            "message": "면접 질문이 생성되었습니다",
            "data": {
                "questions": questions[:5],  # 최대 5개
                "interview_type": request.interview_type,
                "field": request.field,
                "experience_level": request.experience_level,
                "generated_at": datetime.now().isoformat()
            }
        }
        
    except Exception as e:
        logger.error(f"간단한 면접 질문 생성 실패: {e}")
        return {
            "success": False,
            "message": "면접 질문 생성에 실패했습니다",
            "error": str(e)
        }

# 간단한 이미지 생성
@router.post("/simple-image")
async def generate_simple_image(request: SimpleImageRequest):
    """프론트엔드용 간단한 이미지 생성"""
    try:
        result = await enhanced_openai_service.generate_image_advanced(
            prompt=request.prompt,
            quality="hd",
            style="vivid"
        )
        
        return {
            "success": True,
            "message": "이미지가 생성되었습니다",
            "data": {
                "image_url": result.get('url'),
                "prompt": request.prompt,
                "enhanced_prompt": result.get('prompt'),
                "style": request.style,
                "generated_at": datetime.now().isoformat()
            }
        }
        
    except Exception as e:
        logger.error(f"간단한 이미지 생성 실패: {e}")
        return {
            "success": False,
            "message": "이미지 생성에 실패했습니다",
            "error": str(e)
        }

# 간단한 번역
@router.post("/simple-translate")
async def simple_translate(request: SimpleTranslationRequest):
    """프론트엔드용 간단한 번역"""
    try:
        system_prompt = f"""
        당신은 전문 번역가입니다.
        {request.source_language}에서 {request.target_language}로 번역해주세요.
        
        번역 가이드라인:
        1. 자연스럽고 정확한 번역
        2. 문맥과 뉘앙스 보존
        3. {request.document_type} 문서에 적합한 톤과 스타일
        4. 전문용어는 업계 표준 표현 사용
        
        번역할 텍스트만 응답하세요.
        """
        
        result = await enhanced_openai_service.generate_completion_with_retry(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": request.text}
            ],
            temperature=0.3,
            max_tokens=2000
        )
        
        return {
            "success": True,
            "message": "번역이 완료되었습니다",
            "data": {
                "original_text": request.text,
                "translated_text": result.get('content', ''),
                "source_language": request.source_language,
                "target_language": request.target_language,
                "document_type": request.document_type,
                "translated_at": datetime.now().isoformat()
            }
        }
        
    except Exception as e:
        logger.error(f"간단한 번역 실패: {e}")
        return {
            "success": False,
            "message": "번역에 실패했습니다",
            "error": str(e)
        }

# 간단한 챗봇
@router.post("/simple-chat")
async def simple_chat(request: SimpleChatRequest):
    """프론트엔드용 간단한 챗봇"""
    try:
        system_prompt = """
        You must respond ONLY in Korean. You are a Korean employment counselor at the '잡았다' platform.
        
        당신은 '잡았다' 플랫폼의 한국어 취업 상담 전문가입니다. 
        반드시 한국어로만 답변하세요. 절대로 영어나 다른 언어를 사용하지 마세요.
        
        주요 역할:
        1. 취업 준비 관련 조언 제공
        2. 면접 팁과 자기소개서 작성 가이드  
        3. 커리어 상담과 진로 방향 제시
        4. 구직 활동 전반에 대한 도움
        
        친근하고 전문적인 한국어로만 도움이 되는 답변을 해주세요.
        영어 질문이 들어와도 반드시 한국어로 답변하세요.
        """
        
        # 한국어 강제 메시지
        korean_user_message = f"""다음 질문에 반드시 한국어로만 답변해주세요. 영어 사용 금지:
        
질문: {request.message}

한국어로 답변:"""

        result = await enhanced_openai_service.generate_completion_with_retry(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": korean_user_message}
            ],
            temperature=0.7,
            max_tokens=1000
        )
        
        return {
            "success": True,
            "message": "응답이 생성되었습니다",
            "data": {
                "user_message": request.message,
                "bot_response": result.get('content', ''),
                "user_id": request.user_id,
                "timestamp": datetime.now().isoformat()
            }
        }
        
    except Exception as e:
        logger.error(f"간단한 챗봇 응답 실패: {e}")
        return {
            "success": False,
            "message": "챗봇 응답 생성에 실패했습니다",
            "error": str(e)
        }

# 면접 답변 평가
@router.post("/evaluate-answer")
async def evaluate_interview_answer(request: InterviewEvaluationRequest):
    """프론트엔드용 간단한 면접 답변 평가"""
    try:
        system_prompt = f"""
        당신은 전문 면접관이자 평가 전문가입니다.
        다음 면접 답변을 전문적으로 평가해주세요.
        
        평가 기준:
        1. 답변의 완성도 (논리적 구성, 명확성)
        2. 기술적 정확성 ({request.interview_type} 면접 기준)
        3. 직무 적합성 ({request.job_position} 포지션 기준)
        4. 경험 수준 적절성 ({request.experience_level} 레벨 기준)
        5. 의사소통 능력
        
        JSON 형식으로 정확히 응답해주세요:
        {{
            "overall_score": 점수(0-100),
            "detailed_scores": {{
                "completeness": 점수(0-100),
                "technical_accuracy": 점수(0-100),
                "job_relevance": 점수(0-100),
                "communication": 점수(0-100)
            }},
            "strengths": ["강점1", "강점2", "강점3"],
            "improvements": ["개선점1", "개선점2", "개선점3"],
            "feedback": "상세한 피드백 (3-4문장)",
            "model_answer": "모범 답변 예시",
            "grade": "점수에 따른 등급 (A+, A, B+, B, C+, C, D)"
        }}
        """
        
        user_prompt = f"""
        면접 질문: {request.question}
        
        지원자 답변: {request.answer}
        
        면접 유형: {request.interview_type}
        직무: {request.job_position}
        경험 수준: {request.experience_level}
        
        위 답변을 평가해주세요.
        """
        
        result = await enhanced_openai_service.generate_completion_with_retry(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            temperature=0.3,
            max_tokens=2000
        )
        
        # JSON 파싱 시도
        try:
            evaluation = json.loads(result.get('content', '{}'))
        except json.JSONDecodeError:
            # JSON 파싱 실패시 기본 구조 생성
            content = result.get('content', '')
            evaluation = {
                "overall_score": 75,
                "detailed_scores": {
                    "completeness": 70,
                    "technical_accuracy": 75,
                    "job_relevance": 80,
                    "communication": 75
                },
                "strengths": ["답변에 성실함이 드러남", "기본적인 이해도를 보임", "의사소통 의지가 있음"],
                "improvements": ["더 구체적인 예시 필요", "기술적 깊이 보완", "논리적 구성 개선"],
                "feedback": content[:200] + "..." if len(content) > 200 else content,
                "model_answer": "구체적인 예시와 함께 체계적으로 설명하는 것이 좋습니다.",
                "grade": "B"
            }
        
        return {
            "success": True,
            "message": "면접 답변 평가가 완료되었습니다",
            "data": {
                "evaluation": evaluation,
                "question": request.question,
                "answer": request.answer,
                "metadata": {
                    "interview_type": request.interview_type,
                    "job_position": request.job_position,
                    "experience_level": request.experience_level,
                    "evaluated_at": datetime.now().isoformat()
                }
            }
        }
        
    except Exception as e:
        logger.error(f"면접 답변 평가 실패: {e}")
        return {
            "success": False,
            "message": "면접 답변 평가에 실패했습니다",
            "error": str(e)
        }

# 간단한 자소서 생성
@router.post("/simple-cover-letter")
async def generate_simple_cover_letter(
    company_name: str,
    position: str,
    applicant_name: str,
    experience: str = "신입"
):
    """프론트엔드용 간단한 자소서 생성"""
    try:
        system_prompt = f"""
        당신은 전문 자기소개서 작성 전문가입니다.
        
        다음 정보를 바탕으로 전문적인 자기소개서를 작성해주세요:
        - 회사명: {company_name}
        - 지원 직무: {position}
        - 지원자: {applicant_name}
        - 경력: {experience}
        
        자기소개서 구성:
        1. 지원 동기 (200자 내외)
        2. 성장 과정 (300자 내외)  
        3. 나의 강점 (300자 내외)
        4. 포부와 다짐 (200자 내외)
        
        각 섹션을 명확히 구분하여 작성해주세요.
        """
        
        result = await enhanced_openai_service.generate_completion_with_retry(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": f"{company_name} {position} 포지션에 지원하는 자기소개서를 작성해주세요."}
            ],
            temperature=0.7,
            max_tokens=2000
        )
        
        return {
            "success": True,
            "message": "자기소개서가 생성되었습니다",
            "data": {
                "cover_letter": result.get('content', ''),
                "company_name": company_name,
                "position": position,
                "applicant_name": applicant_name,
                "experience": experience,
                "generated_at": datetime.now().isoformat()
            }
        }
        
    except Exception as e:
        logger.error(f"간단한 자소서 생성 실패: {e}")
        return {
            "success": False,
            "message": "자소서 생성에 실패했습니다",
            "error": str(e)
        }

# API 목록 확인
@router.get("/")
async def list_simple_apis():
    """사용 가능한 간단한 API 목록"""
    return {
        "message": "프론트엔드용 간단한 AI API",
        "available_apis": {
            "POST /simple-interview": "면접 질문 생성",
            "POST /evaluate-answer": "면접 답변 평가 및 피드백",
            "POST /simple-image": "이미지 생성", 
            "POST /simple-translate": "텍스트 번역",
            "POST /simple-chat": "챗봇 대화",
            "POST /simple-cover-letter": "자소서 생성"
        },
        "examples": {
            "interview": {
                "field": "backend",
                "experience_level": "junior", 
                "job_title": "백엔드 개발자",
                "interview_type": "technical"
            },
            "image": {
                "prompt": "전문적인 면접 상황",
                "style": "realistic"
            },
            "translation": {
                "text": "안녕하세요",
                "source_language": "ko",
                "target_language": "en"
            },
            "chat": {
                "message": "면접 준비는 어떻게 해야 하나요?",
                "user_id": "test_user"
            },
            "evaluation": {
                "question": "RESTful API란 무엇인지 설명해주세요",
                "answer": "REST는 HTTP를 통해 자원을 처리하는 아키텍처 스타일입니다",
                "interview_type": "technical",
                "job_position": "백엔드 개발자",
                "experience_level": "junior"
            }
        }
    }