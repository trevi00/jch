-- JBD (Job Board) 프로젝트 데이터베이스 스키마
-- Generated: 2025-09-15
-- 설계 스펙 기준 속성명 적용 버전

-- 1. 사용자 테이블 (users)
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    name VARCHAR(100) NOT NULL,
    user_type ENUM('GENERAL', 'COMPANY') NOT NULL DEFAULT 'GENERAL',
    email_verified BOOLEAN DEFAULT FALSE,
    company_email_verified BOOLEAN DEFAULT FALSE,

    -- OAuth 인증 관련
    oauth_id VARCHAR(255),
    oauth_provider VARCHAR(50),

    -- 프로필 정보
    profile_image_url TEXT,
    phone VARCHAR(20),
    birth_date DATE,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    address TEXT,
    website_url TEXT,
    github_url TEXT,
    linkedin_url TEXT,

    -- 취업 관련 정보
    current_job_title VARCHAR(200),
    career_summary TEXT,
    desired_salary_min INT,
    desired_salary_max INT,
    desired_job_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'FREELANCE', 'PROJECT_BASED', 'TEMPORARY', 'REMOTE', 'HYBRID'),
    desired_location TEXT,
    work_experience_years INT,

    -- 개인 설정
    notification_enabled BOOLEAN DEFAULT TRUE,
    email_notification_enabled BOOLEAN DEFAULT TRUE,
    sms_notification_enabled BOOLEAN DEFAULT FALSE,
    job_alert_enabled BOOLEAN DEFAULT TRUE,
    marketing_consent BOOLEAN DEFAULT FALSE,
    data_processing_consent BOOLEAN DEFAULT TRUE,
    third_party_sharing_consent BOOLEAN DEFAULT FALSE,

    -- 활동 정보
    last_login_at TIMESTAMP,
    last_job_search_at TIMESTAMP,
    profile_completion_rate INT DEFAULT 0,
    job_application_count INT DEFAULT 0,
    job_view_count INT DEFAULT 0,

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    INDEX idx_email (email),
    INDEX idx_user_type (user_type),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted_at (deleted_at),
    INDEX idx_oauth_provider_id (oauth_provider, oauth_id)
);

-- 2. 회사 정보 테이블 (companies)
CREATE TABLE companies (
    company_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    business_number VARCHAR(50),
    industry VARCHAR(100),
    company_size ENUM('STARTUP', 'SMALL', 'MEDIUM', 'LARGE', 'ENTERPRISE'),
    establishment_date DATE,
    company_description TEXT,
    company_website TEXT,
    company_address TEXT,
    company_logo_url TEXT,

    -- 추가 회사 정보
    employee_count INT,
    annual_revenue BIGINT,
    company_culture TEXT,
    benefits TEXT,

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_company_name (company_name),
    INDEX idx_industry (industry),
    INDEX idx_deleted_at (deleted_at)
);

-- 3. 사용자 학력 테이블 (user_education)
CREATE TABLE user_education (
    education_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    school_name VARCHAR(200) NOT NULL,
    degree VARCHAR(100),
    major VARCHAR(150),
    gpa DECIMAL(3,2),
    start_date DATE,
    end_date DATE,
    is_current BOOLEAN DEFAULT FALSE,
    description TEXT,

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_deleted_at (deleted_at)
);

-- 4. 사용자 경력 테이블 (user_experiences)
CREATE TABLE user_experiences (
    experience_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    position VARCHAR(150) NOT NULL,
    employment_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'FREELANCE'),
    start_date DATE NOT NULL,
    end_date DATE,
    is_current BOOLEAN DEFAULT FALSE,
    description TEXT,
    achievements TEXT,

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_deleted_at (deleted_at)
);

-- 5. 사용자 기술/스킬 테이블 (user_skills)
CREATE TABLE user_skills (
    user_skill_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    proficiency_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT') DEFAULT 'BEGINNER',
    years_of_experience INT DEFAULT 0,

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_skill_name (skill_name),
    INDEX idx_deleted_at (deleted_at),
    UNIQUE KEY unique_user_skill (user_id, skill_name)
);

-- 6. 사용자 자격증 테이블 (user_certifications)
CREATE TABLE user_certifications (
    certification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    certification_name VARCHAR(200) NOT NULL,
    issuing_organization VARCHAR(200),
    issue_date DATE,
    expiry_date DATE,
    credential_id VARCHAR(100),
    credential_url TEXT,

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_deleted_at (deleted_at)
);

-- 7. 사용자 포트폴리오 테이블 (user_portfolios)
CREATE TABLE user_portfolios (
    portfolio_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    project_url TEXT,
    github_url TEXT,
    image_url TEXT,
    technologies_used TEXT,
    start_date DATE,
    end_date DATE,

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_deleted_at (deleted_at)
);

-- 8. 채용공고 테이블 (job_postings)
CREATE TABLE job_postings (
    job_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    department VARCHAR(100),
    location VARCHAR(200),
    job_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'FREELANCE', 'PROJECT_BASED', 'TEMPORARY', 'REMOTE', 'HYBRID') NOT NULL,
    experience_level ENUM('ENTRY_LEVEL', 'JUNIOR', 'MID_LEVEL', 'SENIOR', 'EXPERT', 'MANAGER', 'DIRECTOR', 'ANY') NOT NULL,
    description TEXT NOT NULL,
    qualifications TEXT,
    required_skills TEXT,
    preferred_skills TEXT,

    -- 급여 정보
    min_salary INT,
    max_salary INT,
    salary_type ENUM('HOURLY', 'MONTHLY', 'YEARLY') DEFAULT 'YEARLY',

    -- 공고 상태 및 일정
    status ENUM('DRAFT', 'PUBLISHED', 'CLOSED', 'EXPIRED') DEFAULT 'DRAFT',
    application_deadline DATE,
    start_date DATE,

    -- 통계 정보
    view_count INT DEFAULT 0,
    application_count INT DEFAULT 0,

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (company_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_company_user_id (company_user_id),
    INDEX idx_title (title),
    INDEX idx_location (location),
    INDEX idx_job_type (job_type),
    INDEX idx_experience_level (experience_level),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted_at (deleted_at)
);

-- 9. 채용지원 테이블 (job_applications)
CREATE TABLE job_applications (
    application_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    cover_letter TEXT,
    resume_url TEXT,
    status ENUM('SUBMITTED', 'REVIEWED', 'DOCUMENT_PASSED', 'INTERVIEW_SCHEDULED', 'INTERVIEW_PASSED', 'HIRED', 'REJECTED', 'WITHDRAWN') DEFAULT 'SUBMITTED',

    -- 지원 관련 추가 정보
    expected_salary INT,
    available_start_date DATE,
    notes TEXT,

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (job_id) REFERENCES job_postings(job_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_job_id (job_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted_at (deleted_at),
    UNIQUE KEY unique_application (job_id, user_id)
);

-- 10. 커뮤니티 카테고리 테이블 (categories)
CREATE TABLE categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    color VARCHAR(7) DEFAULT '#007bff',
    icon VARCHAR(50),
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_display_order (display_order),
    INDEX idx_is_active (is_active)
);

-- 11. 커뮤니티 게시글 테이블 (posts)
CREATE TABLE posts (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    image_url TEXT,

    -- 게시글 상태 및 통계
    is_published BOOLEAN DEFAULT TRUE,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,

    -- AI 관련 필드
    sentiment_score DECIMAL(3,2),
    sentiment_label VARCHAR(20),

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE RESTRICT,
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_title (title),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted_at (deleted_at),
    INDEX idx_is_published (is_published)
);

-- 12. 댓글 테이블 (comments)
CREATE TABLE comments (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_comment_id BIGINT,
    content TEXT NOT NULL,

    -- 댓글 통계
    like_count INT DEFAULT 0,

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_comment_id (parent_comment_id),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted_at (deleted_at)
);

-- 13. AI 모의면접 테이블 (interviews)
CREATE TABLE interviews (
    interview_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    field VARCHAR(100),
    experience_level VARCHAR(50),
    interview_type VARCHAR(50),
    status ENUM('IN_PROGRESS', 'COMPLETED', 'ABANDONED') DEFAULT 'IN_PROGRESS',

    -- 면접 결과 및 피드백
    total_score DECIMAL(5,2),
    feedback TEXT,
    recommendations TEXT,

    -- 면접 세부 정보
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    duration_minutes INT,
    question_count INT DEFAULT 0,
    answered_count INT DEFAULT 0,

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- 14. AI 면접 질문 테이블 (interview_questions)
CREATE TABLE interview_questions (
    question_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interview_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50),
    question_order INT NOT NULL,

    -- 답변 및 평가
    user_answer TEXT,
    ai_feedback TEXT,
    score DECIMAL(5,2),
    keywords TEXT,

    -- 답변 시간 정보
    time_limit_seconds INT,
    actual_time_seconds INT,

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (interview_id) REFERENCES interviews(interview_id) ON DELETE CASCADE,
    INDEX idx_interview_id (interview_id),
    INDEX idx_question_order (question_order),
    INDEX idx_question_type (question_type)
);

-- 15. 자격증 검증 요청 테이블 (certificate_requests)
CREATE TABLE certificate_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    request_type VARCHAR(100) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED') DEFAULT 'PENDING',
    request_data JSON,
    response_data JSON,

    -- 처리 정보
    processed_by BIGINT,
    processed_at TIMESTAMP,
    notes TEXT,

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (processed_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_request_type (request_type),
    INDEX idx_created_at (created_at)
);

-- 16. 시스템 메트릭 테이블 (system_metrics)
CREATE TABLE system_metrics (
    metric_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,4),
    metric_type VARCHAR(50),
    measurement_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tags JSON,

    INDEX idx_metric_name (metric_name),
    INDEX idx_measurement_time (measurement_time),
    INDEX idx_metric_type (metric_type)
);

-- 17. 이메일 히스토리 테이블 (email_history)
CREATE TABLE email_history (
    email_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_email VARCHAR(255) NOT NULL,
    sender_email VARCHAR(255),
    subject VARCHAR(500),
    content TEXT,
    email_type VARCHAR(50),
    status ENUM('SENT', 'FAILED', 'PENDING') DEFAULT 'PENDING',
    provider VARCHAR(50),

    -- 발송 정보
    sent_at TIMESTAMP,
    error_message TEXT,
    external_message_id VARCHAR(255),

    -- Audit 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_recipient_email (recipient_email),
    INDEX idx_sender_email (sender_email),
    INDEX idx_status (status),
    INDEX idx_email_type (email_type),
    INDEX idx_sent_at (sent_at),
    INDEX idx_created_at (created_at)
);

-- 초기 카테고리 데이터 삽입
INSERT INTO categories (name, description, color, icon, display_order) VALUES
('일반', '일반적인 취업 관련 정보와 질문', '#6c757d', 'chat', 1),
('면접후기', '실제 면접 경험담과 후기', '#28a745', 'clipboard-check', 2),
('자기소개서', '자기소개서 작성 팁과 첨삭', '#17a2b8', 'file-text', 3),
('AI', 'AI 관련 기술과 취업 정보', '#6f42c1', 'cpu', 4),
('취업정보', '각종 채용 정보와 기업 분석', '#fd7e14', 'briefcase', 5);