#!/usr/bin/env python3
"""
SSL 문제 우회용 간단한 챗봇 서버
실제 AI 모델 없이 Mock 응답으로 API 테스트
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
import uvicorn
from datetime import datetime
import json

# FastAPI 앱 생성
app = FastAPI(title="잡았다 AI Service - 간단 챗봇 버전", version="1.0.0")

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

class APIResponse(BaseModel):
    success: bool
    message: str
    data: Optional[Dict[str, Any]] = None
    error: Optional[str] = None

# 메모리 저장소 (실제로는 데이터베이스 사용)
chat_history = {}

# Mock 응답 데이터베이스
mock_responses = {
    "회원가입": "잡았다 플랫폼 회원가입은 구글 OAuth 또는 이메일로 가능합니다. 메인 페이지에서 '회원가입' 버튼을 클릭해주세요.",
    "sign up": "You can sign up for the JBD platform using Google OAuth or email. Please click the 'Sign Up' button on the main page.",
    "signup": "You can sign up for the JBD platform using Google OAuth or email. Please click the 'Sign Up' button on the main page.",
    "로그인": "로그인 문제가 있으시면 이메일/비밀번호를 확인하고, 브라우저 캐시를 삭제해보세요.",
    "login": "If you have login issues, please check your email/password and clear your browser cache.",
    "AI 면접": "AI 면접 기능은 로그인 후 'AI 면접' 메뉴에서 이용 가능합니다. 기술면접과 인성면접을 선택할 수 있습니다.",
    "ai interview": "AI Interview feature is available in the 'AI Interview' menu after login. You can choose technical or personality interviews.",
    "interview": "AI Interview feature is available in the 'AI Interview' menu after login. You can choose technical or personality interviews.",
    "자소서": "자소서 생성 기능은 기업과 직무를 입력하면 AI가 단계별 질문을 제공하여 맞춤형 자소서를 생성해드립니다.",
    "cover letter": "The cover letter generation feature provides step-by-step questions from AI to create customized cover letters when you input company and job information.",
    "번역": "문서 번역 기능은 '문서 번역' 메뉴에서 문서 유형을 선택하고 텍스트를 입력하면 이용 가능합니다.",
    "translate": "Document translation is available in the 'Document Translation' menu where you select document type and input text.",
    "증명서": "증명서 신청은 마이페이지에서 원하는 증명서 종류를 선택하여 신청할 수 있습니다.",
    "certificate": "Certificate applications can be made by selecting the desired certificate type in My Page.",
    "안녕": "안녕하세요! 잡았다 AI 챗봇입니다. 무엇을 도와드릴까요?",
    "hello": "Hello! I'm the JBD AI Chatbot. How can I help you?",
    "hi": "Hello! I'm the JBD AI Chatbot. How can I help you?",
    "도움": "저는 회원가입, 로그인, AI 면접, 자소서 생성, 문서 번역, 증명서 발급 등에 대해 도움을 드릴 수 있습니다.",
    "help": "I can help with sign up, login, AI interviews, cover letter generation, document translation, certificate issuance, and more.",
    "thank": "You're welcome! Feel free to ask if you need more help with the JBD platform."
}

def get_mock_response(message: str) -> str:
    """메시지에 따른 Mock 응답 생성"""
    message_lower = message.lower().strip()
    
    # 키워드 매칭
    for keyword, response in mock_responses.items():
        if keyword in message_lower:
            return response
    
    # 기본 응답
    return "죄송합니다. 구체적인 질문을 입력해주시면 더 정확한 답변을 드릴 수 있습니다. 예: '회원가입 방법', 'AI 면접 사용법', '자소서 생성' 등"

@app.get("/")
async def root():
    return {
        "message": "잡았다 AI Service - 간단 챗봇 버전",
        "version": "1.0.0",
        "status": "running",
        "features": ["챗봇 대화", "빠른 응답", "히스토리 관리"],
        "docs": "/docs"
    }

@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "chatbot_service": "mock_running",
        "ssl_bypass": True,
        "timestamp": datetime.now().isoformat()
    }

@app.post("/api/v1/chatbot/chat")
async def chat_with_bot(request: ChatRequest):
    """챗봇과 대화하기 - Mock 버전"""
    try:
        if not request.message.strip():
            return APIResponse(
                success=False,
                message="메시지를 입력해주세요",
                error="empty_message"
            )
        
        # Mock 응답 생성
        bot_response = get_mock_response(request.message)
        
        # 히스토리 저장
        if request.user_id not in chat_history:
            chat_history[request.user_id] = []
        
        chat_entry = {
            "user": request.message,
            "assistant": bot_response,
            "timestamp": datetime.now().isoformat()
        }
        
        chat_history[request.user_id].append(chat_entry)
        
        # 히스토리 길이 제한 (최근 10개만 유지)
        if len(chat_history[request.user_id]) > 10:
            chat_history[request.user_id] = chat_history[request.user_id][-10:]
        
        response_data = ChatResponse(
            user_id=request.user_id,
            message=request.message,
            response=bot_response,
            timestamp=datetime.now(),
            success=True
        )
        
        return APIResponse(
            success=True,
            message="응답이 생성되었습니다",
            data=response_data.dict()
        )
        
    except Exception as e:
        return APIResponse(
            success=False,
            message="서비스 오류가 발생했습니다",
            error=str(e)
        )

@app.get("/api/v1/chatbot/suggestions")
async def get_suggested_questions():
    """추천 질문 목록 조회"""
    suggestions = [
        "회원가입은 어떻게 하나요?",
        "AI 면접 기능을 사용하려면 어떻게 해야 하나요?", 
        "자소서 생성 기능에 대해 알려주세요",
        "증명서 신청은 어떻게 하나요?",
        "채용공고는 어디서 확인할 수 있나요?",
        "비밀번호를 잊어버렸어요",
        "문서 번역은 어떻게 사용하나요?",
        "플랫폼 이용료가 있나요?",
        "문의는 어떻게 할 수 있나요?"
    ]
    
    return APIResponse(
        success=True,
        message="추천 질문을 조회했습니다",
        data={"suggestions": suggestions}
    )

@app.get("/api/v1/chatbot/categories")
async def get_chat_categories():
    """문의 카테고리별 예시 질문 조회"""
    categories = {
        "계정 관련": [
            "회원가입 방법",
            "비밀번호 찾기",
            "회원 유형 변경", 
            "탈퇴 방법"
        ],
        "플랫폼 기능": [
            "AI 면접 이용 방법",
            "자소서 생성 기능",
            "문서 번역 사용법", 
            "커뮤니티 이용 가이드"
        ],
        "증명서": [
            "증명서 종류",
            "신청 방법",
            "발급 소요 시간",
            "다운로드 방법"
        ],
        "기술 지원": [
            "로그인 문제",
            "파일 업로드 오류",
            "페이지 로딩 문제",
            "브라우저 호환성"
        ]
    }
    
    return APIResponse(
        success=True,
        message="문의 카테고리를 조회했습니다",
        data={"categories": categories}
    )

@app.get("/api/v1/chatbot/history/{user_id}")
async def get_chat_history(user_id: str):
    """사용자 채팅 히스토리 조회"""
    user_history = chat_history.get(user_id, [])
    
    return APIResponse(
        success=True,
        message="채팅 히스토리를 조회했습니다",
        data={
            "user_id": user_id,
            "history": user_history,
            "total_messages": len(user_history)
        }
    )

@app.delete("/api/v1/chatbot/history/{user_id}")
async def clear_chat_history(user_id: str):
    """사용자 채팅 히스토리 초기화"""
    if user_id in chat_history:
        del chat_history[user_id]
    
    return APIResponse(
        success=True,
        message="채팅 히스토리가 초기화되었습니다",
        data={"user_id": user_id}
    )

@app.get("/api/v1/chatbot/status")
async def get_chatbot_status():
    """챗봇 서비스 상태 확인"""
    return APIResponse(
        success=True,
        message="챗봇 서비스 상태를 확인했습니다",
        data={
            "chatbot_service": "healthy",
            "index_status": "mock_loaded",
            "active_sessions": len(chat_history),
            "knowledge_base_path": "mock_knowledge_base",
            "ssl_bypass": True
        }
    )

if __name__ == "__main__":
    print("Starting JBD AI Service - Simple Chatbot Server...")
    print("URL: http://localhost:8001") 
    print("Chatbot API: http://localhost:8001/api/v1/chatbot/chat")
    print("Suggestions: http://localhost:8001/api/v1/chatbot/suggestions")
    print("Docs: http://localhost:8001/docs")
    print("Exit: Ctrl+C")
    print("=" * 60)
    
    uvicorn.run(
        app,
        host="localhost",
        port=8001,
        reload=False,  # SSL 문제 방지
        log_level="info"
    )