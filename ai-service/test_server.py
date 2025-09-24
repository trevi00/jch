#!/usr/bin/env python3
"""
간단한 테스트 서버 - SSL 문제 해결을 위한 최소한의 FastAPI 서버
"""

from fastapi import FastAPI
import uvicorn

# 최소한의 FastAPI 앱
app = FastAPI(title="잡았다 AI Service - 테스트 버전")

@app.get("/")
async def root():
    return {"message": "잡았다 AI Service 테스트 서버", "status": "running"}

@app.get("/health")
async def health_check():
    return {"status": "healthy", "version": "test-1.0.0"}

@app.get("/api/v1/test")
async def test_endpoint():
    return {
        "success": True,
        "message": "테스트 엔드포인트가 정상 작동합니다",
        "data": {"test": True}
    }

if __name__ == "__main__":
    print("🚀 잡았다 AI Service 테스트 서버를 시작합니다...")
    print("📡 URL: http://localhost:8001")
    print("🔍 Health Check: http://localhost:8001/health")
    print("⚡ Test API: http://localhost:8001/api/v1/test")
    print("📚 자동 문서: http://localhost:8001/docs")
    print("✋ 종료: Ctrl+C")
    print("=" * 50)
    
    uvicorn.run(
        app,
        host="localhost",
        port=8001,
        reload=True,
        log_level="info"
    )