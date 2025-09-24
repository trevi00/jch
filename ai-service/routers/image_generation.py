from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any
import logging
from datetime import datetime
import aiohttp
import aiofiles
import os
import uuid
from pathlib import Path

from models.schemas import (
    ImageGenerationRequest, ImageGenerationResponse, APIResponse
)
from services.openai_service import openai_service

logger = logging.getLogger(__name__)
router = APIRouter()

class ImageGenerationService:
    def __init__(self):
        # 이미지 저장 디렉토리 설정
        self.image_dir = Path("static/images")
        self.image_dir.mkdir(parents=True, exist_ok=True)
        
        self.style_prompts = {
            "realistic": "realistic, high quality, detailed",
            "artistic": "artistic, creative, stylized", 
            "cartoon": "cartoon style, cute, colorful",
            "professional": "professional, clean, business style",
            "minimalist": "minimalist, simple, clean design",
            "vintage": "vintage style, retro, classic",
            "modern": "modern, contemporary, sleek"
        }
        
        self.emotion_keywords = {
            "positive": ["joyful", "bright", "cheerful", "optimistic", "energetic"],
            "negative": ["melancholy", "dark", "somber", "reflective", "moody"],
            "neutral": ["calm", "peaceful", "balanced", "serene", "stable"]
        }
    
    async def download_and_save_image(self, image_url: str, user_id: int) -> str:
        """이미지를 다운로드하여 로컬에 저장하고 새 URL 반환"""
        try:
            # 고유한 파일명 생성
            file_id = str(uuid.uuid4())
            file_name = f"{user_id}_{file_id}.png"
            file_path = self.image_dir / file_name
            
            # 이미지 다운로드
            async with aiohttp.ClientSession() as session:
                async with session.get(image_url) as response:
                    if response.status == 200:
                        async with aiofiles.open(file_path, 'wb') as file:
                            async for chunk in response.content.iter_chunked(8192):
                                await file.write(chunk)
                        
                        # 로컬 URL 반환 (AI 서비스 기준)
                        local_url = f"/static/images/{file_name}"
                        logger.info(f"이미지 저장 완료: {file_path} -> {local_url}")
                        return local_url
                    else:
                        logger.error(f"이미지 다운로드 실패: HTTP {response.status}")
                        return image_url  # 원본 URL 반환
                        
        except Exception as e:
            logger.error(f"이미지 저장 실패: {e}")
            return image_url  # 원본 URL 반환
    
    async def analyze_post_sentiment(self, text: str) -> str:
        """게시글 감정 분석"""
        
        system_prompt = """
        당신은 텍스트 감정 분석 전문가입니다.
        주어진 텍스트의 감정을 분석하여 다음 중 하나로 분류해주세요:
        
        - positive: 긍정적, 기쁨, 희망적, 활기찬 감정
        - negative: 부정적, 슬픔, 우울한, 화난 감정  
        - neutral: 중립적, 평온한, 객관적인 감정
        
        감정 분류 결과만 답변해주세요 (positive, negative, neutral 중 하나).
        """
        
        try:
            response = await openai_service.generate_completion([
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": f"다음 텍스트의 감정을 분석해주세요: {text}"}
            ], max_tokens=10)
            
            sentiment = response.strip().lower()
            if sentiment not in ["positive", "negative", "neutral"]:
                sentiment = "neutral"
                
            return sentiment
            
        except Exception as e:
            logger.error(f"감정 분석 실패: {e}")
            return "neutral"
    
    async def enhance_prompt_with_sentiment(
        self, 
        original_prompt: str, 
        sentiment: str,
        style: str = "realistic"
    ) -> str:
        """감정 분석 결과를 반영한 프롬프트 개선"""
        
        # 스타일 키워드 추가
        style_keywords = self.style_prompts.get(style, "realistic, high quality")
        
        # 감정 키워드 추가
        emotion_keywords = ", ".join(self.emotion_keywords.get(sentiment, ["balanced"]))
        
        enhanced_prompt = f"{original_prompt}, {style_keywords}, {emotion_keywords}, high quality, detailed"
        
        return enhanced_prompt
    
    async def generate_image_with_sentiment(
        self, 
        prompt: str,
        user_id: int,
        post_text: str = "",
        style: str = "realistic",
        size: str = "1024x1024"
    ) -> Dict[str, Any]:
        """감정 분석 기반 이미지 생성"""
        
        try:
            # 게시글이 있으면 감정 분석
            sentiment = "neutral"
            if post_text:
                sentiment = await self.analyze_post_sentiment(post_text)
            
            # 프롬프트 개선
            enhanced_prompt = await self.enhance_prompt_with_sentiment(
                original_prompt=prompt,
                sentiment=sentiment,
                style=style
            )
            
            # 이미지 생성
            image_url = await openai_service.generate_image(
                prompt=enhanced_prompt,
                size=size
            )
            
            # 이미지를 로컬에 저장
            local_image_url = await self.download_and_save_image(image_url, user_id)
            
            return {
                "image_url": local_image_url,  # 로컬 저장된 이미지 URL
                "original_dall_e_url": image_url,  # 원본 DALL-E URL (백업용)
                "sentiment": sentiment,
                "enhanced_prompt": enhanced_prompt,
                "original_prompt": prompt
            }
            
        except Exception as e:
            logger.error(f"감정 기반 이미지 생성 실패: {e}")
            raise HTTPException(status_code=500, detail="이미지 생성에 실패했습니다")
    
    async def generate_simple_image(
        self,
        prompt: str,
        user_id: int,
        style: str = "realistic", 
        size: str = "1024x1024"
    ) -> Dict[str, str]:
        """단순 이미지 생성"""
        
        try:
            # 스타일 적용
            style_keywords = self.style_prompts.get(style, "realistic, high quality")
            enhanced_prompt = f"{prompt}, {style_keywords}"
            
            image_url = await openai_service.generate_image(
                prompt=enhanced_prompt,
                size=size
            )
            
            # 이미지를 로컬에 저장
            local_image_url = await self.download_and_save_image(image_url, user_id)
            
            return {
                "image_url": local_image_url,
                "original_dall_e_url": image_url
            }
            
        except Exception as e:
            logger.error(f"이미지 생성 실패: {e}")
            raise HTTPException(status_code=500, detail="이미지 생성에 실패했습니다")
    
    async def generate_multiple_variations(
        self,
        prompt: str,
        count: int = 3,
        style: str = "realistic",
        size: str = "1024x1024"
    ) -> List[str]:
        """같은 프롬프트로 여러 변형 이미지 생성"""
        
        if count > 5:  # 비용 절약을 위한 제한
            count = 5
        
        try:
            image_urls = []
            style_keywords = self.style_prompts.get(style, "realistic, high quality")
            
            for i in range(count):
                # 각 변형마다 약간 다른 키워드 추가
                variation_keywords = [
                    "variation 1, unique perspective",
                    "variation 2, different angle", 
                    "variation 3, alternative composition",
                    "variation 4, creative interpretation",
                    "variation 5, artistic freedom"
                ]
                
                enhanced_prompt = f"{prompt}, {style_keywords}, {variation_keywords[i % 5]}"
                
                image_url = await openai_service.generate_image(
                    prompt=enhanced_prompt,
                    size=size
                )
                
                image_urls.append(image_url)
            
            return image_urls
            
        except Exception as e:
            logger.error(f"다중 이미지 생성 실패: {e}")
            raise HTTPException(status_code=500, detail="다중 이미지 생성에 실패했습니다")

image_generation_service = ImageGenerationService()

@router.get("/health", response_model=APIResponse)
async def get_image_generation_health():
    """이미지 생성 서비스 상태 확인"""
    try:
        return APIResponse(
            success=True,
            message="이미지 생성 서비스가 정상 작동 중입니다",
            data={"status": "healthy", "service": "image_generation", "dall_e_available": True}
        )
    except Exception as e:
        logger.error(f"이미지 생성 서비스 상태 확인 오류: {e}")
        return APIResponse(
            success=False,
            message="이미지 생성 서비스 상태 확인에 실패했습니다",
            error=str(e)
        )

@router.post("/generate", response_model=APIResponse)
async def generate_image(request: ImageGenerationRequest):
    """기본 이미지 생성"""
    try:
        image_result = await image_generation_service.generate_simple_image(
            prompt=request.prompt,
            user_id=request.user_id,
            style=request.style,
            size=request.size
        )
        
        result = ImageGenerationResponse(
            user_id=request.user_id,
            prompt=request.prompt,
            image_url=image_result["image_url"],  # 로컬 저장된 URL
            created_at=datetime.now()
        )
        
        return APIResponse(
            success=True,
            message="이미지가 성공적으로 생성되었습니다",
            data={"image": result}
        )
        
    except Exception as e:
        logger.error(f"이미지 생성 API 오류: {e}")
        return APIResponse(
            success=False,
            message="이미지 생성에 실패했습니다",
            error=str(e)
        )

@router.post("/generate-with-sentiment", response_model=APIResponse) 
async def generate_image_with_sentiment(
    prompt: str,
    user_id: int,
    post_text: str,
    style: str = "realistic",
    size: str = "1024x1024"
):
    """감정 분석 기반 이미지 생성 (커뮤니티 게시글용)"""
    try:
        result = await image_generation_service.generate_image_with_sentiment(
            prompt=prompt,
            user_id=user_id,
            post_text=post_text,
            style=style,
            size=size
        )
        
        image_response = ImageGenerationResponse(
            user_id=user_id,
            prompt=result["original_prompt"],
            image_url=result["image_url"],
            created_at=datetime.now()
        )
        
        return APIResponse(
            success=True,
            message="감정 분석 기반 이미지가 성공적으로 생성되었습니다",
            data={
                "image": image_response,
                "sentiment": result["sentiment"],
                "enhanced_prompt": result["enhanced_prompt"]
            }
        )
        
    except Exception as e:
        logger.error(f"감정 기반 이미지 생성 API 오류: {e}")
        return APIResponse(
            success=False,
            message="감정 기반 이미지 생성에 실패했습니다",
            error=str(e)
        )

@router.post("/generate-variations", response_model=APIResponse)
async def generate_image_variations(
    prompt: str,
    user_id: int,
    count: int = 3,
    style: str = "realistic",
    size: str = "1024x1024"
):
    """다중 변형 이미지 생성"""
    try:
        image_urls = await image_generation_service.generate_multiple_variations(
            prompt=prompt,
            count=count,
            style=style,
            size=size
        )
        
        results = []
        for i, url in enumerate(image_urls):
            result = ImageGenerationResponse(
                user_id=user_id,
                prompt=f"{prompt} (변형 {i+1})",
                image_url=url,
                created_at=datetime.now()
            )
            results.append(result)
        
        return APIResponse(
            success=True,
            message=f"{len(results)}개의 변형 이미지가 성공적으로 생성되었습니다",
            data={"images": results}
        )
        
    except Exception as e:
        logger.error(f"변형 이미지 생성 API 오류: {e}")
        return APIResponse(
            success=False,
            message="변형 이미지 생성에 실패했습니다",
            error=str(e)
        )

@router.post("/analyze-sentiment", response_model=APIResponse)
async def analyze_text_sentiment(text: str):
    """텍스트 감정 분석 (독립적 사용)"""
    try:
        sentiment = await image_generation_service.analyze_post_sentiment(text)
        
        # 감정별 이모티콘 매핑
        emotion_icons = {
            "positive": "😊",
            "negative": "😢", 
            "neutral": "😐"
        }
        
        return APIResponse(
            success=True,
            message="감정 분석이 완료되었습니다",
            data={
                "text": text,
                "sentiment": sentiment,
                "emotion_icon": emotion_icons.get(sentiment, "😐"),
                "keywords": image_generation_service.emotion_keywords.get(sentiment, [])
            }
        )
        
    except Exception as e:
        logger.error(f"감정 분석 API 오류: {e}")
        return APIResponse(
            success=False,
            message="감정 분석에 실패했습니다",
            error=str(e)
        )

@router.get("/styles", response_model=APIResponse)
async def get_available_styles():
    """사용 가능한 이미지 스타일 목록 조회"""
    try:
        styles = []
        for style, description in image_generation_service.style_prompts.items():
            styles.append({
                "style": style,
                "description": description,
                "display_name": style.title()
            })
        
        return APIResponse(
            success=True,
            message="사용 가능한 스타일 목록입니다",
            data={"styles": styles}
        )
        
    except Exception as e:
        logger.error(f"스타일 목록 조회 API 오류: {e}")
        return APIResponse(
            success=False,
            message="스타일 목록 조회에 실패했습니다",
            error=str(e)
        )