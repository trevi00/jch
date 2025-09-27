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
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` (`display_order`, `is_active`, `is_deleted`, `category_id`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`) VALUES (1,_binary '',_binary '\0',1,'2025-09-23 18:10:32.223838',NULL,'2025-09-23 18:10:32.223838','취업정보','취업 관련 정보 공유'),(2,_binary '',_binary '\0',2,'2025-09-23 18:10:32.231837',NULL,'2025-09-23 18:10:32.231837','면접정보','면접 관련 정보 공유'),(3,_binary '',_binary '\0',3,'2025-09-23 18:10:32.234839',NULL,'2025-09-23 18:10:32.234839','Q&A','질문과 답변'),(4,_binary '',_binary '\0',4,'2025-09-23 18:10:32.238350',NULL,'2025-09-23 18:10:32.238350','자유게시판','자유로운 소통 공간 (AI 이미지 생성 및 감정분석 지원)'),(5,_binary '',_binary '\0',5,'2025-09-23 18:10:32.241350',NULL,'2025-09-23 18:10:32.241350','기업게시판','기업 전용 게시판'),(6,_binary '',_binary '\0',6,'2025-09-23 18:10:32.244350',NULL,'2025-09-23 18:10:32.244350','공지','관리자 공지사항');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `skills`
--

LOCK TABLES `skills` WRITE;
/*!40000 ALTER TABLE `skills` DISABLE KEYS */;
INSERT INTO `skills` (`is_deleted`, `created_at`, `deleted_at`, `skill_id`, `updated_at`, `skill_name`, `category`) VALUES (_binary '\0','2025-09-24 10:02:59.116021',NULL,1,'2025-09-24 10:02:59.116021','Java','BACKEND'),(_binary '\0','2025-09-24 10:05:39.064614',NULL,2,'2025-09-24 10:05:39.064614','Spring Boot','BACKEND'),(_binary '\0','2025-09-24 10:08:01.876305',NULL,3,'2025-09-24 10:08:01.876305','React','FRONTEND'),(_binary '\0','2025-09-24 10:31:43.990913',NULL,4,'2025-09-24 10:31:43.990913','Python','BACKEND'),(_binary '\0','2025-09-24 10:32:33.356056',NULL,5,'2025-09-24 10:32:33.356056','TypeScript','FRONTEND'),(_binary '\0','2025-09-24 10:34:28.675484',NULL,6,'2025-09-24 10:34:28.675484','JavaScript','PROGRAMMING_LANGUAGE');
/*!40000 ALTER TABLE `skills` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-24 15:00:31
