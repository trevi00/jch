#!/usr/bin/env python3
"""
Basic API test without unicode characters
"""

import requests
import json

def test_basic_endpoints():
    """Test basic endpoints"""
    base_url = "http://localhost:8001"
    test_user_id = "test_user_123"
    
    print("=== Chatbot API Test ===")
    
    # Test 1: Health check
    print("\n1. Health Check")
    try:
        response = requests.get(f"{base_url}/health", timeout=5)
        print(f"   Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            print(f"   Response: {data}")
            print("   PASSED")
        else:
            print("   FAILED")
    except Exception as e:
        print(f"   ERROR: {e}")
    
    # Test 2: Chat endpoint
    print("\n2. Chat Test")
    try:
        response = requests.post(
            f"{base_url}/api/v1/chatbot/chat",
            json={"user_id": test_user_id, "message": "How do I sign up?"},
            timeout=10
        )
        print(f"   Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            print(f"   Success: {data.get('success')}")
            if 'data' in data and 'response' in data['data']:
                print(f"   Bot Response: {data['data']['response'][:100]}...")
            print("   PASSED")
        else:
            print("   FAILED")
    except Exception as e:
        print(f"   ERROR: {e}")
    
    # Test 3: Suggestions
    print("\n3. Suggestions Test")
    try:
        response = requests.get(f"{base_url}/api/v1/chatbot/suggestions", timeout=5)
        print(f"   Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            if 'data' in data and 'suggestions' in data['data']:
                print(f"   Suggestions count: {len(data['data']['suggestions'])}")
            print("   PASSED")
        else:
            print("   FAILED")
    except Exception as e:
        print(f"   ERROR: {e}")
    
    # Test 4: Categories
    print("\n4. Categories Test")
    try:
        response = requests.get(f"{base_url}/api/v1/chatbot/categories", timeout=5)
        print(f"   Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            if 'data' in data and 'categories' in data['data']:
                print(f"   Categories count: {len(data['data']['categories'])}")
            print("   PASSED")
        else:
            print("   FAILED")
    except Exception as e:
        print(f"   ERROR: {e}")
    
    # Test 5: Chat History
    print("\n5. Chat History Test")
    try:
        response = requests.get(f"{base_url}/api/v1/chatbot/history/{test_user_id}", timeout=5)
        print(f"   Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            if 'data' in data:
                print(f"   History items: {data['data'].get('total_messages', 0)}")
            print("   PASSED")
        else:
            print("   FAILED")
    except Exception as e:
        print(f"   ERROR: {e}")
    
    # Test 6: Status
    print("\n6. Status Test")
    try:
        response = requests.get(f"{base_url}/api/v1/chatbot/status", timeout=5)
        print(f"   Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            if 'data' in data:
                print(f"   Chatbot service: {data['data'].get('chatbot_service', 'unknown')}")
            print("   PASSED")
        else:
            print("   FAILED")
    except Exception as e:
        print(f"   ERROR: {e}")

    print("\n=== Test Complete ===")

if __name__ == "__main__":
    test_basic_endpoints()