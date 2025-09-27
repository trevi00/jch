-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: jobplatform
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `career_histories`
--

DROP TABLE IF EXISTS `career_histories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `career_histories` (
  `end_date` date DEFAULT NULL,
  `is_current` bit(1) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `start_date` date NOT NULL,
  `career_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `achievements` text COLLATE utf8mb4_unicode_ci,
  `company_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `department` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `position` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `employment_type` enum('CONTRACT','FREELANCE','FULL_TIME','HYBRID','INTERNSHIP','OTHER','PART_TIME','PROJECT_BASED','REMOTE','VOLUNTEER') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`career_id`),
  KEY `FKpsp8evft90mxilsnx5dy48jp9` (`user_id`),
  CONSTRAINT `FKpsp8evft90mxilsnx5dy48jp9` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `display_order` int NOT NULL,
  `is_active` bit(1) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `UKt8o6pivur7nn124jehx7cygw5` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `certificate_requests`
--

DROP TABLE IF EXISTS `certificate_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `certificate_requests` (
  `createdAt` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `processedAt` datetime(6) DEFAULT NULL,
  `updatedAt` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `adminNotes` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `purpose` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `certificateType` enum('ATTENDANCE_CERTIFICATE','COMPLETION_CERTIFICATE','COURSE_COMPLETION_CERTIFICATE','ENROLLMENT_CERTIFICATE','TRANSCRIPT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('APPROVED','COMPLETED','PENDING','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK75q1p5de82yf36b08sh6vcqm9` (`user_id`),
  CONSTRAINT `FK75q1p5de82yf36b08sh6vcqm9` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `certifications`
--

DROP TABLE IF EXISTS `certifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `certifications` (
  `expiration_date` date DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `is_lifetime_valid` bit(1) DEFAULT NULL,
  `issue_date` date DEFAULT NULL,
  `certification_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `certificate_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `certification_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `certification_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `issuing_organization` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `related_skills` text COLLATE utf8mb4_unicode_ci,
  `score` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `verification_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ACTIVE','EXPIRED','PENDING','REVOKED') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`certification_id`),
  KEY `FKbfsgbyyudnkdkf6julrlp6od0` (`user_id`),
  CONSTRAINT `FKbfsgbyyudnkdkf6julrlp6od0` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comments`
--

DROP TABLE IF EXISTS `comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comments` (
  `is_deleted` bit(1) NOT NULL,
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `like_count` bigint NOT NULL,
  `parent_comment_id` bigint DEFAULT NULL,
  `post_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`comment_id`),
  KEY `FK8omq0tc18jd43bu5tjh6jvraq` (`user_id`),
  KEY `FK7h839m3lkvhbyv3bcdv7sm4fj` (`parent_comment_id`),
  KEY `FKh4c7lvsc298whoyd4w9ta25cr` (`post_id`),
  CONSTRAINT `FK7h839m3lkvhbyv3bcdv7sm4fj` FOREIGN KEY (`parent_comment_id`) REFERENCES `comments` (`comment_id`),
  CONSTRAINT `FK8omq0tc18jd43bu5tjh6jvraq` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKh4c7lvsc298whoyd4w9ta25cr` FOREIGN KEY (`post_id`) REFERENCES `posts` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `companies`
--

DROP TABLE IF EXISTS `companies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `companies` (
  `employee_count` int DEFAULT NULL,
  `establishment_date` date DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `is_verified` bit(1) NOT NULL,
  `revenue` decimal(15,2) DEFAULT NULL,
  `company_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `business_number` varchar(12) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `industry` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `company_email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `company_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website_url` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `logo_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`company_id`),
  UNIQUE KEY `UK5xg6ed73n32iai9psir68pia9` (`user_id`),
  UNIQUE KEY `UKrnj5dhq8dihsu7afygc2oxa79` (`business_number`),
  CONSTRAINT `FK9l5d0fem75e59uwf9upwuf9du` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `education_infos`
--

DROP TABLE IF EXISTS `education_infos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `education_infos` (
  `end_date` date DEFAULT NULL,
  `is_current_education` bit(1) DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `start_date` date DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `education_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `achievements` text COLLATE utf8mb4_unicode_ci,
  `degree` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `grade_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `grade_value` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `institution_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `major` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `education_type` enum('BOOTCAMP','CERTIFICATE_COURSE','GRADUATE_SCHOOL','HIGH_SCHOOL','ONLINE_COURSE','OTHER','UNIVERSITY','VOCATIONAL_SCHOOL') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`education_id`),
  KEY `FKfxssmwdgevij1vwkmcpiraxd7` (`user_id`),
  CONSTRAINT `FKfxssmwdgevij1vwkmcpiraxd7` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `email_history`
--

DROP TABLE IF EXISTS `email_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_history` (
  `is_deleted` bit(1) NOT NULL,
  `was_translated` bit(1) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `source_language` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_language` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `document_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `subject` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `original_content` text COLLATE utf8mb4_unicode_ci,
  `recipient_email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sender_email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sender_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sendgrid_message_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `translated_content` text COLLATE utf8mb4_unicode_ci,
  `status` enum('BOUNCED','CLICKED','DELIVERED','FAILED','OPENED','PENDING','SENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `interview_questions`
--

DROP TABLE IF EXISTS `interview_questions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interview_questions` (
  `is_deleted` bit(1) NOT NULL,
  `question_order` int DEFAULT NULL,
  `score` decimal(38,2) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `interview_id` bigint NOT NULL,
  `question_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `answer` text COLLATE utf8mb4_unicode_ci,
  `improvements` json DEFAULT NULL,
  `question_text` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `question_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `strengths` json DEFAULT NULL,
  `suggestion` text COLLATE utf8mb4_unicode_ci,
  `temp_question_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`question_id`),
  KEY `FK3dw8ec8nqxk4sv9gup5pk9sqs` (`interview_id`),
  CONSTRAINT `FK3dw8ec8nqxk4sv9gup5pk9sqs` FOREIGN KEY (`interview_id`) REFERENCES `interviews` (`interview_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `interviews`
--

DROP TABLE IF EXISTS `interviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interviews` (
  `answered_questions` int DEFAULT NULL,
  `duration_minutes` int DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `overall_score` decimal(38,2) DEFAULT NULL,
  `total_questions` int DEFAULT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `interview_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `experience_level` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `interview_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `job_role` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('CANCELLED','COMPLETED','IN_PROGRESS') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`interview_id`),
  KEY `FK5e4rxgf88qk96qk90lgngxrd0` (`user_id`),
  CONSTRAINT `FK5e4rxgf88qk96qk90lgngxrd0` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job_applications`
--

DROP TABLE IF EXISTS `job_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_applications` (
  `is_deleted` bit(1) NOT NULL,
  `application_id` bigint NOT NULL AUTO_INCREMENT,
  `applied_at` datetime(6) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `final_decision_at` datetime(6) DEFAULT NULL,
  `interview_scheduled_at` datetime(6) DEFAULT NULL,
  `job_id` bigint NOT NULL,
  `reviewed_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `rejection_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `interviewer_notes` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `portfolio_urls` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cover_letter` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `resume_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('DOCUMENT_PASSED','HIRED','INTERVIEW_PASSED','INTERVIEW_SCHEDULED','REJECTED','REVIEWED','SUBMITTED','WITHDRAWN') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`application_id`),
  UNIQUE KEY `UKs8gqynyonkhfrg6afij2tp4jt` (`user_id`,`job_id`),
  KEY `FKnhwwf2t406ujtc15jnbq0ugd6` (`job_id`),
  CONSTRAINT `FKnhwwf2t406ujtc15jnbq0ugd6` FOREIGN KEY (`job_id`) REFERENCES `job_postings` (`job_id`),
  CONSTRAINT `FKqs2guhg7p83917vto86imuthy` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job_postings`
--

DROP TABLE IF EXISTS `job_postings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_postings` (
  `deadline_date` date DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `is_remote_possible` bit(1) DEFAULT NULL,
  `salary_max` int DEFAULT NULL,
  `salary_min` int DEFAULT NULL,
  `salary_negotiable` bit(1) DEFAULT NULL,
  `application_count` bigint NOT NULL,
  `company_user_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `job_id` bigint NOT NULL AUTO_INCREMENT,
  `published_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `view_count` bigint NOT NULL,
  `benefits` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `required_skills` varchar(1500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `qualifications` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(3000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `company_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contact_email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `department` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `field` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `location` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `working_hours` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `experience_level` enum('ANY','DIRECTOR','ENTRY_LEVEL','EXPERT','JUNIOR','MANAGER','MID_LEVEL','SENIOR') COLLATE utf8mb4_unicode_ci NOT NULL,
  `job_type` enum('CONTRACT','FREELANCE','FULL_TIME','HYBRID','INTERNSHIP','PART_TIME','PROJECT_BASED','REMOTE','TEMPORARY') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('CLOSED','DRAFT','EXPIRED','PUBLISHED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`job_id`),
  KEY `FKotlrjfc1fb8g08akohs0cjkfc` (`company_user_id`),
  CONSTRAINT `FKotlrjfc1fb8g08akohs0cjkfc` FOREIGN KEY (`company_user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `portfolios`
--

DROP TABLE IF EXISTS `portfolios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `portfolios` (
  `display_order` int DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `is_featured` bit(1) NOT NULL,
  `start_date` date DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `portfolio_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `portfolio_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `technologies_used` json DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `portfolio_type` enum('API_BACKEND','COMPETITION','DATA_ANALYSIS','DESIGN','DESKTOP_APPLICATION','HACKATHON','MACHINE_LEARNING','MOBILE_APPLICATION','OPEN_SOURCE','OTHER','PERSONAL_PROJECT','RESEARCH','TEAM_PROJECT','WEB_APPLICATION') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`portfolio_id`),
  KEY `FK9xt36kgm9cxsf79r2me0d9f6u` (`user_id`),
  CONSTRAINT `FK9xt36kgm9cxsf79r2me0d9f6u` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `posts`
--

DROP TABLE IF EXISTS `posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `posts` (
  `is_deleted` bit(1) NOT NULL,
  `is_notice` bit(1) NOT NULL,
  `is_pinned` bit(1) NOT NULL,
  `sentiment_score` double DEFAULT NULL,
  `category_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `like_count` bigint NOT NULL,
  `post_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `view_count` bigint NOT NULL,
  `sentiment_label` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `image_url` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`post_id`),
  KEY `FK5lidm6cqbc7u4xhqpxm898qme` (`user_id`),
  KEY `FKijnwr3brs8vaosl80jg9rp7uc` (`category_id`),
  CONSTRAINT `FK5lidm6cqbc7u4xhqpxm898qme` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKijnwr3brs8vaosl80jg9rp7uc` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `skills`
--

DROP TABLE IF EXISTS `skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `skills` (
  `is_deleted` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `skill_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `skill_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` enum('AI_ML','BACKEND','CERTIFICATION','CLOUD','COMMUNICATION','DATABASE','DATA_SCIENCE','DESIGN','DEVOPS','FRAMEWORK','FRONTEND','LANGUAGE','MOBILE','OTHER','PROGRAMMING_LANGUAGE','PROJECT_MANAGEMENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`skill_id`),
  UNIQUE KEY `UKqdkr64bxreqdbn6b4k9vumvp3` (`skill_name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `support_faqs`
--

DROP TABLE IF EXISTS `support_faqs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `support_faqs` (
  `display_order` int NOT NULL,
  `helpful_count` int NOT NULL,
  `is_active` bit(1) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `view_count` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `faq_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `question` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `answer` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`faq_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `support_messages`
--

DROP TABLE IF EXISTS `support_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `support_messages` (
  `is_deleted` bit(1) NOT NULL,
  `is_from_admin` bit(1) NOT NULL,
  `is_internal_note` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `message_id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint NOT NULL,
  `ticket_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `attachment_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message_content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`message_id`),
  KEY `FKt3x5f2qc7d89medr0xxslx982` (`sender_id`),
  KEY `FKejv4umpsfsqv4amvnjrmni3xi` (`ticket_id`),
  CONSTRAINT `FKejv4umpsfsqv4amvnjrmni3xi` FOREIGN KEY (`ticket_id`) REFERENCES `support_tickets` (`ticket_id`),
  CONSTRAINT `FKt3x5f2qc7d89medr0xxslx982` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `support_tickets`
--

DROP TABLE IF EXISTS `support_tickets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `support_tickets` (
  `is_deleted` bit(1) NOT NULL,
  `satisfaction_rating` int DEFAULT NULL,
  `assigned_admin_id` bigint DEFAULT NULL,
  `closed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `first_response_at` datetime(6) DEFAULT NULL,
  `last_activity_at` datetime(6) NOT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `ticket_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `satisfaction_feedback` text COLLATE utf8mb4_unicode_ci,
  `priority` enum('HIGH','LOW','MEDIUM','URGENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('CLOSED','IN_PROGRESS','OPEN','RESOLVED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`ticket_id`),
  KEY `FKobe7isl26ikcdry1tgxblb6t5` (`assigned_admin_id`),
  KEY `FK4reg1h2465c00bg6dmqlv7ujv` (`user_id`),
  CONSTRAINT `FK4reg1h2465c00bg6dmqlv7ujv` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKobe7isl26ikcdry1tgxblb6t5` FOREIGN KEY (`assigned_admin_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `system_metrics`
--

DROP TABLE IF EXISTS `system_metrics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_metrics` (
  `metric_date` date NOT NULL,
  `createdAt` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `metric_value` bigint NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `metric_type` enum('API_CALLS','DAILY_ACTIVE_USERS','ERROR_COUNT','LOGIN_COUNT','SIGNUP_COUNT') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7224gkansae4qw4lol6ad3a5x` (`metric_date`,`metric_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `translation_requests`
--

DROP TABLE IF EXISTS `translation_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `translation_requests` (
  `character_count` int DEFAULT NULL,
  `cost` double DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `processed_at` datetime(6) DEFAULT NULL,
  `translation_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `source_language` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_language` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `document_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_message` text COLLATE utf8mb4_unicode_ci,
  `source_text` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `translated_text` text COLLATE utf8mb4_unicode_ci,
  `status` enum('COMPLETED','FAILED','IN_PROGRESS','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`translation_id`),
  KEY `FK2r20idhullg26rvqaro32776q` (`user_id`),
  CONSTRAINT `FK2r20idhullg26rvqaro32776q` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_certifications`
--

DROP TABLE IF EXISTS `user_certifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_certifications` (
  `expiry_date` date DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `issue_date` date DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `description` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `certification_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `credential_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `credential_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `issuing_organization` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn2bw638vse1c6o3w1uoti7sdk` (`user_id`),
  CONSTRAINT `FKn2bw638vse1c6o3w1uoti7sdk` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_education`
--

DROP TABLE IF EXISTS `user_education`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_education` (
  `gpa` decimal(3,2) DEFAULT NULL,
  `graduation_date` date DEFAULT NULL,
  `graduation_year` int DEFAULT NULL,
  `is_current` bit(1) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `max_gpa` decimal(3,2) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `major` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `school_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `education_level` enum('ASSOCIATE','BACHELOR','BOOTCAMP','DOCTORATE','HIGH_SCHOOL','MASTER','OTHER','VOCATIONAL') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK925l7dikt8tmrc89n1kbp0ht9` (`user_id`),
  CONSTRAINT `FK925l7dikt8tmrc89n1kbp0ht9` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_profiles`
--

DROP TABLE IF EXISTS `user_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_profiles` (
  `age` int DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `profile_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `bio` text COLLATE utf8mb4_unicode_ci,
  `desired_job` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `first_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `profile_image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gender` enum('FEMALE','MALE','OTHER') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`profile_id`),
  UNIQUE KEY `UKe5h89rk3ijvdmaiig4srogdc6` (`user_id`),
  CONSTRAINT `FKjcad5nfve11khsnpwj1mv8frj` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_skills`
--

DROP TABLE IF EXISTS `user_skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_skills` (
  `is_deleted` bit(1) NOT NULL,
  `years_of_experience` int DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `skill_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `user_skill_id` bigint NOT NULL AUTO_INCREMENT,
  `proficiency_level` enum('ADVANCED','BEGINNER','EXPERT','INTERMEDIATE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`user_skill_id`),
  KEY `FKh223y61gwijpgqt6nlsuti07g` (`skill_id`),
  KEY `FKro13if9r7fwkr5115715127ai` (`user_id`),
  CONSTRAINT `FKh223y61gwijpgqt6nlsuti07g` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`skill_id`),
  CONSTRAINT `FKro13if9r7fwkr5115715127ai` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `company_email_verified` bit(1) NOT NULL,
  `email_verified` bit(1) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `admin_converted_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `oauth_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `employment_status` enum('EMPLOYED','JOB_SEEKING','PREPARING','STUDENT') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `oauth_provider` enum('GOOGLE','NATIVE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_type` enum('ADMIN','COMPANY','GENERAL') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'jobplatform'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-24 14:56:35
