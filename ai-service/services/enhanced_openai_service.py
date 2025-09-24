from openai import OpenAI
from typing import List, Dict, Any, Optional
from config.settings import settings
import logging
import asyncio
from functools import lru_cache
import json
from datetime import datetime, timedelta
import hashlib

logger = logging.getLogger(__name__)

class EnhancedOpenAIService:
    """전문적인 수준의 OpenAI 서비스 클래스"""
    
    def __init__(self):
        self.client = OpenAI(api_key=settings.OPENAI_API_KEY)
        self.cache = {}
        self.cache_ttl = timedelta(hours=1)
        
    def _get_cache_key(self, messages: List[Dict], model: str, **kwargs) -> str:
        """캐시 키 생성"""
        content = json.dumps({
            "messages": messages,
            "model": model,
            **kwargs
        }, sort_keys=True)
        return hashlib.md5(content.encode()).hexdigest()
    
    def _is_cache_valid(self, cache_entry: Dict) -> bool:
        """캐시 유효성 검사"""
        if not cache_entry:
            return False
        cached_time = cache_entry.get("timestamp")
        if not cached_time:
            return False
        return datetime.now() - cached_time < self.cache_ttl
    
    async def generate_completion_with_retry(
        self, 
        messages: List[Dict[str, str]], 
        model: str = None,
        temperature: float = None,
        max_tokens: int = None,
        use_cache: bool = True,
        retry_count: int = 3,
        system_prompt_enhancement: bool = True
    ) -> Dict[str, Any]:
        """재시도 로직과 캐싱이 포함된 고급 completion 생성"""
        
        # 모델 선택 로직
        if model is None:
            # 메시지 길이에 따라 자동 모델 선택
            total_length = sum(len(m.get("content", "")) for m in messages)
            model = settings.OPENAI_MODEL_ADVANCED if total_length > 1000 else settings.OPENAI_MODEL_FAST
        
        # 캐시 확인
        if use_cache:
            cache_key = self._get_cache_key(messages, model, temperature=temperature, max_tokens=max_tokens)
            cached = self.cache.get(cache_key)
            if cached and self._is_cache_valid(cached):
                logger.info("캐시에서 응답 반환")
                return cached["response"]
        
        # 시스템 프롬프트 향상
        if system_prompt_enhancement and messages and messages[0].get("role") == "system":
            messages[0]["content"] = self._enhance_system_prompt(messages[0]["content"])
        
        # 재시도 로직
        last_error = None
        for attempt in range(retry_count):
            try:
                response = await self._call_openai_api(
                    messages=messages,
                    model=model,
                    temperature=temperature or settings.OPENAI_TEMPERATURE,
                    max_tokens=max_tokens or settings.OPENAI_MAX_TOKENS
                )
                
                # 응답 품질 검증
                if not self._validate_response_quality(response):
                    logger.warning(f"응답 품질 검증 실패 (시도 {attempt + 1}/{retry_count})")
                    if attempt < retry_count - 1:
                        await asyncio.sleep(2 ** attempt)  # Exponential backoff
                        continue
                
                # 성공적인 응답 캐싱
                result = {
                    "content": response,
                    "model": model,
                    "timestamp": datetime.now().isoformat(),
                    "quality_score": self._calculate_quality_score(response)
                }
                
                if use_cache:
                    self.cache[cache_key] = {
                        "response": result,
                        "timestamp": datetime.now()
                    }
                
                return result
                
            except Exception as e:
                last_error = e
                logger.error(f"OpenAI API 호출 실패 (시도 {attempt + 1}/{retry_count}): {e}")
                if attempt < retry_count - 1:
                    await asyncio.sleep(2 ** attempt)
                    
        raise Exception(f"모든 재시도 실패: {last_error}")
    
    async def _call_openai_api(
        self,
        messages: List[Dict[str, str]],
        model: str,
        temperature: float,
        max_tokens: int
    ) -> str:
        """OpenAI API 호출"""
        response = self.client.chat.completions.create(
            model=model,
            messages=messages,
            temperature=temperature,
            max_tokens=max_tokens,
            presence_penalty=0.1,  # 반복 감소
            frequency_penalty=0.1,  # 다양성 증가
            top_p=0.95  # 품질 향상
        )
        return response.choices[0].message.content
    
    def _enhance_system_prompt(self, original_prompt: str) -> str:
        """시스템 프롬프트 향상"""
        enhancement = """
        다음 지침을 따라주세요:
        1. 전문적이고 정확한 답변을 제공하세요
        2. 구조화된 형식으로 응답하세요
        3. 실무에 적용 가능한 구체적인 내용을 포함하세요
        4. 최신 업계 표준과 베스트 프랙티스를 반영하세요
        5. 필요시 예시와 코드 샘플을 포함하세요
        
        원래 지침:
        """
        return enhancement + original_prompt
    
    def _validate_response_quality(self, response: str) -> bool:
        """응답 품질 검증"""
        if not response or len(response.strip()) < 10:
            return False
        
        # 의미 없는 반복 체크
        words = response.split()
        if len(words) > 10:
            unique_words = set(words)
            if len(unique_words) / len(words) < 0.3:  # 30% 미만이 고유 단어
                return False
        
        return True
    
    def _calculate_quality_score(self, response: str) -> float:
        """응답 품질 점수 계산"""
        score = 0.0
        
        # 길이 점수
        length = len(response)
        if length > 100:
            score += 0.2
        if length > 500:
            score += 0.2
            
        # 구조화 점수 (번호, 불릿 포인트 등)
        if any(marker in response for marker in ['1.', '2.', '•', '-', '*']):
            score += 0.2
            
        # 전문 용어 포함 여부
        professional_terms = ['구현', '설계', '아키텍처', '최적화', '성능', '보안', '테스트']
        if any(term in response for term in professional_terms):
            score += 0.2
            
        # 코드 블록 포함 여부
        if '```' in response or 'def ' in response or 'function ' in response:
            score += 0.2
            
        return min(score, 1.0)
    
    async def generate_image_advanced(
        self, 
        prompt: str, 
        size: str = None,
        quality: str = "hd",  # 고화질로 기본 설정
        style: str = "vivid",  # 생동감 있는 스타일
        model: str = None
    ) -> Dict[str, Any]:
        """고급 이미지 생성"""
        
        # 프롬프트 향상
        enhanced_prompt = self._enhance_image_prompt(prompt)
        
        try:
            response = self.client.images.generate(
                model=model or settings.IMAGE_MODEL,
                prompt=enhanced_prompt,
                size=size or settings.IMAGE_SIZE,
                quality=quality,
                style=style,
                n=1,
            )
            
            return {
                "url": response.data[0].url,
                "prompt": enhanced_prompt,
                "original_prompt": prompt,
                "metadata": {
                    "model": model or settings.IMAGE_MODEL,
                    "size": size or settings.IMAGE_SIZE,
                    "quality": quality,
                    "style": style,
                    "timestamp": datetime.now().isoformat()
                }
            }
        except Exception as e:
            logger.error(f"이미지 생성 실패: {e}")
            raise
    
    def _enhance_image_prompt(self, original_prompt: str) -> str:
        """이미지 프롬프트 향상"""
        enhancements = [
            "professional quality",
            "highly detailed",
            "sharp focus",
            "studio lighting",
            "trending on artstation"
        ]
        
        # 원본 프롬프트가 짧으면 향상 추가
        if len(original_prompt) < 50:
            return f"{original_prompt}, {', '.join(enhancements)}"
        return original_prompt
    
    async def create_embedding_batch(
        self,
        texts: List[str],
        model: str = "text-embedding-3-large"  # 더 나은 임베딩 모델
    ) -> List[List[float]]:
        """배치 임베딩 생성"""
        try:
            # 빈 텍스트 필터링
            valid_texts = [t for t in texts if t and t.strip()]
            if not valid_texts:
                return []
            
            # 배치 처리 (최대 100개씩)
            batch_size = 100
            all_embeddings = []
            
            for i in range(0, len(valid_texts), batch_size):
                batch = valid_texts[i:i + batch_size]
                response = self.client.embeddings.create(
                    model=model,
                    input=batch,
                    encoding_format="float"
                )
                
                batch_embeddings = [data.embedding for data in response.data]
                all_embeddings.extend(batch_embeddings)
            
            return all_embeddings
            
        except Exception as e:
            logger.error(f"임베딩 생성 실패: {e}")
            raise
    
    def clear_cache(self):
        """캐시 초기화"""
        self.cache.clear()
        logger.info("캐시가 초기화되었습니다")
    
    def get_cache_stats(self) -> Dict[str, Any]:
        """캐시 통계 반환"""
        valid_entries = sum(1 for entry in self.cache.values() if self._is_cache_valid(entry))
        return {
            "total_entries": len(self.cache),
            "valid_entries": valid_entries,
            "expired_entries": len(self.cache) - valid_entries,
            "cache_ttl_hours": self.cache_ttl.total_seconds() / 3600
        }

# 싱글톤 인스턴스
enhanced_openai_service = EnhancedOpenAIService()