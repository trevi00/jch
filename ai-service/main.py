import ssl
import os

# SSL verification 우회 설정 (PostgreSQL SSL 설정 문제 해결)
ssl._create_default_https_context = ssl._create_unverified_context

# 잘못된 CA bundle 환경변수 제거
for var in ['CURL_CA_BUNDLE', 'REQUESTS_CA_BUNDLE', 'SSL_CERT_FILE', 'SSL_CERT_DIR']:
    if var in os.environ:
        del os.environ[var]

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from dotenv import load_dotenv
from pathlib import Path

from routers import interview, cover_letter, translation, image_generation, chatbot, sentiment, simple_apis

# 환경 변수 로드
load_dotenv()

app = FastAPI(
    title="잡았다 AI Service",
    description="국비 학원 원생 취업 지원 솔루션 AI API",
    version="1.0.0",
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 프로덕션에서는 구체적으로 설정
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 라우터 등록
app.include_router(interview.router, prefix="/api/v1/interview", tags=["면접"])
app.include_router(cover_letter.router, prefix="/api/v1/cover-letter", tags=["자소서"])
app.include_router(translation.router, prefix="/api/v1/translation", tags=["번역"])
app.include_router(image_generation.router, prefix="/api/v1/image", tags=["이미지생성"])
app.include_router(chatbot.router, prefix="/api/v1/chatbot", tags=["챗봇"])
app.include_router(sentiment.router, prefix="/api/v1/sentiment", tags=["감정분석"])
app.include_router(simple_apis.router, prefix="/api/v1/simple", tags=["간단한API"])

# 정적 파일 서빙 설정 (저장된 이미지 제공)
static_images_dir = Path("static/images")
static_images_dir.mkdir(parents=True, exist_ok=True)
app.mount("/static", StaticFiles(directory="static"), name="static")

@app.get("/")
async def root():
    return {"message": "잡았다 AI Service API", "version": "1.0.0"}

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    
    host = os.getenv("FASTAPI_HOST", "localhost")
    port = int(os.getenv("FASTAPI_PORT", 8001))
    
    uvicorn.run(app, host=host, port=port)