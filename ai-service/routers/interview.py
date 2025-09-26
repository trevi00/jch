from fastapi import APIRouter, HTTPException, Depends
from typing import List, Optional
import logging
from datetime import datetime
import json
from pydantic import BaseModel

from models.schemas import (
    InterviewRequest, InterviewQuestion, InterviewAnswer, 
    InterviewFeedback, InterviewResult, APIResponse, InterviewType
)
from services.openai_service import openai_service
from services.enhanced_interview_service import enhanced_interview_service
from services.interview_service import interview_service
from services.enhanced_openai_service import enhanced_openai_service

logger = logging.getLogger(__name__)
router = APIRouter()

class InterviewService:
    def __init__(self):
        self.technical_prompts = {
            "beginner": "초급 개발자를 위한 기술면접 질문",
            "intermediate": "중급 개발자를 위한 기술면접 질문", 
            "senior": "고급 개발자를 위한 기술면접 질문"
        }
        
        self.personality_prompts = {
            "general": "일반적인 인성면접 질문",
            "leadership": "리더십 관련 인성면접 질문",
            "teamwork": "팀워크 관련 인성면접 질문"
        }
    
    async def generate_questions(
        self, 
        interview_type: InterviewType, 
        job_position: str = None,
        experience_level: str = "intermediate",
        skills: List[str] = []
    ) -> List[InterviewQuestion]:
        """면접 질문 생성"""
        
        if interview_type == InterviewType.TECHNICAL:
            system_prompt = f"""
            당신은 {job_position or '소프트웨어 개발자'} 포지션의 기술면접관입니다.
            {experience_level} 수준의 지원자를 위한 기술면접 질문 5개를 생성해주세요.
            
            요구사항:
            - 기술 스킬: {', '.join(skills) if skills else '일반 프로그래밍'}
            - 난이도: {experience_level}
            - 각 질문은 구체적이고 실무 중심이어야 합니다
            
            JSON 형식으로 응답해주세요:
            {{
                "questions": [
                    {{"question": "질문 내용", "category": "카테고리", "difficulty": "난이도"}},
                    ...
                ]
            }}
            """
        else:  # PERSONALITY
            system_prompt = f"""
            당신은 {job_position or '일반 직무'} 포지션의 인성면접관입니다.
            지원자의 인성과 조직 적합성을 평가하는 질문 5개를 생성해주세요.
            
            요구사항:
            - 다양한 상황별 질문 포함
            - STAR 기법으로 답변 가능한 질문
            - 협업, 문제해결, 성장 의지 등을 평가할 수 있는 질문
            
            JSON 형식으로 응답해주세요:
            {{
                "questions": [
                    {{"question": "질문 내용", "category": "카테고리", "difficulty": "난이도"}},
                    ...
                ]
            }}
            """
        
        try:
            response = await openai_service.generate_completion([
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": "면접 질문을 생성해주세요."}
            ])
            
            # JSON 파싱
            result = json.loads(response)
            questions = []
            
            for q in result["questions"]:
                questions.append(InterviewQuestion(
                    question=q["question"],
                    category=q["category"],
                    difficulty=q["difficulty"]
                ))
            
            return questions
            
        except Exception as e:
            logger.error(f"면접 질문 생성 실패: {e}")
            raise HTTPException(status_code=500, detail="면접 질문 생성에 실패했습니다")
    
    async def evaluate_answer(
        self,
        question: str,
        answer: str,
        interview_type: InterviewType
    ) -> InterviewFeedback:
        """면접 답변 평가 - 새로운 피드백 형식"""

        system_prompt = f"""
        당신은 전문적인 면접관입니다.
        {'기술면접' if interview_type == InterviewType.TECHNICAL else '인성면접'} 답변을 평가해주세요.

        새로운 평가 기준에 따라 다음 6가지 항목으로 평가해주세요:
        1. 점수 (0-100점)
        2. 상세 피드백 (전반적인 답변 분석)
        3. 모범 답변 (구체적이고 실무적인 예시)
        4. 강점 (잘한 점 3가지)
        5. 개선사항 (보완할 점 3가지)
        6. 평가기준 (어떤 기준으로 평가했는지)

        JSON 형식으로 정확히 응답해주세요:
        {{
            "score": 점수,
            "detailed_feedback": "답변에 대한 상세한 분석과 피드백",
            "model_answer": "구체적이고 실무적인 모범 답변 예시",
            "strengths": ["강점1", "강점2", "강점3"],
            "improvements": ["개선사항1", "개선사항2", "개선사항3"],
            "evaluation_criteria": ["평가기준1", "평가기준2", "평가기준3"]
        }}
        """
        
        user_prompt = f"""
        질문: {question}
        
        지원자 답변: {answer}
        
        위 답변을 평가해주세요.
        """
        
        try:
            response = await openai_service.generate_completion([
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ])
            
            result = json.loads(response)

            return InterviewFeedback(
                score=result["score"],
                feedback=result.get("detailed_feedback", result.get("feedback", "")),
                strengths=result["strengths"],
                improvements=result["improvements"],
                example_answer=result.get("model_answer", result.get("example_answer", "")),
                evaluation_criteria=result.get("evaluation_criteria", [])
            )
            
        except Exception as e:
            logger.error(f"면접 답변 평가 실패: {e}")
            raise HTTPException(status_code=500, detail="면접 답변 평가에 실패했습니다")

interview_service = InterviewService()

@router.get("/health", response_model=APIResponse)
async def get_interview_health():
    """면접 서비스 상태 확인"""
    try:
        return APIResponse(
            success=True,
            message="면접 서비스가 정상 작동 중입니다",
            data={"status": "healthy", "service": "interview"}
        )
    except Exception as e:
        logger.error(f"면접 서비스 상태 확인 오류: {e}")
        return APIResponse(
            success=False,
            message="면접 서비스 상태 확인에 실패했습니다",
            error=str(e)
        )

@router.get("/demo-questions", response_model=APIResponse)
async def get_demo_interview_questions(
    interview_type: str = "technical",
    job_position: str = "소프트웨어 엔지니어", 
    experience_level: str = "intermediate"
):
    """면접 질문 데모 (GET 방식으로 간단 테스트)"""
    try:
        # 간단한 데모 질문들 반환
        if interview_type == "technical":
            questions = [
                {"question": f"{job_position} 포지션에 지원한 이유를 말씀해주세요.", "category": "동기", "difficulty": experience_level},
                {"question": "가장 자신있는 기술 스택에 대해 설명해주세요.", "category": "기술", "difficulty": experience_level},
                {"question": "최근에 해결한 어려운 기술적 문제가 있다면 설명해주세요.", "category": "문제해결", "difficulty": experience_level},
                {"question": "코드 리뷰는 어떻게 진행하시나요?", "category": "프로세스", "difficulty": experience_level},
                {"question": "새로운 기술을 학습할 때 어떤 방법을 사용하시나요?", "category": "학습", "difficulty": experience_level}
            ]
        else:  # personality
            questions = [
                {"question": "자기소개를 해주세요.", "category": "기본", "difficulty": experience_level},
                {"question": f"{job_position} 포지션에 지원한 이유는 무엇인가요?", "category": "동기", "difficulty": experience_level},
                {"question": "팀워크에서 갈등이 있었던 경험과 해결방법을 말해주세요.", "category": "협업", "difficulty": experience_level},
                {"question": "본인의 장점과 단점은 무엇인가요?", "category": "성격", "difficulty": experience_level},
                {"question": "5년 후 본인의 모습을 어떻게 그리고 계신가요?", "category": "비전", "difficulty": experience_level}
            ]
        
        return APIResponse(
            success=True,
            message="면접 질문이 성공적으로 생성되었습니다",
            data={"questions": questions, "total_count": len(questions)}
        )
    except Exception as e:
        logger.error(f"면접 질문 데모 오류: {e}")
        return APIResponse(
            success=False,
            message="면접 질문 생성에 실패했습니다",
            error=str(e)
        )

@router.get("/demo-feedback", response_model=APIResponse)
async def get_demo_interview_feedback(
    question: str = "자기소개를 해주세요",
    answer: str = "안녕하세요. 저는 3년차 소프트웨어 개발자입니다.",
    interview_type: str = "technical"
):
    """면접 답변 평가 데모 (GET 방식으로 간단 테스트)"""
    try:
        # 실제로 OpenAI를 사용해서 평가해보겠습니다
        interview_type_enum = InterviewType.TECHNICAL if interview_type == "technical" else InterviewType.PERSONALITY
        
        feedback = await interview_service.evaluate_answer(
            question=question,
            answer=answer,
            interview_type=interview_type_enum
        )
        
        return APIResponse(
            success=True,
            message="면접 답변이 성공적으로 평가되었습니다",
            data={
                "question": question,
                "answer": answer,
                "feedback": {
                    "score": feedback.score,
                    "detailed_feedback": feedback.feedback,
                    "model_answer": feedback.example_answer,
                    "strengths": feedback.strengths,
                    "improvements": feedback.improvements,
                    "evaluation_criteria": feedback.evaluation_criteria
                }
            }
        )
    except Exception as e:
        logger.error(f"면접 답변 평가 데모 오류: {e}")
        # 실패시 기본 피드백 반환
        return APIResponse(
            success=True,
            message="기본 피드백을 제공합니다",
            data={
                "question": question,
                "answer": answer,
                "feedback": {
                    "score": 75,
                    "detailed_feedback": "답변이 간결하고 명확합니다. 더 구체적인 경험이나 기술 스택을 언급하면 좋겠습니다.",
                    "model_answer": "안녕하세요. 저는 3년간 백엔드 개발을 담당한 개발자입니다. Java와 Spring Boot를 주로 사용하며, 최근에는 마이크로서비스 아키텍처 구축 프로젝트에서 30% 성능 향상을 달성했습니다.",
                    "strengths": ["간결한 표현", "명확한 전달", "경력 언급"],
                    "improvements": ["구체적 경험 추가", "기술 스택 설명", "성과 사례 언급"],
                    "evaluation_criteria": ["답변의 구조성", "전문성 표현", "구체성 및 명확성"]
                }
            }
        )


@router.post("/evaluate-answer", response_model=APIResponse)
async def evaluate_interview_answer(request: InterviewAnswer):
    """OpenAI 기반 전문적인 면접 답변 평가"""
    try:
        # interview_service.py의 evaluate_answer 사용 (실제 OpenAI 호출)
        result = await interview_service.evaluate_answer(
            question=request.question,
            answer=request.answer,
            position="소프트웨어 엔지니어",  # 기본값
            interview_type="technical"  # 기본값
        )
        
        if result["success"]:
            evaluation_data = result["evaluation"]
            return APIResponse(
                success=True,
                message="면접 답변이 성공적으로 평가되었습니다",
                data={
                    "feedback": {
                        "score": evaluation_data.get("score", 0),
                        "feedback": evaluation_data.get("feedback", ""),
                        "strengths": evaluation_data.get("strengths", []),
                        "improvements": evaluation_data.get("improvements", []),
                        "example_answer": evaluation_data.get("sample_answer", "")
                    },
                    "metadata": {
                        "question": result["question"],
                        "answer": result["answer"],
                        "evaluated_at": result["evaluated_at"]
                    }
                }
            )
        else:
            return APIResponse(
                success=False,
                message="면접 답변 평가에 실패했습니다",
                error=result["error"]
            )
    except Exception as e:
        logger.error(f"면접 답변 평가 API 오류: {e}")
        return APIResponse(
            success=False,
            message="면접 답변 평가에 실패했습니다",
            error=str(e)
        )

class InterviewCompleteRequest(BaseModel):
    user_id: int
    interview_type: InterviewType
    questions: List[str]
    answers: List[str]

@router.post("/complete-interview", response_model=APIResponse)
async def complete_interview(request: InterviewCompleteRequest):
    """전체 면접 완료 및 종합 평가"""
    try:
        # 각 답변에 대한 개별 평가
        feedbacks = []
        total_score = 0
        
        for question, answer in zip(request.questions, request.answers):
            feedback = await interview_service.evaluate_answer(
                question=question,
                answer=answer,
                interview_type=request.interview_type
            )
            feedbacks.append(feedback)
            total_score += feedback.score
        
        overall_score = total_score / len(feedbacks) if feedbacks else 0
        
        # 종합 평가 생성
        system_prompt = f"""
        당신은 전문 면접관입니다. 
        {'기술면접' if request.interview_type == InterviewType.TECHNICAL else '인성면접'}의 전체적인 종합 평가를 작성해주세요.
        
        개별 점수들을 바탕으로 지원자의 전반적인 역량과 개선 방향을 제시해주세요.
        """
        
        user_prompt = f"""
        면접 유형: {request.interview_type.value}
        개별 점수: {[f.score for f in feedbacks]}
        전체 평균: {overall_score:.1f}점
        
        종합 평가를 작성해주세요.
        """
        
        summary = await openai_service.generate_completion([
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt}
        ])
        
        result = InterviewResult(
            user_id=request.user_id,
            interview_type=request.interview_type,
            questions=[InterviewQuestion(question=q, category="", difficulty="") for q in request.questions],
            answers=request.answers,
            feedback=feedbacks,
            overall_score=overall_score,
            created_at=datetime.now()
        )
        
        return APIResponse(
            success=True,
            message="면접이 성공적으로 완료되었습니다",
            data={
                "result": result,
                "summary": summary
            }
        )
        
    except Exception as e:
        logger.error(f"면접 완료 API 오류: {e}")
        return APIResponse(
            success=False,
            message="면접 완료 처리에 실패했습니다",
            error=str(e)
        )

# 프론트엔드 호환성을 위한 API
class FrontendInterviewRequest(BaseModel):
    interviewType: str = "technical"  # 'technical' | 'personality'
    userId: Optional[int] = None  # optional
    jobPosition: Optional[str] = None  # optional
    experienceLevel: Optional[str] = None  # optional
    skills: Optional[List[str]] = []  # optional

@router.post("/generate-questions")
async def frontend_generate_questions(request: FrontendInterviewRequest):
    """프론트엔드 완전 호환 면접 질문 생성"""
    try:
        # 기본값 설정
        field = "backend"  # jobPosition에서 추출하거나 기본값
        experience_level = request.experienceLevel or "junior"
        job_title = request.jobPosition or "개발자"
        interview_type = request.interviewType or "technical"
        
        # 단순화된 API 사용
        result = await enhanced_openai_service.generate_completion_with_retry(
            messages=[
                {
                    "role": "system", 
                    "content": f"""
                    당신은 {job_title} 포지션의 {interview_type} 면접관입니다.
                    {experience_level} 레벨 지원자를 위한 면접 질문 5개를 생성해주세요.
                    
                    응답 형식:
                    1. 질문 1
                    2. 질문 2  
                    3. 질문 3
                    4. 질문 4
                    5. 질문 5
                    """
                },
                {
                    "role": "user",
                    "content": f"{job_title} 포지션의 {interview_type} 면접 질문을 생성해주세요."
                }
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
                    questions.append({
                        "question": question,
                        "category": interview_type,
                        "difficulty": experience_level
                    })
        
        return {
            "success": True,
            "message": "면접 질문이 생성되었습니다",
            "data": {
                "questions": questions[:5],
                "interviewType": interview_type,
                "jobPosition": job_title,
                "experienceLevel": experience_level,
                "userId": request.userId
            }
        }
        
    except Exception as e:
        logger.error(f"프론트엔드 면접 질문 생성 실패: {e}")
        return {
            "success": False,
            "message": "면접 질문 생성에 실패했습니다",
            "error": str(e)
        }

# 전문성이 강화된 새로운 엔드포인트들

class EnhancedInterviewRequest(BaseModel):
    field: str = "backend"  # backend, frontend, fullstack, devops, mobile, data, IT, 건축, 뷰티, 미디어, 금융
    experience_level: str = "mid"  # junior, mid, senior
    job_title: str = "소프트웨어 엔지니어"
    interview_type: str = "technical"  # technical, personality
    skills: List[str] = []
    company_context: str = ""

# 카테고리별 면접 질문 생성 요청 모델
class CategoryInterviewRequest(BaseModel):
    category: str  # "IT", "건축", "뷰티", "미디어", "금융"
    experience_level: str = "BEGINNER"  # "BEGINNER", "INTERMEDIATE", "ADVANCED"
    interview_type: str = "technical"  # "technical", "personality"
    job_position: str = ""  # 구체적인 직무명

@router.post("/professional/generate-questions", response_model=APIResponse)
async def generate_professional_interview_questions(request: EnhancedInterviewRequest):
    """전문성이 강화된 면접 질문 생성"""
    try:
        # 문자열을 InterviewType enum으로 변환
        interview_type = InterviewType.TECHNICAL if request.interview_type.lower() == "technical" else InterviewType.PERSONALITY
        
        questions = await enhanced_interview_service.generate_professional_questions(
            interview_type=interview_type,
            field=request.field,
            experience_level=request.experience_level,
            job_title=request.job_title,
            skills=request.skills,
            company_context=request.company_context
        )
        
        return APIResponse(
            success=True,
            message="전문적인 면접 질문이 성공적으로 생성되었습니다",
            data={
                "questions": [
                    {
                        "question": q.question,
                        "category": q.category,
                        "difficulty": q.difficulty
                    } for q in questions
                ],
                "metadata": {
                    "field": request.field,
                    "experience_level": request.experience_level,
                    "interview_type": request.interview_type,
                    "total_questions": len(questions)
                }
            }
        )
    except Exception as e:
        logger.error(f"전문 면접 질문 생성 API 오류: {e}")
        return APIResponse(
            success=False,
            message="전문적인 면접 질문 생성에 실패했습니다",
            error=str(e)
        )

class EnhancedInterviewAnswer(BaseModel):
    question: str
    answer: str
    field: str = "backend"
    experience_level: str = "mid"
    interview_type: str = "technical"

@router.post("/professional/evaluate-answer", response_model=APIResponse)
async def evaluate_professional_interview_answer(request: EnhancedInterviewAnswer):
    """전문성이 강화된 면접 답변 평가"""
    try:
        # 문자열을 InterviewType enum으로 변환
        interview_type = InterviewType.TECHNICAL if request.interview_type.lower() == "technical" else InterviewType.PERSONALITY
        
        feedback = await enhanced_interview_service.evaluate_answer_professionally(
            question=request.question,
            answer=request.answer,
            interview_type=interview_type,
            field=request.field,
            experience_level=request.experience_level
        )
        
        return APIResponse(
            success=True,
            message="전문적인 면접 답변 평가가 완료되었습니다",
            data={
                "evaluation": {
                    "overall_score": feedback.score,
                    "feedback": feedback.feedback,
                    "strengths": feedback.strengths,
                    "improvements": feedback.improvements,
                    "example_answer": feedback.example_answer
                },
                "metadata": {
                    "field": request.field,
                    "experience_level": request.experience_level,
                    "interview_type": request.interview_type,
                    "evaluation_date": datetime.now().isoformat()
                }
            }
        )
    except Exception as e:
        logger.error(f"전문 면접 답변 평가 API 오류: {e}")
        return APIResponse(
            success=False,
            message="전문적인 면접 답변 평가에 실패했습니다",
            error=str(e)
        )

@router.get("/professional/demo", response_model=APIResponse)
async def get_professional_demo(
    field: str = "backend",
    experience_level: str = "mid", 
    interview_type: str = "technical"
):
    """전문 면접 시스템 데모"""
    try:
        # 샘플 질문 생성
        interview_type_enum = InterviewType.TECHNICAL if interview_type.lower() == "technical" else InterviewType.PERSONALITY
        
        questions = await enhanced_interview_service.generate_professional_questions(
            interview_type=interview_type_enum,
            field=field,
            experience_level=experience_level,
            job_title="소프트웨어 엔지니어"
        )
        
        # 샘플 답변 평가
        sample_question = questions[0].question if questions else "자기소개를 해주세요"
        sample_answer = f"저는 {experience_level} 수준의 {field} 개발자입니다. 다양한 프로젝트 경험을 통해 실무 역량을 키워왔습니다."
        
        feedback = await enhanced_interview_service.evaluate_answer_professionally(
            question=sample_question,
            answer=sample_answer,
            interview_type=interview_type_enum,
            field=field,
            experience_level=experience_level
        )
        
        return APIResponse(
            success=True,
            message="전문 면접 시스템 데모가 준비되었습니다",
            data={
                "demo_questions": [
                    {
                        "question": q.question,
                        "category": q.category,
                        "difficulty": q.difficulty
                    } for q in questions[:3]  # 처음 3개만 데모로
                ],
                "sample_evaluation": {
                    "question": sample_question,
                    "answer": sample_answer,
                    "score": feedback.score,
                    "feedback": feedback.feedback,
                    "strengths": feedback.strengths,
                    "improvements": feedback.improvements
                },
                "system_info": {
                    "field": field,
                    "experience_level": experience_level,
                    "interview_type": interview_type,
                    "professional_features": [
                        "경력 수준별 맞춤 질문",
                        "기술 분야별 전문 질문",
                        "상세한 평가 기준",
                        "구체적인 개선 방향 제시",
                        "실무 중심 피드백"
                    ]
                }
            }
        )
    except Exception as e:
        logger.error(f"전문 면접 데모 오류: {e}")
        return APIResponse(
            success=False,
            message="전문 면접 시스템 데모 생성에 실패했습니다",
            error=str(e)
        )

# 프론트엔드 호환 면접 답변 평가 모델
class FrontendEvaluateRequest(BaseModel):
    question: str
    answer: str
    jobRole: str

@router.post("/evaluate-answer-frontend")
async def evaluate_answer_frontend_compatible(request: FrontendEvaluateRequest):
    """프론트엔드 완전 호환 면접 답변 평가 - Interview.tsx용"""
    try:
        system_prompt = f"""
        당신은 전문 면접관이자 평가 전문가입니다.
        다음 면접 답변을 전문적으로 평가해주세요.
        
        평가 기준:
        1. 답변의 완성도 (논리적 구성, 명확성)
        2. 기술적 정확성 (기술면접 기준)
        3. 직무 적합성 ({request.jobRole} 포지션 기준)
        4. 의사소통 능력
        
        JSON 형식으로 정확히 응답해주세요:
        {{
            "score": 점수(0-100),
            "strengths": ["강점1", "강점2", "강점3"],
            "improvements": ["개선점1", "개선점2", "개선점3"],
            "suggestion": "상세한 개선 제안 (2-3문장)"
        }}
        """
        
        user_prompt = f"""
        면접 질문: {request.question}
        
        지원자 답변: {request.answer}
        
        직무: {request.jobRole}
        
        위 답변을 평가해주세요.
        """
        
        result = await enhanced_openai_service.generate_completion_with_retry(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            temperature=0.3,
            max_tokens=1500
        )
        
        # JSON 파싱 시도
        try:
            evaluation = json.loads(result.get('content', '{}'))
        except json.JSONDecodeError:
            # JSON 파싱 실패시 기본 구조 생성
            evaluation = {
                "score": 75,
                "strengths": ["답변에 성실함이 드러남", "기본적인 이해도를 보임", "의사소통 의지가 있음"],
                "improvements": ["더 구체적인 예시 필요", "기술적 깊이 보완", "논리적 구성 개선"],
                "suggestion": "구체적인 경험과 예시를 포함하여 답변하시면 더 좋겠습니다."
            }
        
        return {
            "success": True,
            "message": "면접 답변 평가가 완료되었습니다",
            "data": {
                "feedback": evaluation
            }
        }
        
    except Exception as e:
        logger.error(f"프론트엔드 호환 면접 답변 평가 실패: {e}")
        return {
            "success": False,
            "message": "면접 답변 평가에 실패했습니다",
            "error": str(e)
        }

# 프론트엔드 호환 면접 완료 모델
class InterviewCompleteData(BaseModel):
    jobRole: str
    questions: List[dict]
    answers: List[dict]
    user_id: str = "guest"  # 사용자 ID, 기본값은 guest

@router.post("/complete")
async def complete_interview_frontend_compatible(request: InterviewCompleteData):
    """프론트엔드 완전 호환 면접 완료 처리 - Interview.tsx용"""
    try:
        import time
        from datetime import datetime
        
        logger.info(f"면접 완료 요청 처리 시작: jobRole={request.jobRole}")
        
        # 임시로 AI 서비스에서 직접 처리 (백엔드 문제 해결 전까지)
        # 면접 데이터 처리 및 통계 계산
        total_questions = len(request.questions)
        answered_questions = len(request.answers)
        
        # 점수 계산
        total_score = 0
        scored_answers = 0
        
        for answer in request.answers:
            if answer.get("feedback") and answer["feedback"].get("score"):
                total_score += answer["feedback"]["score"]
                scored_answers += 1
        
        overall_score = total_score / scored_answers if scored_answers > 0 else 0
        
        # 면접 결과 데이터 생성
        interview_result = {
            "interviewId": int(time.time()),  # 임시 ID
            "overallScore": round(overall_score, 1),
            "totalQuestions": total_questions,
            "answeredQuestions": answered_questions,
            "completedAt": datetime.now().isoformat(),
            "jobRole": request.jobRole,
            "questions": request.questions,
            "answers": request.answers
        }
        
        # 면접 기록 저장 (사용자별 분리)
        save_interview_record(request.user_id, interview_result)
        
        logger.info(f"면접 완료 처리 성공: 평균점수={overall_score:.1f}, 질문수={total_questions}")
        
        return {
            "success": True,
            "message": "면접이 성공적으로 완료되었습니다",
            "data": interview_result
        }
                
    except Exception as e:
        logger.error(f"면접 완료 처리 실패: {e}")
        return {
            "success": False,
            "message": "면접 완료 처리에 실패했습니다",
            "error": str(e)
        }

# 면접 기록 저장 (임시 파일 기반)
import os
import json
from typing import List

INTERVIEW_RECORDS_DIR = "data/interview_records"
os.makedirs(INTERVIEW_RECORDS_DIR, exist_ok=True)

def save_interview_record(user_id: str, interview_data: dict):
    """면접 기록 저장"""
    logger.info(f"면접 기록 저장 시작: user_id={user_id}, interview_id={interview_data.get('interviewId', 'N/A')}")
    user_file = os.path.join(INTERVIEW_RECORDS_DIR, f"user_{user_id}.json")
    logger.info(f"저장할 파일 경로: {user_file}")
    
    # 기존 기록 불러오기
    if os.path.exists(user_file):
        with open(user_file, 'r', encoding='utf-8') as f:
            records = json.load(f)
        logger.info(f"기존 기록 {len(records)}개 로드됨")
    else:
        records = []
        logger.info("새 기록 파일 생성")
    
    # 새 기록 추가
    records.append(interview_data)
    
    # 저장
    with open(user_file, 'w', encoding='utf-8') as f:
        json.dump(records, f, ensure_ascii=False, indent=2)
    
    logger.info(f"면접 기록 저장 완료: {user_file}, 총 {len(records)}개 기록")

def get_interview_records(user_id: str) -> List[dict]:
    """면접 기록 조회"""
    user_file = os.path.join(INTERVIEW_RECORDS_DIR, f"user_{user_id}.json")
    
    if os.path.exists(user_file):
        with open(user_file, 'r', encoding='utf-8') as f:
            return json.load(f)
    else:
        return []

@router.get("/history")
async def get_interview_history(user_id: str = "guest"):
    """면접 기록 조회 API"""
    try:
        records = get_interview_records(user_id)
        
        # 최신순으로 정렬
        records.sort(key=lambda x: x.get('completedAt', ''), reverse=True)
        
        return {
            "success": True,
            "data": {
                "interviews": records,
                "total": len(records)
            }
        }
        
    except Exception as e:
        logger.error(f"면접 기록 조회 실패: {e}")
        return {
            "success": False,
            "message": "면접 기록 조회에 실패했습니다",
            "error": str(e)
        }

class AIReviewRequest(BaseModel):
    interview_id: int
    user_id: str = "guest"  # 사용자 ID 추가

@router.post("/ai-review")
async def get_ai_review(request: AIReviewRequest):
    """AI 총평 생성 API - 사용자별 접근 제한"""
    try:
        # 해당 사용자의 면접 기록에서만 찾기 (보안 강화)
        user_records = get_interview_records(request.user_id)

        interview_data = None
        for record in user_records:
            if record.get('interviewId') == request.interview_id:
                interview_data = record
                break

        if not interview_data:
            raise HTTPException(status_code=404, detail="면접 기록을 찾을 수 없습니다")

        # AI 총평 생성
        ai_review = await generate_ai_review(interview_data)

        return {
            "success": True,
            "data": ai_review
        }

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"AI 총평 생성 실패: {e}")
        raise HTTPException(status_code=500, detail=f"AI 총평 생성에 실패했습니다: {str(e)}")

async def generate_ai_review(interview_data: dict) -> dict:
    """AI 총평 생성 함수"""
    try:
        questions = interview_data.get('questions', [])
        answers = interview_data.get('answers', [])
        job_role = interview_data.get('jobRole', '개발자')
        
        # 질문별 상세 분석 생성
        detailed_analysis = []
        
        for question in questions:
            question_id = question.get('id')
            question_text = question.get('question')
            
            # 해당 질문의 답변 찾기
            user_answer = None
            for answer in answers:
                if answer.get('questionId') == question_id:
                    user_answer = answer.get('answer')
                    break
            
            if user_answer:
                # AI로 개별 질문 분석
                analysis_prompt = f"""
                면접 질문과 지원자의 답변을 분석해주세요.
                
                직무: {job_role}
                질문: {question_text}
                지원자 답변: {user_answer}
                
                다음 형식으로 분석해주세요:
                1. 점수 (0-100점)
                2. 잘한 점 (3개 이하)
                3. 부족한 점 (3개 이하)
                4. 개선 방안 (3개 이하)
                5. 모범 답안 (구체적이고 실무적인 답변)
                6. 추천사항 (1-2문장)
                
                JSON 형식으로 응답해주세요:
                {{
                    "score": 85,
                    "strengths": ["강점1", "강점2"],
                    "weaknesses": ["약점1", "약점2"],
                    "improvements": ["개선점1", "개선점2"],
                    "modelAnswer": "모범 답안 텍스트",
                    "recommendation": "추천사항 텍스트"
                }}
                """
                
                analysis_response = await enhanced_openai_service.generate_completion_with_retry(
                    analysis_prompt,
                    max_tokens=1500
                )
                
                try:
                    analysis_result = json.loads(analysis_response)
                    detailed_analysis.append({
                        "questionId": question_id,
                        "question": question_text,
                        "userAnswer": user_answer,
                        "score": analysis_result.get("score", 70),
                        "strengths": analysis_result.get("strengths", []),
                        "weaknesses": analysis_result.get("weaknesses", []),
                        "improvements": analysis_result.get("improvements", []),
                        "modelAnswer": analysis_result.get("modelAnswer", ""),
                        "recommendation": analysis_result.get("recommendation", "")
                    })
                except json.JSONDecodeError:
                    # JSON 파싱 실패 시 기본값 사용
                    detailed_analysis.append({
                        "questionId": question_id,
                        "question": question_text,
                        "userAnswer": user_answer,
                        "score": 70,
                        "strengths": ["답변을 제공했습니다"],
                        "weaknesses": ["더 구체적인 설명이 필요합니다"],
                        "improvements": ["실무 경험을 포함한 답변을 해보세요"],
                        "modelAnswer": "구체적인 예시와 함께 경험을 설명하는 것이 좋습니다.",
                        "recommendation": "더 자세한 설명을 추가해보세요."
                    })
        
        # 전체 평가 생성
        avg_score = sum(analysis.get("score", 0) for analysis in detailed_analysis) / len(detailed_analysis) if detailed_analysis else 0
        
        overall_prompt = f"""
        {job_role} 면접의 전체적인 평가를 해주세요.
        
        총 {len(questions)}개 질문에 대한 평균 점수: {avg_score:.1f}점
        
        전체적인 면접 성과에 대한 종합 평가와 향후 개선 방향을 제시해주세요.
        3-4문장으로 작성해주세요.
        """
        
        overall_assessment = await enhanced_openai_service.generate_completion_with_retry(
            overall_prompt,
            max_tokens=500
        )
        
        # 전체 추천사항 생성
        recommendations_prompt = f"""
        {job_role} 면접 결과를 바탕으로 지원자에게 도움이 될 전체적인 추천사항을 5개 제시해주세요.
        실무적이고 구체적인 조언으로 작성해주세요.
        
        JSON 배열 형식으로 응답해주세요:
        ["추천사항1", "추천사항2", "추천사항3", "추천사항4", "추천사항5"]
        """
        
        recommendations_response = await enhanced_openai_service.generate_completion_with_retry(
            recommendations_prompt,
            max_tokens=800
        )
        
        try:
            recommendations = json.loads(recommendations_response)
        except json.JSONDecodeError:
            recommendations = [
                "면접 경험을 쌓기 위해 모의 면접을 더 진행해보세요",
                "기술적인 질문에 대한 실무 경험을 포함해서 답변하세요",
                "구체적인 예시와 수치를 활용한 답변을 준비하세요",
                "면접 전 해당 기업과 직무에 대한 충분한 조사를 하세요",
                "자신의 강점을 어필할 수 있는 스토리를 준비하세요"
            ]
        
        return {
            "overallAssessment": overall_assessment,
            "detailedAnalysis": detailed_analysis,
            "totalScore": round(avg_score, 1),
            "recommendations": recommendations
        }
        
    except Exception as e:
        logger.error(f"AI 총평 생성 중 오류: {e}")
        # 기본 응답 반환
        return {
            "overallAssessment": "면접 분석을 완료했습니다. 전반적으로 양호한 수준의 답변을 보여주셨습니다.",
            "detailedAnalysis": [],
            "totalScore": 70.0,
            "recommendations": ["면접 경험을 더 쌓아보시기 바랍니다."]
        }

# 카테고리별 면접 질문 생성 API
@router.post("/category/generate-questions", response_model=APIResponse)
async def generate_category_interview_questions(request: CategoryInterviewRequest):
    """카테고리별 맞춤형 면접 질문 생성 (IT, 건축, 뷰티, 미디어, 금융)"""
    try:
        # 카테고리별 전문 질문 템플릿
        category_templates = {
            "IT": {
                "technical": {
                    "BEGINNER": [
                        "프로그래밍 언어 중 가장 자신있는 것은 무엇이며, 그 이유는?",
                        "데이터베이스와 관련된 기본적인 개념에 대해 설명해주세요.",
                        "웹 개발 시 프론트엔드와 백엔드의 차이점은 무엇인가요?",
                        "버전 관리 시스템(Git)을 사용해본 경험이 있나요?",
                        "소프트웨어 개발 생명주기(SDLC)에 대해 알고 계시나요?"
                    ],
                    "INTERMEDIATE": [
                        "RESTful API 설계 원칙에 대해 설명해주세요.",
                        "데이터베이스 정규화와 비정규화의 장단점은?",
                        "클라우드 서비스(AWS, Azure 등) 사용 경험이 있나요?",
                        "CI/CD 파이프라인 구축 경험에 대해 말씀해주세요.",
                        "마이크로서비스 아키텍처의 장단점은 무엇인가요?"
                    ],
                    "ADVANCED": [
                        "대규모 시스템 아키텍처 설계 경험을 말씀해주세요.",
                        "성능 최적화를 위한 전략과 실제 경험은?",
                        "팀의 기술 스택 결정 과정에서 고려사항은 무엇인가요?",
                        "레거시 시스템 마이그레이션 전략에 대해 설명해주세요.",
                        "기술 리더로서 팀원들의 성장을 어떻게 지원하시나요?"
                    ]
                },
                "personality": [
                    "IT 분야에서 일하고 싶은 이유는 무엇인가요?",
                    "기술 트렌드를 어떻게 따라가고 계시나요?",
                    "어려운 기술적 문제를 해결한 경험이 있나요?",
                    "팀 프로젝트에서 갈등을 어떻게 해결하시나요?",
                    "5년 후 IT 업계에서의 본인의 모습을 그려보세요."
                ]
            },
            "건축": {
                "technical": {
                    "BEGINNER": [
                        "건축 설계의 기본 원리에 대해 설명해주세요.",
                        "CAD 프로그램 사용 경험이 있나요?",
                        "건축 구조의 기본적인 종류들을 설명해주세요.",
                        "건축법규의 중요성에 대해 어떻게 생각하시나요?",
                        "친환경 건축에 대한 관심이나 지식이 있나요?"
                    ],
                    "INTERMEDIATE": [
                        "BIM(Building Information Modeling) 경험이 있나요?",
                        "시공 현장에서의 품질 관리 경험을 말씀해주세요.",
                        "건축물의 구조 안전성 검토 과정을 설명해주세요.",
                        "프로젝트 일정 관리에서 중요한 포인트는?",
                        "건축주와의 의견 충돌을 어떻게 해결하시나요?"
                    ],
                    "ADVANCED": [
                        "대규모 프로젝트 관리 경험을 말씀해주세요.",
                        "건축 트렌드와 미래 전망에 대한 견해는?",
                        "팀원들의 업무 분배와 관리는 어떻게 하시나요?",
                        "설계 변경 시 발생하는 문제들을 어떻게 해결하시나요?",
                        "지속가능한 건축을 위한 전략은 무엇인가요?"
                    ]
                },
                "personality": [
                    "건축 분야를 선택한 동기는 무엇인가요?",
                    "가장 인상적이었던 건축물이 있다면?",
                    "창의적인 아이디어를 어떻게 현실화시키시나요?",
                    "스트레스가 많은 상황에서 어떻게 대처하시나요?",
                    "건축가로서 사회적 책임에 대해 어떻게 생각하시나요?"
                ]
            },
            "뷰티": {
                "technical": {
                    "BEGINNER": [
                        "피부 타입별 관리법에 대해 설명해주세요.",
                        "기본적인 메이크업 순서를 말씀해주세요.",
                        "헤어컷의 기본 기법들을 알고 계시나요?",
                        "색상 이론과 뷰티에의 적용에 대해 설명해주세요.",
                        "위생과 안전 관리의 중요성에 대해 어떻게 생각하시나요?"
                    ],
                    "INTERMEDIATE": [
                        "트렌디한 메이크업 기법 중 전문적으로 다루는 것은?",
                        "고객 상담 시 고려해야 할 요소들은 무엇인가요?",
                        "뷰티 제품의 성분과 효과에 대한 지식이 있나요?",
                        "특수 메이크업(웨딩, 무대 등) 경험이 있나요?",
                        "고객 불만 처리 경험과 해결 방법을 말씀해주세요."
                    ],
                    "ADVANCED": [
                        "뷰티 샵 운영 관리 경험이 있나요?",
                        "직원 교육과 관리는 어떻게 하시나요?",
                        "뷰티 트렌드 분석과 예측은 어떻게 하시나요?",
                        "고객 관계 관리 전략을 말씀해주세요.",
                        "뷰티 산업의 미래 전망에 대한 견해는?"
                    ]
                },
                "personality": [
                    "뷰티 업계에서 일하고 싶은 이유는?",
                    "아름다움에 대한 본인만의 철학이 있나요?",
                    "까다로운 고객을 만났을 때 어떻게 대처하시나요?",
                    "새로운 뷰티 트렌드를 어떻게 습득하시나요?",
                    "뷰티 전문가로서 갖춰야 할 덕목은 무엇인가요?"
                ]
            },
            "미디어": {
                "technical": {
                    "BEGINNER": [
                        "영상 편집 프로그램 사용 경험이 있나요?",
                        "카메라의 기본 설정과 촬영 기법을 알고 계시나요?",
                        "스토리텔링의 기본 구조를 설명해주세요.",
                        "소셜미디어 플랫폼별 특성을 알고 계시나요?",
                        "저작권과 관련된 기본 지식이 있나요?"
                    ],
                    "INTERMEDIATE": [
                        "콘텐츠 기획부터 제작까지의 프로세스를 설명해주세요.",
                        "타겟 오디언스 분석은 어떻게 하시나요?",
                        "라이브 스트리밍이나 실시간 방송 경험이 있나요?",
                        "데이터 분석을 통한 콘텐츠 개선 방법은?",
                        "다양한 미디어 채널 관리 경험을 말씀해주세요."
                    ],
                    "ADVANCED": [
                        "미디어 전략 수립과 실행 경험이 있나요?",
                        "크리에이티브 팀 관리와 방향 설정은 어떻게 하시나요?",
                        "미디어 트렌드 변화에 어떻게 대응하시나요?",
                        "브랜드 아이덴티티와 일관성 유지 전략은?",
                        "미디어 산업의 디지털 전환에 대한 견해는?"
                    ]
                },
                "personality": [
                    "미디어 분야에 관심을 갖게 된 계기는?",
                    "창의적인 아이디어는 주로 어디서 얻으시나요?",
                    "마감 압박이 있는 상황에서 어떻게 대처하시나요?",
                    "팀워크가 중요한 프로젝트 경험이 있나요?",
                    "미디어의 사회적 영향력에 대해 어떻게 생각하시나요?"
                ]
            },
            "금융": {
                "technical": {
                    "BEGINNER": [
                        "기본적인 회계 원리에 대해 설명해주세요.",
                        "재무제표의 종류와 각각의 역할은 무엇인가요?",
                        "은행업무의 기본적인 종류들을 설명해주세요.",
                        "금리의 개념과 경제에 미치는 영향은?",
                        "리스크 관리의 중요성에 대해 어떻게 생각하시나요?"
                    ],
                    "INTERMEDIATE": [
                        "투자 포트폴리오 구성 원리를 설명해주세요.",
                        "신용평가 과정과 기준에 대해 알고 계시나요?",
                        "파생상품의 종류와 특징을 설명해주세요.",
                        "금융 규제와 컴플라이언스 경험이 있나요?",
                        "디지털 금융 서비스 트렌드에 대한 견해는?"
                    ],
                    "ADVANCED": [
                        "금융 상품 개발과 전략 수립 경험이 있나요?",
                        "리스크 관리 체계 구축 경험을 말씀해주세요.",
                        "금융 시장 분석과 예측은 어떻게 하시나요?",
                        "팀 성과 관리와 목표 설정은 어떻게 하시나요?",
                        "핀테크 시대의 전통 금융업 전략은 무엇인가요?"
                    ]
                },
                "personality": [
                    "금융 분야를 선택한 이유는 무엇인가요?",
                    "숫자와 데이터 분석에 대한 관심이 있나요?",
                    "고객과의 신뢰 관계는 어떻게 구축하시나요?",
                    "윤리적 딜레마 상황에서 어떻게 판단하시나요?",
                    "금융 전문가로서 사회적 책임에 대한 생각은?"
                ]
            }
        }

        category = request.category
        interview_type = request.interview_type
        experience_level = request.experience_level
        job_position = request.job_position or f"{category} 전문가"

        # 카테고리별 질문 선택
        if category not in category_templates:
            raise HTTPException(status_code=400, detail=f"지원하지 않는 카테고리입니다: {category}")

        template = category_templates[category]

        if interview_type == "technical":
            if experience_level not in template["technical"]:
                experience_level = "BEGINNER"  # 기본값
            questions_pool = template["technical"][experience_level]
        else:  # personality
            questions_pool = template["personality"]

        # AI를 활용한 추가 질문 생성
        system_prompt = f"""
        당신은 {category} 분야의 전문 면접관입니다.
        {job_position} 포지션의 {experience_level} 수준 지원자를 위한 {interview_type} 면접 질문을 생성해주세요.

        다음 기본 질문들을 참고하여 더욱 전문적이고 구체적인 질문 5개를 만들어주세요:
        {', '.join(questions_pool[:3])}

        요구사항:
        - {category} 분야의 최신 트렌드 반영
        - {experience_level} 수준에 맞는 난이도
        - 실무 중심의 구체적인 질문
        - 지원자의 역량을 평가할 수 있는 질문

        JSON 형식으로 응답해주세요:
        {{
            "questions": [
                {{"question": "질문 내용", "category": "{category}", "difficulty": "{experience_level}"}},
                ...
            ]
        }}
        """

        try:
            response = await enhanced_openai_service.generate_completion_with_retry(
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": f"{category} 분야의 {interview_type} 면접 질문을 생성해주세요."}
                ],
                temperature=0.7,
                max_tokens=1500
            )

            # JSON 파싱 시도
            try:
                result = json.loads(response.get('content', '{}'))
                ai_questions = result.get("questions", [])
            except json.JSONDecodeError:
                # JSON 파싱 실패 시 기본 질문 사용
                ai_questions = [
                    {"question": q, "category": category, "difficulty": experience_level}
                    for q in questions_pool[:5]
                ]

            # 기본 질문과 AI 질문 결합
            final_questions = []

            # 기본 질문 2개
            for i, q in enumerate(questions_pool[:2]):
                final_questions.append({
                    "id": i + 1,
                    "question": q,
                    "category": category,
                    "difficulty": experience_level
                })

            # AI 질문 3개
            for i, q in enumerate(ai_questions[:3]):
                final_questions.append({
                    "id": len(final_questions) + 1,
                    "question": q.get("question", ""),
                    "category": category,
                    "difficulty": experience_level
                })

            return APIResponse(
                success=True,
                message=f"{category} 분야 맞춤형 면접 질문이 생성되었습니다",
                data={
                    "questions": final_questions,
                    "metadata": {
                        "category": category,
                        "interview_type": interview_type,
                        "experience_level": experience_level,
                        "job_position": job_position,
                        "total_questions": len(final_questions)
                    }
                }
            )

        except Exception as e:
            logger.error(f"AI 질문 생성 실패, 기본 질문 사용: {e}")
            # AI 실패시 기본 질문만 사용
            final_questions = [
                {
                    "id": i + 1,
                    "question": q,
                    "category": category,
                    "difficulty": experience_level
                }
                for i, q in enumerate(questions_pool[:5])
            ]

            return APIResponse(
                success=True,
                message=f"{category} 분야 기본 면접 질문이 제공되었습니다",
                data={
                    "questions": final_questions,
                    "metadata": {
                        "category": category,
                        "interview_type": interview_type,
                        "experience_level": experience_level,
                        "job_position": job_position,
                        "total_questions": len(final_questions)
                    }
                }
            )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"카테고리별 면접 질문 생성 실패: {e}")
        return APIResponse(
            success=False,
            message="카테고리별 면접 질문 생성에 실패했습니다",
            error=str(e)
        )

# 카테고리별 면접 답변 평가
class CategoryEvaluateRequest(BaseModel):
    question: str
    answer: str
    category: str  # "IT", "건축", "뷰티", "미디어", "금융"
    experience_level: str = "BEGINNER"  # "BEGINNER", "INTERMEDIATE", "ADVANCED"
    interview_type: str = "technical"

@router.post("/category/evaluate-answer", response_model=APIResponse)
async def evaluate_category_interview_answer(request: CategoryEvaluateRequest):
    """카테고리별 맞춤형 면접 답변 평가"""
    try:
        # 카테고리별 평가 기준
        evaluation_criteria = {
            "IT": {
                "기술적 정확성": "기술 개념의 정확한 이해와 적용",
                "실무 경험": "실제 프로젝트 경험과 문제 해결 능력",
                "학습 능력": "새로운 기술 습득과 적응력",
                "협업 능력": "팀 프로젝트에서의 소통과 협업"
            },
            "건축": {
                "설계 역량": "건축 설계의 기본 원리와 창의성",
                "구조적 이해": "건축 구조와 안전성에 대한 이해",
                "법규 지식": "건축법규와 규정에 대한 지식",
                "프로젝트 관리": "일정 관리와 품질 관리 능력"
            },
            "뷰티": {
                "기술적 숙련도": "뷰티 기법의 숙련도와 트렌드 이해",
                "고객 서비스": "고객 상담과 서비스 마인드",
                "위생 관리": "안전과 위생 관리에 대한 인식",
                "창의성": "독창적이고 창의적인 접근"
            },
            "미디어": {
                "창작 능력": "콘텐츠 기획과 창작 역량",
                "기술적 활용": "미디어 도구와 기술 활용 능력",
                "트렌드 분석": "미디어 트렌드 파악과 적응력",
                "소통 능력": "다양한 채널에서의 소통 능력"
            },
            "금융": {
                "전문 지식": "금융 상품과 시장에 대한 이해",
                "분석 능력": "데이터 분석과 리스크 평가 능력",
                "고객 신뢰": "고객과의 신뢰 관계 구축",
                "윤리 의식": "금융 윤리와 컴플라이언스 인식"
            }
        }

        category = request.category
        if category not in evaluation_criteria:
            raise HTTPException(status_code=400, detail=f"지원하지 않는 카테고리입니다: {category}")

        criteria = evaluation_criteria[category]
        criteria_text = "\n".join([f"- {k}: {v}" for k, v in criteria.items()])

        system_prompt = f"""
        당신은 {category} 분야의 전문 면접관이자 평가 전문가입니다.
        다음 평가 기준에 따라 면접 답변을 전문적으로 평가해주세요.

        {category} 분야 평가 기준:
        {criteria_text}

        평가 요소:
        1. 답변의 완성도와 논리성 (25%)
        2. {category} 전문 지식 수준 (25%)
        3. 실무 적용 가능성 (25%)
        4. 의사소통 능력 (25%)

        JSON 형식으로 정확히 응답해주세요:
        {{
            "score": 점수(0-100),
            "detailed_feedback": "{category} 분야 관점에서의 전문적인 상세 피드백",
            "model_answer": "{category} 분야에 특화된 구체적이고 실무적인 모범 답변 예시",
            "strengths": ["{category} 관련 강점1", "{category} 관련 강점2", "{category} 관련 강점3"],
            "improvements": ["{category} 관련 개선사항1", "{category} 관련 개선사항2", "{category} 관련 개선사항3"],
            "evaluation_criteria": ["{category} 평가기준1", "{category} 평가기준2", "{category} 평가기준3"]
        }}
        """

        user_prompt = f"""
        분야: {category}
        경험 수준: {request.experience_level}
        면접 유형: {request.interview_type}

        면접 질문: {request.question}
        지원자 답변: {request.answer}

        위 답변을 {category} 분야 전문가 관점에서 평가해주세요.
        """

        result = await enhanced_openai_service.generate_completion_with_retry(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            temperature=0.3,
            max_tokens=1500
        )

        # JSON 파싱
        try:
            evaluation = json.loads(result.get('content', '{}'))
        except json.JSONDecodeError:
            # JSON 파싱 실패시 기본 구조 생성
            evaluation = {
                "score": 75,
                "detailed_feedback": f"{category} 분야 전문가로 성장하기 위한 기초는 갖추고 있으나, 더 구체적이고 전문적인 내용이 필요합니다.",
                "model_answer": f"{category} 분야의 실무 경험과 구체적인 사례를 포함한 답변이 좋겠습니다. 전문 용어를 적절히 활용하고 최신 트렌드를 반영한 내용으로 답변해보세요.",
                "strengths": [f"{category} 분야에 대한 기본적인 이해", "성실한 답변 태도", "의사소통 의지"],
                "improvements": [f"{category} 전문 용어 활용 부족", "구체적인 예시 필요", "실무 경험 보완"],
                "evaluation_criteria": [f"{category} 전문 지식", "실무 적용 능력", "의사소통 능력"]
            }

        return APIResponse(
            success=True,
            message=f"{category} 분야 전문 면접 답변 평가가 완료되었습니다",
            data={
                "feedback": evaluation,
                "metadata": {
                    "category": category,
                    "experience_level": request.experience_level,
                    "interview_type": request.interview_type,
                    "evaluation_criteria": list(criteria.keys())
                }
            }
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"카테고리별 면접 답변 평가 실패: {e}")
        return APIResponse(
            success=False,
            message="카테고리별 면접 답변 평가에 실패했습니다",
            error=str(e)
        )