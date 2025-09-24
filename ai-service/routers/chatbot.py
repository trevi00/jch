from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any, Optional
import logging
from datetime import datetime

from models.schemas import APIResponse
from services.chatbot_service import chatbot_service
from utils.security import security_utils
from pydantic import BaseModel

logger = logging.getLogger(__name__)
router = APIRouter()

# 요청/응답 모델
class ChatRequest(BaseModel):
    user_id: str
    message: str

class ChatResponse(BaseModel):
    user_id: str
    message: str
    response: str
    timestamp: datetime
    success: bool

class ChatHistoryResponse(BaseModel):
    user_id: str
    history: List[Dict[str, Any]]
    total_messages: int

@router.post("/chat", response_model=APIResponse)
async def chat_with_bot(request: ChatRequest):
    """챗봇과 대화하기"""
    try:
        # 입력 검증 및 sanitize
        user_id_validation = security_utils.validate_user_input(request.user_id, 100)
        message_validation = security_utils.validate_user_input(request.message, 5000)
        
        if not user_id_validation["valid"]:
            return APIResponse(
                success=False,
                message=f"사용자 ID 오류: {user_id_validation['error']}",
                error="invalid_user_id"
            )
        
        if not message_validation["valid"]:
            return APIResponse(
                success=False,
                message=f"메시지 오류: {message_validation['error']}",
                error="invalid_message"
            )
        
        # Sanitized 된 데이터 사용
        clean_user_id = user_id_validation["sanitized"]
        clean_message = message_validation["sanitized"]
        
        # 챗봇 서비스 호출
        result = await chatbot_service.chat(clean_user_id, clean_message)
        
        if result["success"]:
            # 응답 데이터도 sanitize
            safe_response = security_utils.sanitize_html(result["response"])
            
            response_data = ChatResponse(
                user_id=clean_user_id,
                message=clean_message,
                response=safe_response,
                timestamp=datetime.now(),
                success=True
            )
            
            return APIResponse(
                success=True,
                message="응답이 생성되었습니다",
                data=response_data.model_dump()
            )
        else:
            return APIResponse(
                success=False,
                message="챗봇 응답 생성에 실패했습니다",
                error=result.get("error", "unknown_error"),
                data={"response": result["response"]}
            )
            
    except Exception as e:
        logger.error(f"챗봇 대화 API 오류: {e}")
        return APIResponse(
            success=False,
            message="서비스 오류가 발생했습니다",
            error=str(e)
        )

@router.get("/suggestions", response_model=APIResponse)
async def get_suggested_questions():
    """추천 질문 목록 조회"""
    try:
        suggestions = await chatbot_service.get_suggested_questions()
        
        return APIResponse(
            success=True,
            message="추천 질문을 조회했습니다",
            data={"suggestions": suggestions}
        )
        
    except Exception as e:
        logger.error(f"추천 질문 조회 API 오류: {e}")
        return APIResponse(
            success=False,
            message="추천 질문 조회에 실패했습니다",
            error=str(e)
        )

@router.get("/categories", response_model=APIResponse)
async def get_chat_categories():
    """문의 카테고리별 예시 질문 조회"""
    try:
        categories = await chatbot_service.get_chat_categories()
        
        return APIResponse(
            success=True,
            message="문의 카테고리를 조회했습니다",
            data={"categories": categories}
        )
        
    except Exception as e:
        logger.error(f"문의 카테고리 조회 API 오류: {e}")
        return APIResponse(
            success=False,
            message="문의 카테고리 조회에 실패했습니다",
            error=str(e)
        )

@router.get("/history/{user_id}", response_model=APIResponse)
async def get_chat_history(user_id: str):
    """사용자 채팅 히스토리 조회"""
    try:
        history = chatbot_service.get_chat_history(user_id)
        
        response_data = ChatHistoryResponse(
            user_id=user_id,
            history=history,
            total_messages=len(history)
        )
        
        return APIResponse(
            success=True,
            message="채팅 히스토리를 조회했습니다",
            data=response_data.model_dump()
        )
        
    except Exception as e:
        logger.error(f"채팅 히스토리 조회 API 오류: {e}")
        return APIResponse(
            success=False,
            message="채팅 히스토리 조회에 실패했습니다",
            error=str(e)
        )

@router.delete("/history/{user_id}", response_model=APIResponse)
async def clear_chat_history(user_id: str):
    """사용자 채팅 히스토리 초기화"""
    try:
        chatbot_service.clear_user_session(user_id)
        
        return APIResponse(
            success=True,
            message="채팅 히스토리가 초기화되었습니다",
            data={"user_id": user_id}
        )
        
    except Exception as e:
        logger.error(f"채팅 히스토리 초기화 API 오류: {e}")
        return APIResponse(
            success=False,
            message="채팅 히스토리 초기화에 실패했습니다",
            error=str(e)
        )

@router.get("/health", response_model=APIResponse)
async def get_chatbot_health():
    """챗봇 서비스 상태 확인"""
    try:
        health_status = chatbot_service.get_health_status()
        
        return APIResponse(
            success=True,
            message="챗봇 서비스 상태를 확인했습니다",
            data=health_status
        )
        
    except Exception as e:
        logger.error(f"챗봇 상태 확인 API 오류: {e}")
        return APIResponse(
            success=False,
            message="챗봇 상태 확인에 실패했습니다",
            error=str(e)
        )

@router.post("/quick-response", response_model=APIResponse)
async def quick_response(category: str, question_type: str, user_id: str):
    """빠른 응답 (자주 묻는 질문)"""
    try:
        # 카테고리별 빠른 응답 매핑
        quick_responses = {
            "account": {
                "signup": "회원가입은 구글 OAuth 또는 이메일 회원가입 두 가지 방법이 있습니다. 메인 페이지에서 '회원가입' 버튼을 클릭해주세요.",
                "password": "비밀번호 찾기는 로그인 페이지의 '비밀번호 찾기'를 클릭하시면 가입 이메일로 재설정 링크를 보내드립니다.",
                "withdrawal": "회원탈퇴는 마이페이지 > 계정 설정 > 회원탈퇴에서 가능합니다. 탈퇴 후 30일간 복구 가능합니다."
            },
            "platform": {
                "interview": "AI 면접은 로그인 후 'AI 면접' 메뉴에서 이용 가능합니다. 기술면접과 인성면접 중 선택할 수 있습니다.",
                "cover_letter": "자소서 생성은 기업과 직무를 입력하면 AI가 단계별 질문을 제공하여 전문적인 자소서를 생성해드립니다.",
                "translation": "문서 번역은 '문서 번역' 메뉴에서 문서 유형을 선택하고 텍스트를 입력하면 이용 가능합니다."
            },
            "technical": {
                "login": "로그인 문제가 있으시면 이메일/비밀번호 확인, 브라우저 캐시 삭제를 시도해보세요.",
                "upload": "파일 업로드는 최대 10MB, PDF/DOC/DOCX/JPG/PNG 형식을 지원합니다.",
                "loading": "페이지 로딩이 느리시면 브라우저 캐시를 삭제하고 새로고침해주세요."
            }
        }
        
        response_text = quick_responses.get(category, {}).get(question_type, 
            "해당 질문에 대한 빠른 응답을 찾을 수 없습니다. 구체적인 질문을 입력해주세요.")
        
        return APIResponse(
            success=True,
            message="빠른 응답을 제공했습니다",
            data={
                "user_id": user_id,
                "category": category,
                "question_type": question_type,
                "response": response_text,
                "timestamp": datetime.now()
            }
        )
        
    except Exception as e:
        logger.error(f"빠른 응답 API 오류: {e}")
        return APIResponse(
            success=False,
            message="빠른 응답 제공에 실패했습니다",
            error=str(e)
        )