#!/usr/bin/env python3
"""
Simple API test script without Korean characters
"""

import requests
import json
from datetime import datetime

def test_chatbot_api():
    """Test chatbot API functionality"""
    base_url = "http://localhost:8001"
    test_user_id = "test_user_123"
    
    print("Chatbot API Test Started")
    print("=" * 60)
    
    # Test cases
    test_cases = [
        # 1. Server health check
        {
            "name": "Health Check",
            "method": "GET",
            "url": f"{base_url}/health",
            "expected_status": 200
        },
        
        # 2. Chatbot conversation test 1
        {
            "name": "Chat - Membership inquiry",
            "method": "POST", 
            "url": f"{base_url}/api/v1/chatbot/chat",
            "json": {
                "user_id": test_user_id,
                "message": "How do I sign up?"
            },
            "expected_status": 200
        },
        
        # 3. Chatbot conversation test 2
        {
            "name": "Chat - AI interview inquiry",
            "method": "POST",
            "url": f"{base_url}/api/v1/chatbot/chat", 
            "json": {
                "user_id": test_user_id,
                "message": "I want to use AI interview features"
            },
            "expected_status": 200
        },
        
        # 4. Empty message test
        {
            "name": "Chat - Empty message",
            "method": "POST",
            "url": f"{base_url}/api/v1/chatbot/chat",
            "json": {
                "user_id": test_user_id,
                "message": ""
            },
            "expected_status": 200,
            "expected_success": False
        },
        
        # 5. Get suggestions
        {
            "name": "Get Suggestions",
            "method": "GET",
            "url": f"{base_url}/api/v1/chatbot/suggestions",
            "expected_status": 200
        },
        
        # 6. Get categories
        {
            "name": "Get Categories",
            "method": "GET", 
            "url": f"{base_url}/api/v1/chatbot/categories",
            "expected_status": 200
        },
        
        # 7. Get chat history
        {
            "name": "Get Chat History",
            "method": "GET",
            "url": f"{base_url}/api/v1/chatbot/history/{test_user_id}",
            "expected_status": 200
        },
        
        # 8. Get chatbot status
        {
            "name": "Get Chatbot Status",
            "method": "GET",
            "url": f"{base_url}/api/v1/chatbot/status",
            "expected_status": 200
        },
        
        # 9. Clear chat history
        {
            "name": "Clear Chat History",
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
            # Execute HTTP request
            if test_case['method'] == 'GET':
                response = requests.get(test_case['url'], timeout=10)
            elif test_case['method'] == 'POST':
                response = requests.post(test_case['url'], json=test_case.get('json'), timeout=10)
            elif test_case['method'] == 'DELETE':
                response = requests.delete(test_case['url'], timeout=10)
            
            # Check status code
            if response.status_code == test_case['expected_status']:
                print(f"   ✓ Status code passed ({response.status_code})")
                
                # Parse JSON response
                try:
                    data = response.json()
                    
                    # Check success field (if expected)
                    if 'expected_success' in test_case:
                        if data.get('success') == test_case['expected_success']:
                            print(f"   ✓ Success field passed ({data.get('success')})")
                        else:
                            print(f"   ✗ Success field failed (expected: {test_case['expected_success']}, actual: {data.get('success')})")
                            continue
                    elif data.get('success') is True:
                        print(f"   ✓ Response successful")
                    
                    # Display response data (brief)
                    if 'data' in data and data['data']:
                        if 'response' in data['data']:
                            print(f"   💬 Bot response: {data['data']['response'][:100]}...")
                        elif 'suggestions' in data['data']:
                            print(f"   📝 Suggestions count: {len(data['data']['suggestions'])}")
                        elif 'categories' in data['data']:
                            print(f"   📂 Categories count: {len(data['data']['categories'])}")
                        elif 'history' in data['data']:
                            print(f"   📚 History items: {data['data']['total_messages']}")
                        elif 'chatbot_service' in data['data']:
                            print(f"   🔧 Chatbot status: {data['data']['chatbot_service']}")
                    
                    passed_tests += 1
                    
                except json.JSONDecodeError:
                    print(f"   ⚠ JSON parsing failed")
                    print(f"   📄 Raw response: {response.text[:200]}...")
                    
            else:
                print(f"   ✗ Status code failed (expected: {test_case['expected_status']}, actual: {response.status_code})")
                print(f"   📄 Response: {response.text}")
                
        except requests.exceptions.ConnectionError:
            print(f"   💥 Connection failed: Server is not running")
        except requests.exceptions.Timeout:
            print(f"   ⏰ Timeout: Server response too slow")  
        except Exception as e:
            print(f"   🚨 Error: {e}")
    
    # Final results
    print("\n" + "=" * 60)
    print("🎯 Test Results Summary")
    print(f"   Passed: {passed_tests}/{total_tests}")
    print(f"   Success rate: {(passed_tests/total_tests)*100:.1f}%")
    
    if passed_tests == total_tests:
        print("   🎉 All tests passed!")
        return True
    else:
        print("   ⚠ Some tests failed")
        return False

def test_conversation_flow():
    """Test conversation flow"""
    print("\n" + "="*60)
    print("🗣 Real conversation flow test")
    print("="*60)
    
    base_url = "http://localhost:8001"
    user_id = "conversation_test_user"
    
    # Conversation scenario
    conversation = [
        "Hello",
        "How do I sign up?", 
        "What is AI interview?",
        "Can I get help with cover letter?",
        "Thank you"
    ]
    
    for i, message in enumerate(conversation, 1):
        print(f"\n{i}. User: {message}")
        
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
                    print(f"   🤖 Bot: {bot_response}")
                else:
                    print(f"   ✗ Response failed: {data.get('message', 'Unknown error')}")
            else:
                print(f"   ✗ HTTP error: {response.status_code}")
                
        except Exception as e:
            print(f"   🚨 Error: {e}")
    
    print("\n🎭 Conversation flow test complete!")

if __name__ == "__main__":
    # Basic API test
    api_success = test_chatbot_api()
    
    # Conversation flow test
    test_conversation_flow()
    
    print(f"\n🏁 All tests complete!")
    if api_success:
        print("✅ Chatbot system is working properly!")
    else:
        print("⚠ Some features have issues.")