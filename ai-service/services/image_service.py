#!/usr/bin/env python3
"""
이미지 생성 서비스 - DALL-E를 이용한 AI 이미지 생성
"""

import logging
from typing import List, Dict, Any, Optional
import json
from datetime import datetime
import re

from services.openai_service import openai_service
from config import settings

logger = logging.getLogger(__name__)

class ImageService:
    """이미지 생성 서비스 클래스"""
    
    def __init__(self):
        """서비스 초기화"""
        self.client = openai_service.client
        self.available_styles = {
            "professional": {
                "name": "프로페셔널",
                "description": "비즈니스 및 전문직에 적합한 깔끔하고 신뢰감 있는 스타일",
                "prompt_suffix": "professional, clean, business-like, high quality"
            },
            "creative": {
                "name": "크리에이티브",
                "description": "창의적이고 예술적인 분위기의 독창적인 스타일",
                "prompt_suffix": "creative, artistic, innovative, unique design"
            },
            "modern": {
                "name": "모던",
                "description": "현대적이고 세련된 미니멀한 디자인 스타일",
                "prompt_suffix": "modern, sleek, minimalist, contemporary"
            },
            "friendly": {
                "name": "친근한",
                "description": "따뜻하고 접근하기 쉬운 친근한 분위기",
                "prompt_suffix": "friendly, warm, approachable, welcoming"
            },
            "celebration": {
                "name": "축하",
                "description": "기쁨과 성취를 표현하는 축하 분위기",
                "prompt_suffix": "celebratory, joyful, achievement, success, happy"
            },
            "motivational": {
                "name": "동기부여",
                "description": "영감을 주고 동기를 부여하는 에너지 넘치는 스타일",
                "prompt_suffix": "motivational, inspiring, energetic, uplifting"
            }
        }
        
        self.size_options = ["1024x1024", "1792x1024", "1024x1792"]
        self.sentiment_keywords = {
            "positive": ["기쁨", "행복", "성공", "축하", "감사", "좋은", "멋진", "훌륭한"],
            "excited": ["흥미", "신나는", "재미", "활기", "에너지", "열정"],
            "proud": ["자랑", "성취", "이뤘", "해냈", "자신감", "뿌듯"],
            "grateful": ["감사", "고마운", "도움", "지지", "응원"],
            "neutral": ["일반", "보통", "평범", "기본"],
            "concerned": ["걱정", "불안", "어려운", "힘든", "문제"]
        }
    
    async def generate_image(
        self,
        prompt: str,
        style: str = "professional",
        size: str = "1024x1024",
        quality: str = "standard"
    ) -> Dict[str, Any]:
        """기본 이미지 생성"""
        try:
            logger.info(f"이미지 생성 요청 - 스타일: {style}, 크기: {size}")
            
            if style not in self.available_styles:
                return {
                    "success": False,
                    "error": f"지원하지 않는 스타일: {style}"
                }
            
            if size not in self.size_options:
                size = "1024x1024"  # 기본값
            
            # 프롬프트 향상
            enhanced_prompt = self._enhance_prompt(prompt, style)
            
            response = self.client.images.generate(
                model="dall-e-3",
                prompt=enhanced_prompt,
                size=size,
                quality=quality,
                n=1
            )
            
            image_url = response.data[0].url
            
            logger.info("이미지 생성 완료")
            
            return {
                "success": True,
                "image_url": image_url,
                "original_prompt": prompt,
                "enhanced_prompt": enhanced_prompt,
                "style": style,
                "size": size,
                "quality": quality,
                "generated_at": datetime.now().isoformat()
            }
            
        except Exception as e:
            logger.error(f"이미지 생성 실패: {e}")
            return {
                "success": False,
                "error": str(e)
            }
    
    async def generate_with_sentiment(
        self,
        text: str,
        style_preference: Optional[str] = None
    ) -> Dict[str, Any]:
        """감정 분석 기반 이미지 생성"""
        try:
            logger.info("감정 분석 기반 이미지 생성 시작")
            
            # 감정 분석
            sentiment = self.analyze_sentiment(text)
            
            # 스타일 결정
            if not style_preference:
                style_preference = self._get_style_from_sentiment(sentiment["primary_sentiment"])
            
            # 이미지 프롬프트 생성
            image_prompt = self._create_sentiment_based_prompt(text, sentiment)
            
            # 이미지 생성
            result = await self.generate_image(image_prompt, style_preference)
            
            if result["success"]:
                result["sentiment_analysis"] = sentiment
                result["text_input"] = text
            
            return result
            
        except Exception as e:
            logger.error(f"감정 기반 이미지 생성 실패: {e}")
            return {
                "success": False,
                "error": str(e)
            }
    
    async def generate_variations(
        self,
        base_prompt: str,
        variation_count: int = 3,
        style: str = "professional"
    ) -> Dict[str, Any]:
        """이미지 변형 생성"""
        try:
            logger.info(f"이미지 변형 {variation_count}개 생성")
            
            variations = []
            
            for i in range(variation_count):
                # 프롬프트에 변형 요소 추가
                varied_prompt = self._create_variation_prompt(base_prompt, i)
                
                result = await self.generate_image(varied_prompt, style)
                if result["success"]:
                    variations.append({
                        "variation_id": i + 1,
                        "image_url": result["image_url"],
                        "prompt": result["enhanced_prompt"]
                    })
            
            return {
                "success": True,
                "base_prompt": base_prompt,
                "variation_count": len(variations),
                "variations": variations,
                "style": style,
                "generated_at": datetime.now().isoformat()
            }
            
        except Exception as e:
            logger.error(f"이미지 변형 생성 실패: {e}")
            return {
                "success": False,
                "error": str(e)
            }
    
    def analyze_sentiment(self, text: str) -> Dict[str, Any]:
        """텍스트 감정 분석"""
        try:
            sentiment_scores = {}
            text_lower = text.lower()
            
            # 각 감정별 키워드 매칭
            for sentiment, keywords in self.sentiment_keywords.items():
                score = 0
                for keyword in keywords:
                    if keyword in text_lower:
                        score += 1
                sentiment_scores[sentiment] = score
            
            # 주요 감정 결정
            primary_sentiment = max(sentiment_scores, key=sentiment_scores.get)
            confidence = sentiment_scores[primary_sentiment] / len(text.split()) if text.split() else 0
            
            # 감정 강도 계산
            intensity = min(1.0, confidence * 5)  # 0-1 범위로 정규화
            
            return {
                "primary_sentiment": primary_sentiment,
                "confidence": round(confidence, 2),
                "intensity": round(intensity, 2),
                "all_scores": sentiment_scores,
                "text_length": len(text),
                "analyzed_at": datetime.now().isoformat()
            }
            
        except Exception as e:
            logger.error(f"감정 분석 실패: {e}")
            return {
                "primary_sentiment": "neutral",
                "confidence": 0.5,
                "intensity": 0.5,
                "all_scores": {"neutral": 1},
                "text_length": len(text),
                "error": str(e)
            }
    
    def get_styles(self) -> Dict[str, Any]:
        """사용 가능한 스타일 목록 조회"""
        return {
            "success": True,
            "styles": self.available_styles,
            "size_options": self.size_options
        }
    
    def _enhance_prompt(self, prompt: str, style: str) -> str:
        """프롬프트 향상"""
        style_info = self.available_styles.get(style, self.available_styles["professional"])
        
        enhanced = f"{prompt}, {style_info['prompt_suffix']}"
        
        # 품질 향상 키워드 추가
        enhanced += ", detailed, well-composed, high resolution"
        
        return enhanced
    
    def _get_style_from_sentiment(self, sentiment: str) -> str:
        """감정에 따른 스타일 선택"""
        sentiment_style_map = {
            "positive": "celebration",
            "excited": "creative", 
            "proud": "professional",
            "grateful": "friendly",
            "neutral": "modern",
            "concerned": "professional"
        }
        
        return sentiment_style_map.get(sentiment, "professional")
    
    def _create_sentiment_based_prompt(self, text: str, sentiment: Dict[str, Any]) -> str:
        """감정 기반 프롬프트 생성"""
        primary_sentiment = sentiment["primary_sentiment"]
        intensity = sentiment["intensity"]
        
        # 기본 이미지 설명
        base_prompt = "Abstract representation of success and achievement"
        
        # 감정별 추가 요소
        if primary_sentiment == "positive" and intensity > 0.7:
            base_prompt += ", bright colors, uplifting atmosphere, success celebration"
        elif primary_sentiment == "excited":
            base_prompt += ", dynamic energy, vibrant colors, enthusiasm"
        elif primary_sentiment == "proud":
            base_prompt += ", achievement symbols, professional success, accomplishment"
        elif primary_sentiment == "grateful":
            base_prompt += ", warm atmosphere, community support, thankfulness"
        elif primary_sentiment == "concerned":
            base_prompt += ", supportive atmosphere, problem-solving, growth mindset"
        else:
            base_prompt += ", balanced composition, motivational"
        
        return base_prompt
    
    def _create_variation_prompt(self, base_prompt: str, variation_index: int) -> str:
        """변형 프롬프트 생성"""
        variation_elements = [
            "different color palette",
            "alternative composition",
            "varied lighting style",
            "different perspective",
            "alternative artistic approach"
        ]
        
        element = variation_elements[variation_index % len(variation_elements)]
        return f"{base_prompt}, {element}"
    
    def _clean_prompt(self, prompt: str) -> str:
        """프롬프트 정리 (불적절한 내용 제거)"""
        # 기본적인 필터링
        inappropriate_words = ["nsfw", "nude", "violent", "inappropriate"]
        
        for word in inappropriate_words:
            prompt = re.sub(word, "", prompt, flags=re.IGNORECASE)
        
        return prompt.strip()

# 전역 인스턴스
image_service = ImageService()