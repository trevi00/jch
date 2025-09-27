# GitHub Actions 설정 가이드

## 필요한 GitHub Secrets 설정

GitHub 저장소의 Settings → Secrets and variables → Actions에서 다음 시크릿들을 추가해야 합니다:

### 1. OpenAI API Key
```
Name: OPENAI_API_KEY
Value: [AI 서비스 .env 파일의 OPENAI_API_KEY 값 사용]
```

### 2. Docker Hub 인증 (선택사항)
Docker Hub 계정이 있는 경우:
```
Name: DOCKER_USERNAME
Value: [Docker Hub 사용자명]

Name: DOCKER_PASSWORD
Value: [Docker Hub 액세스 토큰 또는 비밀번호]
```

### 3. 배포 서버 정보 (프로덕션 배포 시 필요)
```
Name: SSH_PRIVATE_KEY
Value: [서버 SSH 개인키]

Name: SSH_HOST
Value: 1.201.17.190

Name: SSH_USER
Value: root (또는 배포용 사용자)

Name: PROD_URL
Value: http://1.201.17.190
```

### 4. Slack 알림 (선택사항)
```
Name: SLACK_WEBHOOK
Value: [Slack Webhook URL]
```

## 워크플로우 파일 구조

```
.github/
├── workflows/
│   ├── ci.yml          # CI/CD 파이프라인 (테스트, 빌드)
│   └── deploy.yml      # 프로덕션 배포
└── dependabot.yml      # 의존성 자동 업데이트

backend/
├── Dockerfile          # Spring Boot Docker 이미지

frontend/
├── Dockerfile          # React + Nginx Docker 이미지
└── nginx.conf          # Nginx 설정

ai-service/
└── Dockerfile          # FastAPI Docker 이미지
```

## 워크플로우 실행 방법

### 1. 자동 실행
- **CI 파이프라인**: main, develop 브랜치에 push 또는 PR 생성 시
- **배포**: v* 태그 생성 시 (예: v1.0.0)

### 2. 수동 실행
- GitHub Actions 탭 → Deploy to Production 선택
- Run workflow 버튼 클릭
- 환경 선택 (production/staging)

## 테스트 스크립트 추가 필요

### Frontend (package.json)
```json
"scripts": {
  "test": "vitest",
  "lint": "eslint . --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
  "type-check": "tsc --noEmit"
}
```

### Backend
Gradle 테스트는 이미 설정되어 있음

### AI Service
```bash
# ai-service/tests/ 디렉토리에 pytest 테스트 파일 작성
```

## 주의사항

1. **OpenAI API Key 보안**:
   - 절대 코드에 직접 포함시키지 마세요
   - GitHub Secrets로만 관리하세요

2. **Docker Hub Rate Limit**:
   - 무료 계정은 pull 제한이 있습니다
   - 필요시 Docker Hub 인증 설정을 추가하세요

3. **데이터베이스 설정**:
   - CI 환경에서는 MySQL 컨테이너를 사용합니다
   - 프로덕션 환경과 분리되어 있습니다

4. **브랜치 보호 규칙 권장**:
   - main 브랜치에 직접 push 금지
   - PR 머지 전 CI 통과 필수
   - 코드 리뷰 필수

## 트러블슈팅

### CI 실패 시
1. Actions 탭에서 실패한 워크플로우 확인
2. 로그에서 에러 메시지 확인
3. 로컬에서 동일한 명령어 실행하여 재현

### Docker 빌드 실패
1. 로컬에서 Dockerfile 빌드 테스트
2. 의존성 버전 충돌 확인
3. 빌드 캐시 삭제 후 재시도

### 배포 실패
1. SSH 연결 확인
2. 서버 디스크 공간 확인
3. 서비스 로그 확인