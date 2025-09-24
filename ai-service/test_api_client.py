#!/usr/bin/env python3
"""
API 클라이언트 테스트 스크립트
"""

import requests
import json

def test_api_endpoints():
    """API 엔드포인트 테스트"""
    base_url = "http://localhost:8001"
    
    print("🧪 FastAPI 서버 테스트 시작")
    print("=" * 50)
    
    # 테스트 케이스들
    test_cases = [
        {
            "name": "루트 엔드포인트",
            "url": f"{base_url}/",
            "method": "GET"
        },
        {
            "name": "헬스체크",
            "url": f"{base_url}/health",
            "method": "GET"
        },
        {
            "name": "테스트 API",
            "url": f"{base_url}/api/v1/test",
            "method": "GET"
        }
    ]
    
    for i, test_case in enumerate(test_cases, 1):
        print(f"\n{i}. {test_case['name']} 테스트")
        print(f"   URL: {test_case['url']}")
        
        try:
            response = requests.get(test_case['url'], timeout=5)
            
            if response.status_code == 200:
                print(f"   ✅ 성공 (200)")
                try:
                    data = response.json()
                    print(f"   📄 응답: {json.dumps(data, indent=2, ensure_ascii=False)}")
                except:
                    print(f"   📄 응답: {response.text}")
            else:
                print(f"   ❌ 실패 ({response.status_code})")
                print(f"   📄 응답: {response.text}")
                
        except requests.exceptions.ConnectionError:
            print(f"   💥 연결 실패: 서버가 실행되지 않았거나 포트가 다릅니다")
        except requests.exceptions.Timeout:
            print(f"   ⏰ 타임아웃: 서버 응답이 너무 느립니다")
        except Exception as e:
            print(f"   🚨 오류: {e}")
    
    print("\n" + "=" * 50)
    print("🎯 테스트 완료!")

if __name__ == "__main__":
    test_api_endpoints()