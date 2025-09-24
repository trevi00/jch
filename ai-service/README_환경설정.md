 🚀 잡았다 AI 서비스 가상환경 설정 가이드

## 1. 가상환경 설정 (처음 한 번만)

### 방법 1: 자동 설정 스크립트 사용 (추천)
```cmd
# ai-service 폴더에서 실행
setup_env.bat
```

### 방법 2: 수동 설정
```cmd
# 1. 가상환경 생성
python -m venv venv

# 2. 가상환경 활성화 (Windows)
venv\Scripts\activate

# 3. pip 업그레이드
python -m pip install --upgrade pip

# 4. 패키지 설치
pip install -r requirements.txt
```

## 2. 일상적인 사용법

### 가상환경 활성화
```cmd
# 방법 1: 스크립트 사용
activate_env.bat

# 방법 2: 직접 활성화
venv\Scripts\activate
```

### FastAPI 서버 실행
```cmd
# 가상환경 활성화 후
python main.py
```

### 테스트 실행
```cmd
# 단위 테스트만
python test_runner.py unit

# 전체 테스트
python test_runner.py
```

### 가상환경 비활성화
```cmd
deactivate
```

## 3. 디렉토리 구조
```
ai-service/
├── venv/                 # 가상환경 폴더 (자동 생성)
├── setup_env.bat        # 초기 설정 스크립트
├── activate_env.bat     # 가상환경 활성화 스크립트
├── requirements.txt     # Python 패키지 목록
├── main.py             # FastAPI 메인 애플리케이션
├── test_runner.py      # 테스트 실행 도구
└── tests/              # 테스트 코드
```

## 4. 패키지 관리

### 새 패키지 설치
```cmd
# 가상환경 활성화 후
pip install 패키지명
pip freeze > requirements.txt  # requirements.txt 업데이트
```

### 설치된 패키지 확인
```cmd
pip list
```

## 5. 문제 해결

### Python 경로 문제
- 환경변수 PATH에 Python이 제대로 설정되어 있는지 확인
- `python --version` 명령어로 Python 설치 확인

### SSL 인증서 오류
```cmd
# 신뢰할 수 있는 호스트에서 설치
pip install -r requirements.txt --trusted-host pypi.org --trusted-host pypi.python.org --trusted-host files.pythonhosted.org
```

### 권한 문제
```cmd
# 관리자 권한으로 명령 프롬프트 실행
# 또는 사용자 디렉토리에 설치
pip install --user -r requirements.txt
```

## 6. IDE 연동 (IntelliJ/PyCharm)

1. **File** > **Settings** > **Project** > **Python Interpreter**
2. **Add Interpreter** > **Existing Environment**
3. 경로 설정: `C:\Users\user\IdeaProjects\jbd\ai-service\venv\Scripts\python.exe`

## 7. 환경변수 설정

`.env` 파일에 다음 내용 추가:
```
OPENAI_API_KEY=your_openai_api_key_here
FASTAPI_HOST=localhost
FASTAPI_PORT=8001
```

## 8. 자주 사용하는 명령어 모음

```cmd
# 프로젝트 초기화
setup_env.bat

# 개발 시작
activate_env.bat
python main.py

# 테스트 실행
python test_runner.py

# 패키지 관리
pip list
pip freeze > requirements.txt
```

---

## ✅ 설정 완료 체크리스트

- [ ] Python 설치 확인 (`python --version`)
- [ ] 가상환경 생성 (`setup_env.bat` 실행)
- [ ] 패키지 설치 완료 (requirements.txt)
- [ ] FastAPI 서버 실행 테스트 (`python main.py`)
- [ ] .env 파일 생성 및 API 키 설정
- [ ] IDE 인터프리터 설정 (선택사항)

**모든 체크리스트가 완료되면 개발을 시작할 수 있습니다! 🎉**