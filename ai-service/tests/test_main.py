import pytest
from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def test_health_check():
    """Test the health check endpoint"""
    response = client.get("/health")
    assert response.status_code == 200
    assert "status" in response.json()

def test_translation_languages():
    """Test get supported languages endpoint"""
    response = client.get("/translation/languages")
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    assert "data" in data

def test_chatbot_categories():
    """Test chatbot categories endpoint"""
    response = client.get("/chatbot/categories")
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    assert "data" in data

@pytest.mark.asyncio
async def test_invalid_endpoint():
    """Test invalid endpoint returns 404"""
    response = client.get("/invalid-endpoint")
    assert response.status_code == 404
