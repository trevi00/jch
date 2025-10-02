# JBD (잡았다) 데이터베이스 스키마 문서

## 개요
JBD(잡았다)는 취업 지원 솔루션 플랫폼으로, 다음과 같은 주요 기능을 제공합니다:
- 사용자 관리 (개인/기업)
- 채용공고 관리
- 커뮤니티 기능
- AI 면접 시스템
- 자격증 관리
- 포트폴리오 관리

## 데이터베이스 구성

### 서버 정보
- **Database**: MySQL 8.0.43
- **Character Set**: utf8mb4_unicode_ci
- **Engine**: InnoDB
- **총 테이블 수**: 25개

## 테이블 분류

### 🔐 사용자 관리 (User Management)
| 테이블명 | 설명 | 레코드 수 |
|---------|------|-----------|
| `users` | 사용자 기본 정보 (개인/기업/관리자) | 34 |
| `user_profiles` | 사용자 프로필 상세 정보 | 34 |
| `companies` | 기업 정보 | 2 |

### 💼 경력 및 교육 (Career & Education)
| 테이블명 | 설명 | 레코드 수 |
|---------|------|-----------|
| `career_histories` | 사용자 경력 사항 | 1 |
| `education_infos` | 교육 정보 | 0 |
| `user_education` | 사용자-교육 연결 테이블 | 1 |
| `skills` | 스킬 마스터 데이터 | 6 |
| `user_skills` | 사용자-스킬 연결 테이블 | 5 |

### 🏆 자격증 관리 (Certification Management)
| 테이블명 | 설명 | 레코드 수 |
|---------|------|-----------|
| `certifications` | 자격증 마스터 데이터 | 0 |
| `user_certifications` | 사용자 자격증 정보 | 1 |
| `certificate_requests` | 자격증 발급 요청 | 0 |

### 💼 채용 관리 (Job Management)
| 테이블명 | 설명 | 레코드 수 |
|---------|------|-----------|
| `job_postings` | 채용공고 | 3 |
| `job_applications` | 지원서 | 3 |

### 💬 커뮤니티 (Community)
| 테이블명 | 설명 | 레코드 수 |
|---------|------|-----------|
| `categories` | 게시판 카테고리 | 6 |
| `posts` | 커뮤니티 게시글 | 1 |
| `comments` | 댓글 | 0 |

### 🤖 AI 서비스 (AI Services)
| 테이블명 | 설명 | 레코드 수 |
|---------|------|-----------|
| `interviews` | AI 면접 기록 | 2 |
| `interview_questions` | 면접 질문 | 4 |
| `translation_requests` | 번역 요청 기록 | 0 |

### 📁 포트폴리오 (Portfolio)
| 테이블명 | 설명 | 레코드 수 |
|---------|------|-----------|
| `portfolios` | 포트폴리오 | 1 |

### 🎧 고객 지원 (Customer Support)
| 테이블명 | 설명 | 레코드 수 |
|---------|------|-----------|
| `support_tickets` | 고객 문의 티켓 | 0 |
| `support_messages` | 고객 문의 메시지 | 0 |
| `support_faqs` | 자주 묻는 질문 | 0 |

### 📧 시스템 (System)
| 테이블명 | 설명 | 레코드 수 |
|---------|------|-----------|
| `email_history` | 이메일 발송 기록 | 0 |
| `system_metrics` | 시스템 메트릭 | 0 |

## 주요 테이블 구조

### users (사용자)
```sql
CREATE TABLE users (
  user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  user_type ENUM('GENERAL', 'COMPANY', 'ADMIN'),
  email_verified BOOLEAN DEFAULT FALSE,
  company_email_verified BOOLEAN DEFAULT FALSE,
  is_admin BOOLEAN DEFAULT FALSE,
  is_deleted BOOLEAN DEFAULT FALSE,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6),
  deleted_at DATETIME(6)
);
```

### job_postings (채용공고)
```sql
CREATE TABLE job_postings (
  job_posting_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  company_name VARCHAR(255) NOT NULL,
  location VARCHAR(255),
  job_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP'),
  experience_level ENUM('ENTRY', 'JUNIOR', 'MID', 'SENIOR', 'EXECUTIVE'),
  description TEXT,
  qualifications TEXT,
  required_skills TEXT,
  min_salary DECIMAL(15,2),
  max_salary DECIMAL(15,2),
  deadline DATE,
  is_active BOOLEAN DEFAULT TRUE,
  is_deleted BOOLEAN DEFAULT FALSE,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6),
  FOREIGN KEY (company_id) REFERENCES users(user_id)
);
```

### interviews (AI 면접)
```sql
CREATE TABLE interviews (
  interview_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  job_role VARCHAR(255),
  interview_type ENUM('TECHNICAL', 'BEHAVIORAL', 'SITUATIONAL'),
  status ENUM('IN_PROGRESS', 'COMPLETED', 'CANCELLED'),
  overall_score INT,
  total_questions INT,
  answered_questions INT,
  created_at DATETIME(6) NOT NULL,
  completed_at DATETIME(6),
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

## 주요 관계 (Relationships)

### 1:N 관계
- `users` → `user_profiles` (사용자 프로필)
- `users` → `career_histories` (경력 사항)
- `users` → `job_postings` (채용공고 - 기업 사용자)
- `users` → `job_applications` (지원서)
- `users` → `interviews` (면접 기록)
- `users` → `posts` (게시글)
- `posts` → `comments` (댓글)
- `categories` → `posts` (카테고리별 게시글)

### N:M 관계
- `users` ↔ `skills` (through `user_skills`)
- `users` ↔ `certifications` (through `user_certifications`)
- `users` ↔ `education_infos` (through `user_education`)

## 인덱스 구성
- **Primary Keys**: 모든 테이블에 AUTO_INCREMENT PK
- **Foreign Keys**: 관계형 데이터 무결성 보장
- **Unique Keys**: 이메일, 카테고리명 등 고유성 보장

## 보안 및 제약사항
- **Soft Delete**: `is_deleted`, `deleted_at` 컬럼으로 논리적 삭제
- **UTF-8 지원**: `utf8mb4_unicode_ci` 콜레이션으로 다국어 지원
- **Foreign Key 제약**: 데이터 무결성 보장
- **NOT NULL 제약**: 필수 데이터 보장

## 데이터베이스 설정 정보

### 연결 설정
- **호스트**: localhost
- **포트**: 3306
- **데이터베이스명**: jobplatform
- **사용자명**: root
- **비밀번호**: 12345
- **타임존**: Asia/Seoul

### 환경 설정
```yaml
# application.yml 설정
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jobplatform?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
    username: root
    password: 12345
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    database-platform: org.hibernate.dialect.MySQLDialect
```

## 초기 데이터 설정

### 필수 마스터 데이터
1. **카테고리 (categories)** - 6개 기본 카테고리
2. **스킬 (skills)** - 6개 기본 스킬 (Java, Spring Boot, React, Python, TypeScript, JavaScript)

### 데이터베이스 초기화 방법
```sql
-- 1. 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS jobplatform
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 2. 스키마 적용
mysql -u root -p12345 -h localhost -P 3306 jobplatform < database-schema.sql

-- 3. 초기 데이터 적용
mysql -u root -p12345 -h localhost -P 3306 jobplatform < initial-data.sql
```

## 사용자 타입별 기능 매핑

### 일반 사용자 (GENERAL)
- ✅ 사용자 프로필 관리 (`user_profiles`)
- ✅ 경력 사항 관리 (`career_histories`)
- ✅ 교육 정보 관리 (`education_infos`, `user_education`)
- ✅ 스킬 관리 (`user_skills`)
- ✅ 자격증 관리 (`user_certifications`)
- ✅ 지원서 작성 (`job_applications`)
- ✅ AI 면접 참여 (`interviews`, `interview_questions`)
- ✅ 커뮤니티 참여 (`posts`, `comments`)
- ✅ 포트폴리오 관리 (`portfolios`)

### 기업 사용자 (COMPANY)
- ✅ 기업 정보 관리 (`companies`)
- ✅ 채용공고 등록 (`job_postings`)
- ✅ 지원자 관리 (`job_applications`)
- ✅ 기업 게시판 사용 (`posts` - 카테고리 5)

### 관리자 (ADMIN)
- ✅ 전체 사용자 관리 (`users`)
- ✅ 시스템 모니터링 (`system_metrics`)
- ✅ 고객 지원 (`support_tickets`, `support_messages`, `support_faqs`)
- ✅ 자격증 요청 승인 (`certificate_requests`)
- ✅ 이메일 발송 관리 (`email_history`)

## 성능 최적화 권장사항

### 인덱스 추가 권장
```sql
-- 자주 조회되는 컬럼에 인덱스 추가
CREATE INDEX idx_job_postings_location ON job_postings(location);
CREATE INDEX idx_job_postings_experience ON job_postings(experience_level);
CREATE INDEX idx_posts_category ON posts(category_id);
CREATE INDEX idx_posts_created ON posts(created_at);
CREATE INDEX idx_users_type ON users(user_type);
```

### 쿼리 최적화
- N+1 문제 방지를 위한 페치 조인 사용
- 페이징 처리 시 LIMIT/OFFSET 사용
- 대용량 데이터 조회 시 배치 처리 적용

## 백업 및 복원 가이드

### 전체 백업
```bash
# 스키마 + 데이터 백업
mysqldump -u root -p12345 -h localhost -P 3306 --routines --triggers jobplatform > full_backup_$(date +%Y%m%d_%H%M%S).sql

# 스키마만 백업
mysqldump -u root -p12345 -h localhost -P 3306 --no-data --routines --triggers jobplatform > schema_backup.sql
```

### 복원
```bash
mysql -u root -p12345 -h localhost -P 3306 jobplatform < backup_file.sql
```

## 문서 정보
- **생성일**: 2025-09-24
- **데이터베이스 버전**: MySQL 8.0.43
- **총 테이블 수**: 25개
- **총 레코드 수**: 약 100+개 레코드
- **문서 버전**: 1.0