#!/usr/bin/env python3
"""
FastAPI 전체 엔드포인트 테스트 스크립트
"""

import requests
import json
import time
from datetime import datetime

BASE_URL = "http://localhost:8001"

def print_header(title):
    """섹션 헤더 출력"""
    print("\n" + "="*60)
    print(f"  {title}")
    print("="*60)

def test_endpoint(method, path, data=None, description=""):
    """엔드포인트 테스트 공통 함수"""
    url = f"{BASE_URL}{path}"
    print(f"\n[{method}] {path}")
    if description:
        print(f"  Description: {description}")
    
    try:
        if method == "GET":
            response = requests.get(url, timeout=30)
        elif method == "POST":
            response = requests.post(url, json=data, timeout=30)
        elif method == "DELETE":
            response = requests.delete(url, timeout=30)
        else:
            print(f"  Unsupported method: {method}")
            return None
            
        print(f"  Status: {response.status_code}")
        
        if response.status_code == 200:
            try:
                result = response.json()
                if isinstance(result, dict):
                    # 간단한 응답 출력
                    if 'success' in result:
                        print(f"  Success: {result['success']}")
                    if 'message' in result:
                        print(f"  Message: {result.get('message', '')[:100]}")
                    if 'data' in result and isinstance(result['data'], dict):
                        data_keys = list(result['data'].keys())[:3]
                        print(f"  Data keys: {', '.join(data_keys)}")
                else:
                    print(f"  Response type: {type(result).__name__}")
                return result
            except:
                print(f"  Response: {response.text[:200]}")
                return response.text
        else:
            print(f"  Error: {response.text[:200]}")
            return None
            
    except Exception as e:
        print(f"  ERROR: {e}")
        return None

def test_all_endpoints():
    """모든 엔드포인트 테스트"""
    
    # 1. 기본 엔드포인트
    print_header("1. BASIC ENDPOINTS")
    test_endpoint("GET", "/", description="Root endpoint")
    test_endpoint("GET", "/health", description="Health check")
    
    # 2. 챗봇 API
    print_header("2. CHATBOT API")
    
    # 채팅
    test_endpoint("POST", "/api/v1/chatbot/chat", 
                 data={"user_id": "test_user", "message": "안녕하세요"},
                 description="Chat with AI bot")
    
    # 추천 질문
    test_endpoint("GET", "/api/v1/chatbot/suggestions",
                 description="Get suggested questions")
    
    # 카테고리
    test_endpoint("GET", "/api/v1/chatbot/categories",
                 description="Get FAQ categories")
    
    # 히스토리
    test_endpoint("GET", "/api/v1/chatbot/history/test_user",
                 description="Get chat history")
    
    # 상태
    test_endpoint("GET", "/api/v1/chatbot/status",
                 description="Get chatbot service status")
    
    # 히스토리 삭제
    test_endpoint("DELETE", "/api/v1/chatbot/history/test_user",
                 description="Clear chat history")
    
    # 3. AI 면접 API
    print_header("3. AI INTERVIEW API")
    
    # 기술면접
    test_endpoint("POST", "/api/v1/interview/technical",
                 data={"position": "백엔드 개발자", "tech_stack": ["Python", "FastAPI", "PostgreSQL"]},
                 description="Generate technical interview questions")
    
    # 인성면접
    test_endpoint("POST", "/api/v1/interview/behavioral",
                 data={"position": "백엔드 개발자", "company_type": "스타트업"},
                 description="Generate behavioral interview questions")
    
    # 면접 피드백
    test_endpoint("POST", "/api/v1/interview/feedback",
                 data={
                     "question": "REST API와 GraphQL의 차이점을 설명해주세요",
                     "answer": "REST API는 리소스 기반으로 설계되고 GraphQL은 쿼리 언어입니다"
                 },
                 description="Get interview answer feedback")
    
    # 모의면접
    test_endpoint("POST", "/api/v1/interview/mock/start",
                 data={"interview_type": "technical", "position": "프론트엔드 개발자"},
                 description="Start mock interview session")
    
    # 4. 자소서 생성 API
    print_header("4. COVER LETTER API")
    
    # 자소서 생성
    test_endpoint("POST", "/api/v1/cover-letter/generate",
                 data={
                     "company": "네이버",
                     "position": "백엔드 개발자",
                     "user_experience": "대학에서 컴퓨터공학을 전공했고 인턴 경험이 있습니다"
                 },
                 description="Generate cover letter")
    
    # 자소서 개선
    test_endpoint("POST", "/api/v1/cover-letter/improve",
                 data={
                     "content": "저는 열정적인 개발자입니다. 새로운 기술을 배우는 것을 좋아합니다.",
                     "company": "카카오",
                     "position": "데이터 엔지니어"
                 },
                 description="Improve existing cover letter")
    
    # 키워드 추출
    test_endpoint("POST", "/api/v1/cover-letter/keywords",
                 data={"job_description": "Python, Django, REST API, Docker, Kubernetes 경험 필수"},
                 description="Extract keywords from job description")
    
    # 5. 번역 API
    print_header("5. TRANSLATION API")
    
    # 텍스트 번역
    test_endpoint("POST", "/api/v1/translation/translate",
                 data={
                     "text": "안녕하세요. 저는 AI 개발자입니다.",
                     "target_language": "en"
                 },
                 description="Translate text")
    
    # 문서 유형별 번역
    test_endpoint("POST", "/api/v1/translation/document",
                 data={
                     "text": "저는 5년 경력의 풀스택 개발자입니다.",
                     "document_type": "resume",
                     "target_language": "en"
                 },
                 description="Translate document with context")
    
    # 지원 언어 목록
    test_endpoint("GET", "/api/v1/translation/languages",
                 description="Get supported languages")
    
    # 6. 이미지 생성 API
    print_header("6. IMAGE GENERATION API")
    
    # 프로필 이미지 생성
    test_endpoint("POST", "/api/v1/image/profile",
                 data={"description": "전문적이고 친근한 개발자"},
                 description="Generate profile image")
    
    # 커뮤니티 이미지 생성
    test_endpoint("POST", "/api/v1/image/community",
                 data={
                     "post_content": "오늘 드디어 취업에 성공했습니다! 모두 감사합니다.",
                     "style": "celebration"
                 },
                 description="Generate community post image")
    
    # 스타일 목록
    test_endpoint("GET", "/api/v1/image/styles",
                 description="Get available image styles")

def main():
    """메인 함수"""
    print("\n" + "="*60)
    print("   JOBATTA AI SERVICE - FULL API TEST")
    print("="*60)
    
    start_time = time.time()
    
    # 서버 연결 확인
    print("\nChecking server connection...")
    response = test_endpoint("GET", "/health")
    
    if response:
        print("\n[OK] Server is running! Starting full test suite...")
        test_all_endpoints()
        
        elapsed_time = time.time() - start_time
        print("\n" + "="*60)
        print(f"  TEST COMPLETED in {elapsed_time:.2f} seconds")
        print("="*60)
    else:
        print("\n[ERROR] Server is not responding. Please start the server first.")

if __name__ == "__main__":
    main()