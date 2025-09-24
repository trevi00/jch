#!/usr/bin/env python3
"""
잡았다 AI Service 통합 테스트
"""

import requests
import json
import sys
from typing import Dict, Any

class AIServiceTester:
    def __init__(self, base_url: str = "http://localhost:8001"):
        self.base_url = base_url
        self.session = requests.Session()
    
    def test_health_check(self) -> bool:
        """헬스 체크 테스트"""
        try:
            response = self.session.get(f"{self.base_url}/health")
            return response.status_code == 200
        except Exception as e:
            print(f"헬스 체크 실패: {e}")
            return False
    
    def test_interview_questions(self) -> Dict[str, Any]:
        """면접 질문 생성 테스트"""
        url = f"{self.base_url}/api/v1/interview/generate-questions"
        
        payload = {
            "interview_type": "technical",
            "user_id": 1,
            "job_position": "백엔드 개발자",
            "experience_level": "intermediate",
            "skills": ["Java", "Spring Boot", "MySQL"]
        }
        
        try:
            response = self.session.post(url, json=payload)
            return {
                "success": response.status_code == 200,
                "data": response.json() if response.status_code == 200 else None,
                "error": response.text if response.status_code != 200 else None
            }
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def test_cover_letter_questions(self) -> Dict[str, Any]:
        """자소서 질문 생성 테스트"""
        url = f"{self.base_url}/api/v1/cover-letter/generate-questions"
        
        payload = {
            "section": "motivation",
            "company_name": "네이버",
            "position": "백엔드 개발자"
        }
        
        try:
            response = self.session.post(url, json=payload)
            return {
                "success": response.status_code == 200,
                "data": response.json() if response.status_code == 200 else None,
                "error": response.text if response.status_code != 200 else None
            }
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def test_translation(self) -> Dict[str, Any]:
        """번역 테스트"""
        url = f"{self.base_url}/api/v1/translation/translate"
        
        payload = {
            "text": "안녕하세요. 저는 백엔드 개발자입니다.",
            "source_language": "ko",
            "target_language": "en",
            "document_type": "general"
        }
        
        try:
            response = self.session.post(url, json=payload)
            return {
                "success": response.status_code == 200,
                "data": response.json() if response.status_code == 200 else None,
                "error": response.text if response.status_code != 200 else None
            }
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def test_image_generation(self) -> Dict[str, Any]:
        """이미지 생성 테스트"""
        url = f"{self.base_url}/api/v1/image/generate"
        
        payload = {
            "prompt": "a beautiful sunset over mountains",
            "user_id": 1,
            "style": "realistic",
            "size": "1024x1024"
        }
        
        try:
            response = self.session.post(url, json=payload)
            return {
                "success": response.status_code == 200,
                "data": response.json() if response.status_code == 200 else None,
                "error": response.text if response.status_code != 200 else None
            }
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def test_sentiment_analysis(self) -> Dict[str, Any]:
        """감정 분석 테스트"""
        url = f"{self.base_url}/api/v1/image/analyze-sentiment"
        
        payload = {
            "text": "오늘 정말 기분이 좋다! 새로운 프로젝트를 시작하게 되어서 설렌다."
        }
        
        try:
            response = self.session.post(url, json=payload)
            return {
                "success": response.status_code == 200,
                "data": response.json() if response.status_code == 200 else None,
                "error": response.text if response.status_code != 200 else None
            }
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def run_all_tests(self):
        """모든 테스트 실행"""
        print("=== 잡았다 AI Service 통합 테스트 시작 ===")
        print()
        
        # 헬스 체크
        print("1. 헬스 체크 테스트...")
        if self.test_health_check():
            print("   ✅ 성공")
        else:
            print("   ❌ 실패 - AI 서비스가 실행되고 있지 않습니다")
            return False
        
        # 면접 질문 생성 테스트
        print("2. 면접 질문 생성 테스트...")
        result = self.test_interview_questions()
        if result["success"]:
            print("   ✅ 성공")
            if result["data"]:
                questions = result["data"].get("data", {}).get("questions", [])
                print(f"   생성된 질문 수: {len(questions)}")
        else:
            print(f"   ❌ 실패: {result['error']}")
        
        # 자소서 질문 생성 테스트  
        print("3. 자소서 질문 생성 테스트...")
        result = self.test_cover_letter_questions()
        if result["success"]:
            print("   ✅ 성공")
            if result["data"]:
                questions = result["data"].get("data", {}).get("questions", [])
                print(f"   생성된 질문 수: {len(questions)}")
        else:
            print(f"   ❌ 실패: {result['error']}")
        
        # 번역 테스트
        print("4. 번역 테스트...")
        result = self.test_translation()
        if result["success"]:
            print("   ✅ 성공")
            if result["data"]:
                translation = result["data"].get("data", {}).get("translation", {})
                print(f"   번역 결과: {translation.get('translated_text', 'N/A')}")
        else:
            print(f"   ❌ 실패: {result['error']}")
        
        # 감정 분석 테스트
        print("5. 감정 분석 테스트...")
        result = self.test_sentiment_analysis()
        if result["success"]:
            print("   ✅ 성공")
            if result["data"]:
                sentiment = result["data"].get("data", {}).get("sentiment", "N/A")
                print(f"   감정 분석 결과: {sentiment}")
        else:
            print(f"   ❌ 실패: {result['error']}")
        
        # 이미지 생성 테스트 (선택적)
        print("6. 이미지 생성 테스트 (선택적)...")
        print("   ⚠️  이미지 생성은 OpenAI API 비용이 발생하므로 수동으로 테스트하세요")
        
        print()
        print("=== 통합 테스트 완료 ===")
        return True

def main():
    """메인 함수"""
    import argparse
    
    parser = argparse.ArgumentParser(description="AI Service 통합 테스트")
    parser.add_argument("--url", default="http://localhost:8001", 
                       help="AI 서비스 기본 URL (기본값: http://localhost:8001)")
    
    args = parser.parse_args()
    
    tester = AIServiceTester(args.url)
    tester.run_all_tests()

if __name__ == "__main__":
    main()