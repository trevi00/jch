import os
from typing import List, Dict, Any, Optional
from pathlib import Path
import logging

from llama_index.core import VectorStoreIndex, SimpleDirectoryReader, ServiceContext
from llama_index.core.memory import ChatMemoryBuffer
from llama_index.core.chat_engine import CondenseQuestionChatEngine
from llama_index.llms.openai import OpenAI
from llama_index.embeddings.openai import OpenAIEmbedding
from llama_index.core import Settings
from llama_index.core.storage import StorageContext
from llama_index.core.node_parser import SimpleNodeParser

from config.settings import settings

logger = logging.getLogger(__name__)

class ChatbotService:
    def __init__(self):
        # OpenAI 설정
        Settings.llm = OpenAI(
            model="gpt-3.5-turbo",
            temperature=0.1,  # 정확한 답변을 위해 낮은 온도
            api_key=settings.OPENAI_API_KEY
        )
        Settings.embed_model = OpenAIEmbedding(
            model="text-embedding-ada-002",
            api_key=settings.OPENAI_API_KEY
        )
        
        # 지식베이스 경로
        self.knowledge_base_path = "knowledge_base"
        self.index_path = "data/chatbot_index"
        
        # 인덱스 및 채팅 엔진
        self.index = None
        self.chat_engine = None
        
        # 사용자별 채팅 히스토리 (메모리)
        self.user_sessions = {}
        
        # 초기화
        self._load_or_create_index()
        self._create_chat_engine()
    
    def _load_or_create_index(self):
        """지식베이스 인덱스 로드 또는 생성"""
        try:
            index_dir = Path(self.index_path)
            
            if index_dir.exists() and any(index_dir.iterdir()):
                # 기존 인덱스 로드
                logger.info("기존 챗봇 인덱스를 로드합니다...")
                storage_context = StorageContext.from_defaults(persist_dir=self.index_path)
                self.index = VectorStoreIndex.load_from_storage(storage_context)
                logger.info("챗봇 인덱스 로드 완료")
            else:
                # 새 인덱스 생성
                self._create_index()
                
        except Exception as e:
            logger.error(f"인덱스 로드 실패, 새로 생성합니다: {e}")
            self._create_index()
    
    def _create_index(self):
        """새로운 지식베이스 인덱스 생성"""
        logger.info("새로운 챗봇 인덱스를 생성합니다...")
        
        try:
            # 문서 로드
            if not os.path.exists(self.knowledge_base_path):
                logger.warning(f"지식베이스 경로가 없습니다: {self.knowledge_base_path}")
                # 기본 FAQ 문서 생성
                Path(self.knowledge_base_path).mkdir(parents=True, exist_ok=True)
            
            documents = SimpleDirectoryReader(self.knowledge_base_path).load_data()
            
            if not documents:
                logger.warning("로드된 문서가 없습니다. 기본 응답 모드로 작동합니다.")
                # 빈 인덱스 생성
                from llama_index.core import Document
                dummy_doc = Document(text="잡았다 고객지원 챗봇입니다. 궁금한 것이 있으시면 언제든 문의해주세요.")
                documents = [dummy_doc]
            
            # 노드 파서 설정
            node_parser = SimpleNodeParser.from_defaults(
                chunk_size=512,
                chunk_overlap=20
            )
            
            # 인덱스 생성
            self.index = VectorStoreIndex.from_documents(
                documents,
                node_parser=node_parser
            )
            
            # 인덱스 저장
            Path(self.index_path).mkdir(parents=True, exist_ok=True)
            self.index.storage_context.persist(persist_dir=self.index_path)
            
            logger.info(f"챗봇 인덱스 생성 완료: {len(documents)}개 문서 처리")
            
        except Exception as e:
            logger.error(f"인덱스 생성 실패: {e}")
            raise
    
    def _create_chat_engine(self):
        """채팅 엔진 생성"""
        if self.index is None:
            logger.error("인덱스가 없어 채팅 엔진을 생성할 수 없습니다")
            return
        
        try:
            # 메모리 설정 (대화 히스토리 유지)
            memory = ChatMemoryBuffer.from_defaults(token_limit=3000)
            
            # 채팅 엔진 생성
            self.chat_engine = CondenseQuestionChatEngine.from_defaults(
                query_engine=self.index.as_query_engine(
                    similarity_top_k=5,
                    response_mode="compact"
                ),
                memory=memory,
                verbose=True
            )
            
            logger.info("채팅 엔진 생성 완료")
            
        except Exception as e:
            logger.error(f"채팅 엔진 생성 실패: {e}")
            raise
    
    def get_user_session(self, user_id: str) -> Dict[str, Any]:
        """사용자 세션 가져오기 또는 생성"""
        if user_id not in self.user_sessions:
            self.user_sessions[user_id] = {
                "chat_history": [],
                "created_at": logger.time.now() if hasattr(logger, 'time') else None
            }
        return self.user_sessions[user_id]
    
    def clear_user_session(self, user_id: str):
        """사용자 세션 초기화"""
        if user_id in self.user_sessions:
            del self.user_sessions[user_id]
            logger.info(f"사용자 {user_id}의 세션이 초기화되었습니다")
    
    async def chat(self, user_id: str, message: str) -> Dict[str, Any]:
        """챗봇과 대화"""
        if not self.chat_engine:
            return {
                "success": False,
                "response": "죄송합니다. 현재 챗봇 서비스를 이용할 수 없습니다. 잠시 후 다시 시도해주세요.",
                "error": "chat_engine_not_available"
            }
        
        try:
            # 사용자 세션 가져오기
            session = self.get_user_session(user_id)
            
            logger.info(f"사용자 {user_id} 질문: {message}")
            
            # 한국어 답변을 유도하는 메시지 추가
            korean_prompt = f"[한국어로만 답변] 당신은 '잡았다' 취업 플랫폼의 전문 상담사입니다. 친근하고 정중한 한국어로 취업 관련 조언을 제공해주세요. 질문: {message}"
            
            # 채팅 엔진으로 응답 생성
            response = self.chat_engine.chat(korean_prompt)
            response_text = str(response)
            
            # 세션 히스토리에 추가
            session["chat_history"].append({
                "user": message,
                "assistant": response_text,
                "timestamp": logger.time.now() if hasattr(logger, 'time') else None
            })
            
            # 히스토리 길이 제한 (최근 10개 대화만 유지)
            if len(session["chat_history"]) > 10:
                session["chat_history"] = session["chat_history"][-10:]
            
            logger.info(f"사용자 {user_id} 응답 생성 완료")
            
            return {
                "success": True,
                "response": response_text,
                "user_id": user_id,
                "message": message
            }
            
        except Exception as e:
            logger.error(f"챗봇 대화 처리 실패: {e}")
            return {
                "success": False,
                "response": "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                "error": str(e)
            }
    
    async def get_suggested_questions(self) -> List[str]:
        """추천 질문 목록 반환"""
        return [
            "회원가입은 어떻게 하나요?",
            "AI 면접 기능을 사용하려면 어떻게 해야 하나요?",
            "자소서 생성 기능에 대해 알려주세요",
            "증명서 신청은 어떻게 하나요?",
            "채용공고는 어디서 확인할 수 있나요?",
            "비밀번호를 잊어버렸어요",
            "파일 업로드가 안 되는데 어떻게 해야 하나요?",
            "플랫폼 이용료가 있나요?",
            "문의는 어떻게 할 수 있나요?"
        ]
    
    async def get_chat_categories(self) -> Dict[str, List[str]]:
        """문의 카테고리별 예시 질문"""
        return {
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
    
    def get_chat_history(self, user_id: str) -> List[Dict[str, Any]]:
        """사용자 채팅 히스토리 조회"""
        session = self.get_user_session(user_id)
        return session.get("chat_history", [])
    
    def get_health_status(self) -> Dict[str, Any]:
        """챗봇 서비스 상태 확인"""
        return {
            "chatbot_service": "healthy" if self.chat_engine else "unavailable",
            "index_status": "loaded" if self.index else "not_loaded",
            "active_sessions": len(self.user_sessions),
            "knowledge_base_path": self.knowledge_base_path
        }

# 싱글톤 인스턴스
chatbot_service = ChatbotService()