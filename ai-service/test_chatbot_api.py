#!/usr/bin/env python3
"""
챗봇 API 전체 기능 테스트 스크립트
"""

import requests
import json
from datetime import datetime

def test_chatbot_api():
    """챗봇 API 전체 기능 테스트"""
    base_url = "http://localhost:8001"
    test_user_id = "test_user_123"
    
    print("🤖 챗봇 API 전체 기능 테스트 시작")
    print("=" * 60)
    
    # 테스트 케이스들
    test_cases = [
        # 1. 기본 서버 상태 확인
        {
            "name": "서버 상태 확인",
            "method": "GET",
            "url": f"{base_url}/health",
            "expected_status": 200
        },
        
        # 2. 챗봇 대화 테스트
        {
            "name": "챗봇 대화 - 회원가입 문의",
            "method": "POST", 
            "url": f"{base_url}/api/v1/chatbot/chat",
            "json": {
                "user_id": test_user_id,
                "message": "회원가입은 어떻게 하나요?"
            },
            "expected_status": 200
        },
        
        # 3. 챗봇 대화 테스트 2
        {
            "name": "챗봇 대화 - AI 면접 문의",
            "method": "POST",
            "url": f"{base_url}/api/v1/chatbot/chat", 
            "json": {
                "user_id": test_user_id,
                "message": "AI 면접 기능을 사용하고 싶어요"
            },
            "expected_status": 200
        },
        
        # 4. 빈 메시지 테스트
        {
            "name": "챗봇 대화 - 빈 메시지",
            "method": "POST",
            "url": f"{base_url}/api/v1/chatbot/chat",
            "json": {
                "user_id": test_user_id,
                "message": ""
            },
            "expected_status": 200,
            "expected_success": False
        },
        
        # 5. 추천 질문 조회
        {
            "name": "추천 질문 조회",
            "method": "GET",
            "url": f"{base_url}/api/v1/chatbot/suggestions",
            "expected_status": 200
        },
        
        # 6. 카테고리 조회
        {
            "name": "카테고리 조회",
            "method": "GET", 
            "url": f"{base_url}/api/v1/chatbot/categories",
            "expected_status": 200
        },
        
        # 7. 채팅 히스토리 조회
        {
            "name": "채팅 히스토리 조회",
            "method": "GET",
            "url": f"{base_url}/api/v1/chatbot/history/{test_user_id}",
            "expected_status": 200
        },
        
        # 8. 챗봇 상태 확인
        {
            "name": "챗봇 상태 확인",
            "method": "GET",
            "url": f"{base_url}/api/v1/chatbot/status",
            "expected_status": 200
        },
        
        # 9. 채팅 히스토리 삭제
        {
            "name": "채팅 히스토리 삭제",
            "method": "DELETE",
            "url": f"{base_url}/api/v1/chatbot/history/{test_user_id}",
            "expected_status": 200
        }
    ]
    
    passed_tests = 0
    total_tests = len(test_cases)
    
    for i, test_case in enumerate(test_cases, 1):
        print(f"\n{i}. {test_case['name']}")
        print(f"   {test_case['method']} {test_case['url']}")
        
        try:
            # HTTP 요청 실행
            if test_case['method'] == 'GET':
                response = requests.get(test_case['url'], timeout=10)
            elif test_case['method'] == 'POST':
                response = requests.post(test_case['url'], json=test_case.get('json'), timeout=10)
            elif test_case['method'] == 'DELETE':
                response = requests.delete(test_case['url'], timeout=10)
            
            # 상태 코드 확인
            if response.status_code == test_case['expected_status']:
                print(f"   ✅ 상태 코드 통과 ({response.status_code})")
                
                # JSON 응답 파싱
                try:
                    data = response.json()
                    
                    # success 필드 확인 (있는 경우)
                    if 'expected_success' in test_case:
                        if data.get('success') == test_case['expected_success']:
                            print(f"   ✅ Success 필드 통과 ({data.get('success')})")
                        else:
                            print(f"   ❌ Success 필드 실패 (기대: {test_case['expected_success']}, 실제: {data.get('success')})")
                            continue
                    elif data.get('success') is True:
                        print(f"   ✅ 응답 성공")
                    
                    # 응답 데이터 출력 (간략히)
                    if 'data' in data and data['data']:
                        if 'response' in data['data']:
                            print(f"   💬 챗봇 응답: {data['data']['response'][:100]}...")
                        elif 'suggestions' in data['data']:
                            print(f"   📝 추천 질문 수: {len(data['data']['suggestions'])}개")
                        elif 'categories' in data['data']:
                            print(f"   📂 카테고리 수: {len(data['data']['categories'])}개")
                        elif 'history' in data['data']:
                            print(f"   📚 히스토리 항목: {data['data']['total_messages']}개")
                        elif 'chatbot_service' in data['data']:
                            print(f"   🔧 챗봇 상태: {data['data']['chatbot_service']}")
                    
                    passed_tests += 1
                    
                except json.JSONDecodeError:
                    print(f"   ⚠️  JSON 파싱 실패")
                    print(f"   📄 원본 응답: {response.text[:200]}...")
                    
            else:
                print(f"   ❌ 상태 코드 실패 (기대: {test_case['expected_status']}, 실제: {response.status_code})")
                print(f"   📄 응답: {response.text}")
                
        except requests.exceptions.ConnectionError:
            print(f"   💥 연결 실패: 서버가 실행되지 않았습니다")
        except requests.exceptions.Timeout:
            print(f"   ⏰ 타임아웃: 서버 응답이 너무 느립니다")  
        except Exception as e:
            print(f"   🚨 오류: {e}")
    
    # 최종 결과
    print("\n" + "=" * 60)
    print("🎯 테스트 결과 요약")
    print(f"   통과: {passed_tests}/{total_tests}")
    print(f"   성공률: {(passed_tests/total_tests)*100:.1f}%")
    
    if passed_tests == total_tests:
        print("   🎉 모든 테스트 통과!")
        return True
    else:
        print("   ⚠️  일부 테스트 실패")
        return False

def test_conversation_flow():
    """대화 흐름 테스트"""
    print("\n" + "="*60)
    print("🗣️  실제 대화 흐름 테스트")
    print("="*60)
    
    base_url = "http://localhost:8001"
    user_id = "conversation_test_user"
    
    # 대화 시나리오
    conversation = [
        "안녕하세요",
        "회원가입은 어떻게 하나요?", 
        "AI 면접이 뭔가요?",
        "자소서 도움 받을 수 있나요?",
        "감사합니다"
    ]
    
    for i, message in enumerate(conversation, 1):
        print(f"\n{i}. 사용자: {message}")
        
        try:
            response = requests.post(
                f"{base_url}/api/v1/chatbot/chat",
                json={"user_id": user_id, "message": message},
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('success') and 'data' in data:
                    bot_response = data['data']['response']
                    print(f"   🤖 챗봇: {bot_response}")
                else:
                    print(f"   ❌ 응답 실패: {data.get('message', '알 수 없는 오류')}")
            else:
                print(f"   ❌ HTTP 오류: {response.status_code}")
                
        except Exception as e:
            print(f"   🚨 오류: {e}")
    
    print("\n🎭 대화 흐름 테스트 완료!")

if __name__ == "__main__":
    # 기본 API 테스트
    api_success = test_chatbot_api()
    
    # 대화 흐름 테스트
    test_conversation_flow()
    
    print(f"\n🏁 전체 테스트 완료!")
    if api_success:
        print("✅ 챗봇 시스템이 정상적으로 작동합니다!")
    else:
        print("⚠️  일부 기능에 문제가 있습니다.")