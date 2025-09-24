#!/usr/bin/env python3
"""
잡았다 AI Service 실행 스크립트
"""

import uvicorn
import sys
import os
from pathlib import Path

# 프로젝트 루트를 Python 경로에 추가
project_root = Path(__file__).parent
sys.path.insert(0, str(project_root))

if __name__ == "__main__":
    # 환경변수 설정
    os.environ.setdefault("PYTHONPATH", str(project_root))
    
    # FastAPI 서버 실행
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8001,
        reload=True,  # 개발 환경에서만 사용
        log_level="info"
    )