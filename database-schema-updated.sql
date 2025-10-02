-- MySQL Database Schema for Job Platform (jobplatform)
-- Updated: 2025-09-29
-- Database: jobplatform

-- Note: 실제 운영 환경 정보
-- Host: 192.168.50.54
-- Database Name: jobplatform (application.yml 기준)
-- Character Set: utf8mb4
-- Collation: utf8mb4_unicode_ci

-- 주요 수정 사항:
-- 1. portfolios 테이블의 portfolio_type enum에 'LIBRARY_FRAMEWORK' 추가 필요
-- 2. 데이터베이스 이름 확인: jobplatform 사용 중

-- 기존 database-schema.sql 파일 참조
-- 전체 스키마는 /root/jch/database-schema.sql 파일 참조

-- 수정이 필요한 부분:

-- portfolios 테이블 portfolio_type enum 수정
ALTER TABLE `portfolios`
MODIFY COLUMN `portfolio_type` enum(
    'API_BACKEND',
    'COMPETITION',
    'DATA_ANALYSIS',
    'DESIGN',
    'DESKTOP_APPLICATION',
    'HACKATHON',
    'LIBRARY_FRAMEWORK',  -- 새로 추가
    'MACHINE_LEARNING',
    'MOBILE_APPLICATION',
    'OPEN_SOURCE',
    'OTHER',
    'PERSONAL_PROJECT',
    'RESEARCH',
    'TEAM_PROJECT',
    'WEB_APPLICATION'
) COLLATE utf8mb4_unicode_ci DEFAULT NULL;

-- 데이터베이스 연결 정보 확인
-- URL: jdbc:mysql://192.168.50.54:3306/jobplatform?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true