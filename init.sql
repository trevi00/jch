-- 초기 데이터베이스 설정
CREATE DATABASE IF NOT EXISTS jbd_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jbd_db;

-- 권한 설정
GRANT ALL PRIVILEGES ON jbd_db.* TO 'jbd_user'@'%';
FLUSH PRIVILEGES;