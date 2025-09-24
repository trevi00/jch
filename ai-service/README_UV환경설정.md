# ⚡ UV로 잡았다 AI 서비스 환경설정

UV는 Rust로 작성된 **초고속 Python 패키지 관리자**입니다. pip보다 **10-100배 빠르고** 가상환경 관리도 자동으로 해줍니다!

## 🚀 빠른 시작

### 1. UV 자동 설치 + 환경 구성
```cmd
# ai-service 폴더에서 실행 (관리자 권한 권장)
setup_uv_env.bat
```

### 2. 편리한 명령어 사용
```cmd
# FastAPI 서버 실행
uv_commands.bat serve

# 테스트 실행
uv_commands.bat test

# 모든 명령어 보기
uv_commands.bat
```

## 📋 UV 주요 명령어

### 프로젝트 관리
```cmd
uv sync                    # 의존성 설치/동기화 (가상환경 자동 생성)
uv add requests           # 패키지 추가
uv remove requests        # 패키지 제거
uv tree                   # 의존성 트리 확인
```

### 실행 명령어
```cmd
uv run python main.py     # FastAPI 서버 실행
uv run pytest            # 테스트 실행
uv run python -m pip list # 설치된 패키지 목록
```

### 개발 도구
```cmd
# 코드 포맷팅 (개발 의존성 설치 후)
uv run black .            # 코드 포매터
uv run isort .            # import 정렬
uv run flake8 .           # 린터
uv run mypy .             # 타입 체커
```

## 🛠️ 프로젝트 구조

```
ai-service/
├── pyproject.toml          # UV 프로젝트 설정 (requirements.txt 대체)
├── uv.lock                # 의존성 잠금 파일 (자동 생성)
├── .venv/                 # 가상환경 (자동 생성/관리)
├── setup_uv_env.bat       # UV 설치 + 환경 구성 스크립트
├── uv_commands.bat        # 편의 명령어 스크립트
├── main.py               # FastAPI 애플리케이션
├── test_runner.py        # 테스트 러너
└── tests/               # 테스트 코드
```

## ⚙️ pyproject.toml 설정

주요 섹션들:
- **`[project]`**: 프로젝트 메타데이터 및 의존성
- **`[project.optional-dependencies]`**: 개발/테스트용 추가 패키지
- **`[project.scripts]`**: 커스텀 명령어 정의
- **`[tool.*]`**: 각종 개발 도구 설정

## 🎯 UV의 장점

### 1. **속도** ⚡
- pip보다 **10-100배 빠른** 패키지 설치
- 병렬 다운로드 및 캐싱

### 2. **편의성** 🎁
- 가상환경 **자동 생성/관리**
- `pip install` + `venv` + `pip-tools` 기능 통합

### 3. **호환성** 🔗
- 기존 `requirements.txt` 프로젝트와 호환
- PyPI 생태계 완전 지원

## 🔧 일반적인 작업 흐름

```cmd
# 1. 프로젝트 초기 설정 (한 번만)
setup_uv_env.bat

# 2. 개발 시작
uv_commands.bat serve      # 서버 실행

# 3. 새 터미널에서 테스트
uv_commands.bat test       # 테스트 실행

# 4. 새 패키지 추가
uv add numpy              # 프로덕션 의존성
uv add --dev pytest      # 개발 의존성

# 5. 환경 동기화 (팀원이나 배포 시)
uv sync
```

## 🐛 문제 해결

### UV 설치 실패
```cmd
# 수동 설치 (PowerShell 관리자 권한)
irm https://astral.sh/uv/install.ps1 | iex

# 또는 pip으로 설치
pip install uv
```

### SSL/인증서 오류
```cmd
# 신뢰할 수 있는 호스트 설정
uv pip install --trusted-host pypi.org [패키지명]
```

### 환경 초기화
```cmd
# 가상환경 재생성
rmdir /s .venv
uv sync
```

## 💡 팀 협업 팁

### 1. Lock 파일 공유
- `uv.lock` 파일을 Git에 커밋
- 모든 팀원이 동일한 버전 사용

### 2. 의존성 업데이트
```cmd
uv sync --upgrade      # 모든 패키지 최신 버전으로 업데이트
uv lock --upgrade      # lock 파일만 업데이트
```

### 3. 환경 일관성
```cmd
# 새로운 환경에서
git clone [repository]
cd ai-service
uv sync               # 정확히 동일한 환경 복원
```

## 📊 성능 비교

| 작업 | pip + venv | UV | 개선 |
|------|------------|----|----|
| 의존성 설치 | 45초 | 3초 | **15배 빠름** |
| 가상환경 생성 | 수동 | 자동 | **편의성 ↑** |
| 의존성 해결 | 느림 | 빠름 | **10배 빠름** |
| 캐시 관리 | 수동 | 자동 | **관리 불요** |

---

## ✅ UV 환경설정 체크리스트

- [ ] `setup_uv_env.bat` 실행 완료
- [ ] `uv --version` 명령어 동작 확인
- [ ] `uv run python main.py`로 서버 실행 테스트
- [ ] `uv run pytest`로 테스트 실행 확인
- [ ] `.env` 파일 생성 및 API 키 설정
- [ ] IDE에서 `.venv/Scripts/python.exe` 인터프리터 설정

**UV로 더 빠르고 편리한 Python 개발을 경험해보세요! ⚡🚀**