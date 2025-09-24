from fastapi import APIRouter, HTTPException
from typing import Dict, Any
import logging
from datetime import datetime
from pydantic import BaseModel

from models.schemas import APIResponse
from services.enhanced_openai_service import enhanced_openai_service

logger = logging.getLogger(__name__)
router = APIRouter()

class SentimentAnalysisService:
    def __init__(self):
        self.emotion_icons = {
            "positive": "😊",
            "negative": "😢", 
            "neutral": "😐"
        }
        
        self.emotion_keywords = {
            "positive": ["joyful", "bright", "cheerful", "optimistic", "energetic"],
            "negative": ["melancholy", "dark", "somber", "reflective", "moody"],
            "neutral": ["calm", "peaceful", "balanced", "serene", "stable"]
        }
    
    async def analyze_sentiment(self, text: str, language: str = "ko") -> Dict[str, Any]:
        """전문적인 텍스트 감정 분석"""
        
        system_prompt = """
        당신은 자연어처리 및 감정 분석 분야의 전문가입니다.
        다음 기준에 따라 텍스트를 정밀하게 분석해주세요:
        
        분석 차원:
        1. 기본 감정 (Primary Emotion)
           - positive: 긍정적, 기쁨, 희망, 만족, 열정
           - negative: 부정적, 슬픔, 분노, 실망, 우려
           - neutral: 중립적, 객관적, 사실 기술
        
        2. 감정 강도 (Intensity)
           - 매우 강함 (90-100)
           - 강함 (70-89)
           - 보통 (40-69)
           - 약함 (20-39)
           - 매우 약함 (0-19)
        
        3. 감정 뉘앙스 (Nuance)
           - 직접적 vs 간접적 표현
           - 명시적 vs 암시적 감정
           - 단순 vs 복합 감정
        
        응답 형식:
        감정: [positive/negative/neutral]
        신뢰도: [0-100]
        강도: [매우강함/강함/보통/약함/매우약함]
        주요 키워드: [감정을 나타내는 핵심 단어들]
        분석 근거: [간단한 설명]
        """
        
        try:
            result = await enhanced_openai_service.generate_completion_with_retry(
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": f"다음 텍스트를 분석해주세요:\n\n텍스트: {text}\n언어: {language}"}
                ],
                max_tokens=200,
                temperature=0.3,  # 더 일관된 분석을 위해 낮은 온도
                use_cache=True
            )
            
            response = result.get('content', '')
            
            # 고급 응답 파싱
            lines = response.strip().split('\n')
            sentiment = "neutral"
            confidence = 50.0
            intensity = "보통"
            keywords = []
            reasoning = ""
            
            for line in lines:
                if "감정:" in line:
                    sentiment_raw = line.split("감정:")[1].strip().lower()
                    if sentiment_raw in ["positive", "negative", "neutral"]:
                        sentiment = sentiment_raw
                elif "신뢰도:" in line:
                    try:
                        confidence = float(line.split("신뢰도:")[1].strip().replace('%', ''))
                        confidence = max(0, min(100, confidence))
                    except:
                        confidence = 50.0
                elif "강도:" in line:
                    intensity = line.split("강도:")[1].strip()
                elif "주요 키워드:" in line:
                    keywords_str = line.split("주요 키워드:")[1].strip()
                    keywords = [k.strip() for k in keywords_str.split(',') if k.strip()]
                elif "분석 근거:" in line:
                    reasoning = line.split("분석 근거:")[1].strip()
            
            # 백엔드 DTO와 호환되는 형식으로 변환
            # confidence를 0-1 범위로 변환하고, score로도 사용 (positive: +값, negative: -값)
            score_normalized = confidence / 100.0  # 0-1 범위로 정규화
            if sentiment == "negative":
                score_normalized = -score_normalized  # negative는 음수
            elif sentiment == "neutral":
                score_normalized = 0.0  # neutral은 0
                
            # 전문적인 분석 결과 구성
            analysis_result = {
                "label": sentiment,
                "score": score_normalized,
                "confidence": confidence / 100.0,
                "explanation": reasoning if reasoning else f"분석된 감정: {sentiment}, 신뢰도: {confidence:.1f}%, 강도: {intensity}",
                # 향상된 정보
                "sentiment": sentiment,
                "emotion_icon": self.emotion_icons.get(sentiment, "😐"),
                "intensity": intensity,
                "keywords": keywords if keywords else self.emotion_keywords.get(sentiment, ["balanced"]),
                "analyzed_text": text[:100] + "..." if len(text) > 100 else text,
                # 메타데이터
                "metadata": {
                    "model": result.get('model', 'unknown'),
                    "quality_score": result.get('quality_score', 0),
                    "analysis_timestamp": result.get('timestamp', '')
                }
            }
            
            return analysis_result
            
        except Exception as e:
            logger.error(f"감정 분석 실패: {e}")
            return {
                "label": "neutral",
                "score": 0.0,
                "confidence": 0.0,
                "explanation": f"감정 분석 중 오류 발생: {str(e)}",
                # 추가 정보 (호환성을 위해 유지)
                "sentiment": "neutral",
                "emotion_icon": "😐",
                "keywords": ["unknown"],
                "analyzed_text": text[:100] + "..." if len(text) > 100 else text,
                "error": str(e)
            }

sentiment_service = SentimentAnalysisService()

@router.get("/health", response_model=APIResponse)
async def get_sentiment_health():
    """감정분석 서비스 상태 확인"""
    try:
        return APIResponse(
            success=True,
            message="감정분석 서비스가 정상 작동 중입니다",
            data={"status": "healthy", "service": "sentiment_analysis", "openai_available": True}
        )
    except Exception as e:
        logger.error(f"감정분석 서비스 상태 확인 오류: {e}")
        return APIResponse(
            success=False,
            message="감정분석 서비스 상태 확인에 실패했습니다",
            error=str(e)
        )

class SentimentAnalyzeRequest(BaseModel):
    text: str
    language: str = "ko"

@router.post("/analyze", response_model=APIResponse)
async def analyze_sentiment_endpoint(request: SentimentAnalyzeRequest):
    """텍스트 감정 분석"""
    try:
        if not request.text or not request.text.strip():
            raise HTTPException(status_code=400, detail="분석할 텍스트가 필요합니다")
        
        result = await sentiment_service.analyze_sentiment(request.text, request.language)
        
        return APIResponse(
            success=True,
            message="감정 분석이 완료되었습니다",
            data=result
        )
        
    except Exception as e:
        logger.error(f"감정 분석 API 오류: {e}")
        return APIResponse(
            success=False,
            message="감정 분석에 실패했습니다",
            error=str(e)
        )

@router.get("/emotions", response_model=APIResponse)
async def get_emotion_info():
    """지원하는 감정 정보 조회"""
    try:
        emotions = []
        for emotion, icon in sentiment_service.emotion_icons.items():
            emotions.append({
                "emotion": emotion,
                "icon": icon,
                "keywords": sentiment_service.emotion_keywords.get(emotion, []),
                "display_name": {
                    "positive": "긍정적",
                    "negative": "부정적", 
                    "neutral": "중립적"
                }.get(emotion, emotion)
            })
        
        return APIResponse(
            success=True,
            message="지원하는 감정 정보입니다",
            data={"emotions": emotions}
        )
        
    except Exception as e:
        logger.error(f"감정 정보 조회 API 오류: {e}")
        return APIResponse(
            success=False,
            message="감정 정보 조회에 실패했습니다",
            error=str(e)
        )