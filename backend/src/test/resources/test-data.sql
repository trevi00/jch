-- E2E 테스트용 사용자 데이터
-- 비밀번호는 모두 "password" (BCrypt로 인코딩됨)

-- 일반 사용자들 (모든 필수 컬럼 포함)
-- 비밀번호는 모두 "password" (BCrypt로 인코딩됨)
INSERT INTO users (id, email, password, name, phone_number, user_type, email_verified, company_email_verified, account_locked, login_attempt_count, employment_status, is_deleted, created_at, updated_at) VALUES
(1, 'testuser@example.com', '$2a$10$S6DVm6Soks2W.5zHRnrnA.AE7O1B.PSXElewklsn3ylB67SfWOp1i', '테스트 사용자', '010-1234-5678', 'GENERAL', true, false, false, 0, 'JOB_SEEKING', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'emailtest@example.com', '$2a$10$S6DVm6Soks2W.5zHRnrnA.AE7O1B.PSXElewklsn3ylB67SfWOp1i', '이메일 테스트 사용자', '010-1234-5679', 'GENERAL', true, false, false, 0, 'JOB_SEEKING', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'chatbottest@example.com', '$2a$10$S6DVm6Soks2W.5zHRnrnA.AE7O1B.PSXElewklsn3ylB67SfWOp1i', '챗봇 테스트 사용자', '010-1234-5680', 'GENERAL', true, false, false, 0, 'JOB_SEEKING', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'communitytest@example.com', '$2a$10$S6DVm6Soks2W.5zHRnrnA.AE7O1B.PSXElewklsn3ylB67SfWOp1i', '커뮤니티 테스트 사용자', '010-1234-5681', 'GENERAL', true, false, false, 0, 'JOB_SEEKING', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'jobtest@example.com', '$2a$10$S6DVm6Soks2W.5zHRnrnA.AE7O1B.PSXElewklsn3ylB67SfWOp1i', '채용 테스트 사용자', '010-1234-5682', 'GENERAL', true, false, false, 0, 'JOB_SEEKING', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 기업 사용자들 (모든 필수 컬럼 포함)
INSERT INTO users (id, email, password, name, phone_number, user_type, email_verified, company_email_verified, account_locked, login_attempt_count, employment_status, is_deleted, created_at, updated_at) VALUES
(10, 'company@example.com', '$2a$10$S6DVm6Soks2W.5zHRnrnA.AE7O1B.PSXElewklsn3ylB67SfWOp1i', '테스트 기업', '02-1234-5678', 'COMPANY', true, false, false, 0, 'EMPLOYED', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'jobcompany@example.com', '$2a$10$S6DVm6Soks2W.5zHRnrnA.AE7O1B.PSXElewklsn3ylB67SfWOp1i', '채용 테스트 기업', '02-1234-5679', 'COMPANY', true, false, false, 0, 'EMPLOYED', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 관리자 사용자 (모든 필수 컬럼 포함)  
INSERT INTO users (id, email, password, name, phone_number, user_type, email_verified, company_email_verified, account_locked, login_attempt_count, employment_status, is_deleted, created_at, updated_at) VALUES
(20, 'admin@example.com', '$2a$10$S6DVm6Soks2W.5zHRnrnA.AE7O1B.PSXElewklsn3ylB67SfWOp1i', '관리자', '02-1234-5680', 'ADMIN', true, false, false, 0, 'EMPLOYED', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 커뮤니티 카테고리
INSERT INTO categories (id, name, description, is_deleted, active, displayOrder, created_at, updated_at) VALUES
(1, '일반', '일반적인 취업 관련 게시글', false, true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Q&A', '질문과 답변', false, true, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '후기', '면접 후기 및 취업 후기', false, true, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 자동 증가 시퀀스 재설정
ALTER TABLE users AUTO_INCREMENT = 100;
ALTER TABLE categories AUTO_INCREMENT = 10;