#!/usr/bin/env python3
"""
챗봇 시스템 테스트 실행기
FastAPI와 Spring Boot 챗봇 시스템의 통합 테스트를 실행합니다.
"""

import os
import sys
import subprocess
import time
import requests
from pathlib import Path

class TestRunner:
    def __init__(self):
        self.ai_service_url = "http://localhost:8001"
        self.spring_boot_url = "http://localhost:8080"
        self.test_results = {}
    
    def check_environment(self):
        """테스트 환경 체크"""
        print("=== 테스트 환경 체크 ===")
        
        # Python 버전 체크
        python_version = sys.version
        print(f"Python 버전: {python_version}")
        
        # 필요한 패키지 체크
        required_packages = ['pytest', 'fastapi', 'llama_index']
        missing_packages = []
        
        for package in required_packages:
            try:
                __import__(package)
                print(f"✅ {package} 설치됨")
            except ImportError:
                print(f"❌ {package} 설치되지 않음")
                missing_packages.append(package)
        
        if missing_packages:
            print(f"\n누락된 패키지: {missing_packages}")
            print("pip install -r requirements.txt 를 실행하세요.")
            return False
        
        return True
    
    def run_unit_tests(self):
        """단위 테스트 실행"""
        print("\n=== FastAPI 단위 테스트 실행 ===")
        
        try:
            # pytest 실행
            result = subprocess.run([
                sys.executable, "-m", "pytest", 
                "tests/", 
                "-v",
                "--tb=short",
                "--junit-xml=test-results.xml"
            ], capture_output=True, text=True, cwd=".")
            
            print("STDOUT:")
            print(result.stdout)
            
            if result.stderr:
                print("STDERR:")
                print(result.stderr)
            
            self.test_results['unit_tests'] = {
                'return_code': result.returncode,
                'stdout': result.stdout,
                'stderr': result.stderr
            }
            
            if result.returncode == 0:
                print("✅ 단위 테스트 통과")
                return True
            else:
                print("❌ 단위 테스트 실패")
                return False
                
        except Exception as e:
            print(f"❌ 테스트 실행 중 오류: {e}")
            return False
    
    def test_api_manually(self):
        """수동 API 테스트"""
        print("\n=== 수동 API 테스트 ===")
        
        # FastAPI 서버가 실행 중인지 확인
        try:
            response = requests.get(f"{self.ai_service_url}/health", timeout=5)
            print(f"✅ FastAPI 서버 연결됨: {response.status_code}")
        except requests.exceptions.RequestException as e:
            print(f"❌ FastAPI 서버 연결 실패: {e}")
            print("FastAPI 서버를 먼저 실행해주세요: python main.py")
            return False
        
        # 챗봇 API 테스트
        test_cases = [
            {
                "name": "기본 대화 테스트",
                "endpoint": "/api/v1/chatbot/chat",
                "method": "POST",
                "data": {
                    "user_id": "test_user_123",
                    "message": "회원가입은 어떻게 하나요?"
                }
            },
            {
                "name": "제안 질문 테스트",
                "endpoint": "/api/v1/chatbot/suggestions",
                "method": "GET",
                "data": None
            },
            {
                "name": "카테고리 테스트",
                "endpoint": "/api/v1/chatbot/categories",
                "method": "GET",
                "data": None
            },
            {
                "name": "챗봇 상태 테스트",
                "endpoint": "/api/v1/chatbot/health",
                "method": "GET", 
                "data": None
            }
        ]
        
        all_passed = True
        
        for test_case in test_cases:
            print(f"\n🧪 {test_case['name']}")
            
            try:
                url = f"{self.ai_service_url}{test_case['endpoint']}"
                
                if test_case['method'] == 'POST':
                    response = requests.post(url, json=test_case['data'], timeout=10)
                else:
                    response = requests.get(url, timeout=10)
                
                if response.status_code == 200:
                    print(f"   ✅ 성공 (200)")
                    try:
                        json_response = response.json()
                        if json_response.get('success', False):
                            print(f"   ✅ 응답 구조 정상")
                        else:
                            print(f"   ⚠️  응답에 success=False")
                    except:
                        print(f"   ⚠️  JSON 파싱 실패")
                else:
                    print(f"   ❌ 실패 ({response.status_code})")
                    all_passed = False
                    
            except requests.exceptions.RequestException as e:
                print(f"   ❌ 요청 실패: {e}")
                all_passed = False
        
        return all_passed
    
    def test_spring_boot_integration(self):
        """Spring Boot 통합 테스트"""
        print("\n=== Spring Boot 통합 테스트 ===")
        
        try:
            response = requests.get(f"{self.spring_boot_url}/api/chatbot/status", timeout=5)
            print(f"✅ Spring Boot 챗봇 API 연결됨: {response.status_code}")
            return True
        except requests.exceptions.RequestException as e:
            print(f"❌ Spring Boot 서버 연결 실패: {e}")
            print("Spring Boot 서버를 먼저 실행해주세요.")
            return False
    
    def run_performance_test(self):
        """기본 성능 테스트"""
        print("\n=== 기본 성능 테스트 ===")
        
        try:
            # 간단한 응답 시간 테스트
            start_time = time.time()
            response = requests.post(
                f"{self.ai_service_url}/api/v1/chatbot/chat",
                json={"user_id": "perf_test", "message": "안녕하세요"},
                timeout=30
            )
            end_time = time.time()
            
            response_time = end_time - start_time
            
            print(f"응답 시간: {response_time:.2f}초")
            
            if response_time < 5.0:
                print("✅ 응답 시간 양호 (5초 미만)")
                return True
            else:
                print("⚠️  응답 시간 느림 (5초 이상)")
                return False
                
        except Exception as e:
            print(f"❌ 성능 테스트 실패: {e}")
            return False
    
    def generate_test_report(self):
        """테스트 결과 리포트 생성"""
        print("\n=== 테스트 결과 리포트 ===")
        
        report_lines = [
            "# 챗봇 시스템 테스트 결과",
            f"테스트 실행 시간: {time.strftime('%Y-%m-%d %H:%M:%S')}",
            "",
            "## 테스트 결과 요약"
        ]
        
        for test_name, result in self.test_results.items():
            status = "✅ 통과" if result.get('passed', False) else "❌ 실패"
            report_lines.append(f"- {test_name}: {status}")
        
        report_content = "\n".join(report_lines)
        
        # 리포트 파일 저장
        report_file = Path("test_report.md")
        report_file.write_text(report_content, encoding='utf-8')
        
        print(report_content)
        print(f"\n📋 상세 리포트가 {report_file.absolute()}에 저장되었습니다.")
    
    def run_all_tests(self):
        """모든 테스트 실행"""
        print("🚀 챗봇 시스템 통합 테스트를 시작합니다...\n")
        
        # 환경 체크
        if not self.check_environment():
            return False
        
        # 단위 테스트
        unit_test_passed = self.run_unit_tests()
        self.test_results['unit_tests'] = {'passed': unit_test_passed}
        
        # API 테스트
        api_test_passed = self.test_api_manually()
        self.test_results['api_tests'] = {'passed': api_test_passed}
        
        # Spring Boot 통합 테스트 
        spring_test_passed = self.test_spring_boot_integration()
        self.test_results['spring_integration'] = {'passed': spring_test_passed}
        
        # 성능 테스트
        perf_test_passed = self.run_performance_test()
        self.test_results['performance_tests'] = {'passed': perf_test_passed}
        
        # 리포트 생성
        self.generate_test_report()
        
        # 전체 결과
        all_passed = all(result.get('passed', False) for result in self.test_results.values())
        
        if all_passed:
            print("\n🎉 모든 테스트가 통과했습니다!")
            return True
        else:
            print("\n⚠️  일부 테스트가 실패했습니다. 위의 결과를 확인해주세요.")
            return False

def main():
    """메인 실행 함수"""
    runner = TestRunner()
    
    if len(sys.argv) > 1:
        command = sys.argv[1]
        
        if command == "unit":
            runner.run_unit_tests()
        elif command == "api":
            runner.test_api_manually()
        elif command == "spring":
            runner.test_spring_boot_integration()
        elif command == "perf":
            runner.run_performance_test()
        else:
            print("사용법: python test_runner.py [unit|api|spring|perf]")
    else:
        runner.run_all_tests()

if __name__ == "__main__":
    main()