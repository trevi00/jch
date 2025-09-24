from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime
from enum import Enum

# 면접 관련 스키마
class InterviewType(str, Enum):
    TECHNICAL = "technical"
    PERSONALITY = "personality"

class InterviewRequest(BaseModel):
    interview_type: InterviewType
    user_id: int
    job_position: Optional[str] = None
    experience_level: Optional[str] = None
    skills: Optional[List[str]] = Field(default_factory=list)

class InterviewQuestion(BaseModel):
    question: str
    category: str
    difficulty: str

class InterviewAnswer(BaseModel):
    question: str
    answer: str
    user_id: int

class InterviewFeedback(BaseModel):
    score: float = Field(..., ge=0, le=100)
    feedback: str  # 상세 피드백
    strengths: List[str]  # 강점
    improvements: List[str]  # 개선사항
    example_answer: str  # 모범 답변
    evaluation_criteria: Optional[List[str]] = Field(default_factory=list)  # 평가기준

class InterviewResult(BaseModel):
    user_id: int
    interview_type: InterviewType
    questions: List[InterviewQuestion]
    answers: List[str]
    feedback: List[InterviewFeedback]
    overall_score: float
    created_at: datetime

# 자소서 생성 관련 스키마
class CoverLetterSection(str, Enum):
    MOTIVATION = "motivation"  # 지원동기
    GROWTH = "growth"         # 성장과정
    STRENGTHS = "strengths"   # 나의 장점
    COMMUNICATION = "communication"  # 커뮤니케이션

class CoverLetterRequest(BaseModel):
    user_id: int
    company_name: str
    position: str
    sections: List[CoverLetterSection]
    user_info: Optional[Dict[str, Any]] = Field(default_factory=dict)

class CoverLetterResponse(BaseModel):
    user_id: int
    company_name: str
    position: str
    content: Dict[str, str]  # section -> content
    created_at: datetime

# 번역 관련 스키마
class DocumentType(str, Enum):
    RESUME = "resume"
    COVER_LETTER = "cover_letter"
    EMAIL = "email"
    BUSINESS = "business"
    GENERAL = "general"

class TranslationRequest(BaseModel):
    text: str
    source_language: str = "ko"
    target_language: str = "en"
    document_type: DocumentType = DocumentType.GENERAL

class TranslationResponse(BaseModel):
    original_text: str
    translated_text: str
    source_language: str
    target_language: str
    document_type: DocumentType
    created_at: datetime

# 이미지 생성 관련 스키마
class ImageGenerationRequest(BaseModel):
    prompt: str
    user_id: int
    style: Optional[str] = "realistic"
    size: Optional[str] = "1024x1024"

class ImageGenerationResponse(BaseModel):
    user_id: int
    prompt: str
    image_url: str
    created_at: datetime

# 번역 품질 평가 관련 스키마
class TranslationEvaluationRequest(BaseModel):
    original: str
    translated: str
    source_language: str = "ko"
    target_language: str = "en"

# 공통 응답 스키마
class APIResponse(BaseModel):
    success: bool
    message: str
    data: Optional[Any] = None
    error: Optional[str] = None