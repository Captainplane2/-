-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: esports_zds
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
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `post_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `game_project`
--

DROP TABLE IF EXISTS `game_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_project` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `display_name` varchar(255) NOT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `sort_order` int DEFAULT NULL,
  `status` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_sdr8tar4ydn4a8ehw4djn95bo` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `game_project`
--

LOCK TABLES `game_project` WRITE;
/*!40000 ALTER TABLE `game_project` DISABLE KEYS */;
INSERT INTO `game_project` VALUES (1,'CS2',NULL,'CS2',1,0),(2,'英雄联盟',NULL,'LOL',2,0),(3,'王者荣耀',NULL,'WZRY',3,0);
/*!40000 ALTER TABLE `game_project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `match_application`
--

DROP TABLE IF EXISTS `match_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `match_application` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `applicant_team_id` bigint DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `match_id` bigint DEFAULT NULL,
  `status` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `match_application`
--

LOCK TABLES `match_application` WRITE;
/*!40000 ALTER TABLE `match_application` DISABLE KEYS */;
/*!40000 ALTER TABLE `match_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `match_info`
--

DROP TABLE IF EXISTS `match_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `match_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) DEFAULT NULL,
  `game_project` varchar(255) DEFAULT NULL,
  `guest_team_id` bigint DEFAULT NULL,
  `host_team_id` bigint DEFAULT NULL,
  `match_time` datetime(6) DEFAULT NULL,
  `mode` varchar(255) DEFAULT NULL,
  `requirement` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `type` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `match_info`
--

LOCK TABLES `match_info` WRITE;
/*!40000 ALTER TABLE `match_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `match_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text,
  `create_time` datetime(6) DEFAULT NULL,
  `reply` text,
  `reply_time` datetime(6) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notice`
--

DROP TABLE IF EXISTS `notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `author` varchar(255) DEFAULT NULL,
  `content` text,
  `create_time` datetime(6) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `views` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notice`
--

LOCK TABLES `notice` WRITE;
/*!40000 ALTER TABLE `notice` DISABLE KEYS */;
INSERT INTO `notice` VALUES (1,NULL,'测试中！~','2026-03-25 21:17:48.391324',1,'约战功能开发中',6);
/*!40000 ALTER TABLE `notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post`
--

DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comments` int DEFAULT NULL,
  `content` text NOT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `game_project` varchar(255) DEFAULT NULL,
  `likes` int DEFAULT NULL,
  `status` int DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `views` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post`
--

LOCK TABLES `post` WRITE;
/*!40000 ALTER TABLE `post` DISABLE KEYS */;
/*!40000 ALTER TABLE `post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `config_key` varchar(255) NOT NULL,
  `config_value` text,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` VALUES ('allowRegister','true',NULL),('autoMatch','false',NULL),('banner1','',NULL),('banner2','',NULL),('icp','湘ICP备2026003341号',NULL),('siteName','蘸豆，爽！',NULL);
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_banner`
--

DROP TABLE IF EXISTS `t_banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_banner` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) DEFAULT NULL,
  `image` varchar(255) NOT NULL,
  `sort_order` int DEFAULT NULL,
  `status` int DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_banner`
--

LOCK TABLES `t_banner` WRITE;
/*!40000 ALTER TABLE `t_banner` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_banner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_comment`
--

DROP TABLE IF EXISTS `t_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(1000) NOT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `nickname` varchar(255) DEFAULT NULL,
  `post_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_comment`
--

LOCK TABLES `t_comment` WRITE;
/*!40000 ALTER TABLE `t_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_footer_config`
--

DROP TABLE IF EXISTS `t_footer_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_footer_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `copyright` varchar(255) NOT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `friend_site_text` varchar(255) NOT NULL,
  `friend_site_url` varchar(255) NOT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_footer_config`
--

LOCK TABLES `t_footer_config` WRITE;
/*!40000 ALTER TABLE `t_footer_config` DISABLE KEYS */;
INSERT INTO `t_footer_config` VALUES (1,'© 2026 蘸豆，爽！ | 湘ICP备2026003341号','2026-03-28 23:50:02.113257',_binary '','友站','#','2026-03-28 23:50:02.113257');
/*!40000 ALTER TABLE `t_footer_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_match_room`
--

DROP TABLE IF EXISTS `t_match_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_match_room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `game_project` varchar(255) NOT NULL,
  `guest_team_id` bigint DEFAULT NULL,
  `guest_team_name` varchar(255) DEFAULT NULL,
  `host_id` bigint DEFAULT NULL,
  `host_team_id` bigint NOT NULL,
  `host_team_name` varchar(255) DEFAULT NULL,
  `host_university` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `match_time` datetime(6) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `type` int DEFAULT NULL,
  `host_leader_nickname` varchar(255) DEFAULT NULL,
  `host_team_logo` varchar(255) DEFAULT NULL,
  `guest_team_logo` varchar(255) DEFAULT NULL,
  `guest_university` varchar(255) DEFAULT NULL,
  `guest_id` bigint DEFAULT NULL,
  `guest_leader_nickname` varchar(255) DEFAULT NULL,
  `actual_end_time` datetime(6) DEFAULT NULL,
  `actual_start_time` datetime(6) DEFAULT NULL,
  `countdown_seconds` int DEFAULT NULL,
  `countdown_start_time` datetime(6) DEFAULT NULL,
  `finish_confirm_count` int DEFAULT NULL,
  `guest_team_ready` bit(1) DEFAULT NULL,
  `host_team_ready` bit(1) DEFAULT NULL,
  `match_status` enum('WAITING','READY','IN_PROGRESS','FINISHED','EXPIRED','CANCELLED') DEFAULT NULL,
  `guest_finish_confirm` bit(1) DEFAULT NULL,
  `host_finish_confirm` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_match_room`
--

LOCK TABLES `t_match_room` WRITE;
/*!40000 ALTER TABLE `t_match_room` DISABLE KEYS */;
INSERT INTO `t_match_room` VALUES (18,'2026-03-29 15:16:59.795869','威风不减当年\n直播间地址：https://live.bilibili.com/544853','CS2',7,'Fall cons',3,6,'湖大电竞天才少年','湖南大学','','2026-03-30 18:00:00.000000',1,'大虾高举双钳',0,NULL,NULL,'https://zds.captainplane.top/upload/5fa731ee-ccf3-461f-bce2-dd54ce73d25d.png','中南大学',4,'尼蔻',NULL,NULL,NULL,NULL,0,_binary '\0',_binary '\0','READY',_binary '\0',_binary '\0');
/*!40000 ALTER TABLE `t_match_room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_match_status_history`
--

DROP TABLE IF EXISTS `t_match_status_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_match_status_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `change_reason` varchar(255) DEFAULT NULL,
  `change_time` datetime(6) DEFAULT NULL,
  `changed_by` bigint DEFAULT NULL,
  `from_status` enum('WAITING','READY','IN_PROGRESS','FINISHED','EXPIRED','CANCELLED') DEFAULT NULL,
  `to_status` enum('WAITING','READY','IN_PROGRESS','FINISHED','EXPIRED','CANCELLED') DEFAULT NULL,
  `match_room_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK97he2pomyqniqhh3qrtemgeob` (`match_room_id`),
  CONSTRAINT `FK97he2pomyqniqhh3qrtemgeob` FOREIGN KEY (`match_room_id`) REFERENCES `t_match_room` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_match_status_history`
--

LOCK TABLES `t_match_status_history` WRITE;
/*!40000 ALTER TABLE `t_match_status_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_match_status_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_news`
--

DROP TABLE IF EXISTS `t_news`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_news` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `author_id` bigint DEFAULT NULL,
  `content` longtext,
  `cover_image` varchar(255) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `game_project` varchar(255) DEFAULT NULL,
  `summary` varchar(255) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `view_count` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_news`
--

LOCK TABLES `t_news` WRITE;
/*!40000 ALTER TABLE `t_news` DISABLE KEYS */;
INSERT INTO `t_news` VALUES (2,NULL,'<p>湖南大学电竞天才横空出事</p>','','2026-03-26 22:48:01.260148','CS2','三年之约已到，恭迎电竞天才回归','湖南大学电竞天才横空出事','2026-03-28 18:16:57.996048',8),(3,NULL,'<p><span style=\"color: rgb(0, 0, 0); font-size: 16px;\">貧しい人は車で遊び、裕福な人は時計で遊び、トップの富はやはり熊大快跑です。 2026年4月1日、誰もこの日に地球が『熊大快跑』というゲームに侵略されることを知りませんでした。 そして転生者である私は、急いで家や車をすべて売却し、ゲームに飛び込みました。その怒りで妻の柳如煙は身を残さずに家を出て行き、すぐに70歳を超える老人と結婚しました。そのことについて、私は必死に懇願するどころか、むしろ彼女に1万元の離婚金を支払いました。前世、『熊大快跑』の侵入後、私は天材地宝を手に入れただけで、柳如煙と彼女の弟に裏切られました。 今、転生して戻ってきた今、私は前の恥を血で洗い流す。チャージ画面を見ながら、深く息をつき、まるで全世界の空気を肺に吸い込むかのようだ。「チャージしたい、一億！」</span></p>','','2026-03-28 18:57:48.035513','综合','','貧しい人は車で遊び、裕福な人は時計で遊び、トップの富はやはり熊大快跑です','2026-03-28 18:58:08.216178',3);
/*!40000 ALTER TABLE `t_news` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_post`
--

DROP TABLE IF EXISTS `t_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(255) DEFAULT NULL,
  `comments` int DEFAULT NULL,
  `content` longtext,
  `create_time` datetime(6) DEFAULT NULL,
  `game_project` varchar(255) DEFAULT NULL,
  `likes` int DEFAULT NULL,
  `nickname` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `university` varchar(255) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `views` int DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_post`
--

LOCK TABLES `t_post` WRITE;
/*!40000 ALTER TABLE `t_post` DISABLE KEYS */;
INSERT INTO `t_post` VALUES (2,'赛事讨论',0,'<p>助我破鼎！</p>','2026-03-28 11:14:57.109434','CS2',0,'湖大电竞天才少年',0,'豆宗强者，恐怖如斯！','湖南大学','2026-03-28 11:14:57.109434',3,0,NULL),(3,'赛事讨论',0,'<p>2026 年 4 月 1 日，没人知道，地球会在这一天被一款名为《熊大快跑》的游戏所入侵。</p><p>而身为重生者的我，当即变卖了房产与车辆，一头扎进了这款游戏。我的妻子柳如烟为此暴怒，毅然离家出走，转头就嫁给了一位年过七旬的老人。对此，我非但没有苦苦挽留，反而痛快地给了她一万块离婚补偿金。前世，《熊大快跑》入侵世界后，我好不容易夺得天材地宝，却惨遭柳如烟与她弟弟的背叛。</p><p>如今，重生归来，我定要以血洗去前世之辱。我盯着充值界面，深吸一口气，仿佛要将世间所有空气都吸入肺中，沉声说道：</p><p>“我要充值，一个亿！”</p>','2026-03-28 18:06:07.955804','综合',0,'湖大电竞天才少年',0,'穷人玩车，富人玩表，顶富还得看熊大快跑','湖南大学','2026-03-28 19:03:01.193967',3,8,NULL);
/*!40000 ALTER TABLE `t_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team`
--

DROP TABLE IF EXISTS `team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `game_project` varchar(255) DEFAULT NULL,
  `leader_id` bigint DEFAULT NULL,
  `logo` varchar(255) DEFAULT NULL,
  `member_count` int DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `status` int DEFAULT NULL,
  `university` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team`
--

LOCK TABLES `team` WRITE;
/*!40000 ALTER TABLE `team` DISABLE KEYS */;
INSERT INTO `team` VALUES (6,'2026-03-26 22:08:23.979815','复活吧！我的爱人！','CS2',3,'https://zds.captainplane.top/upload/aba9eb6a-ea33-46c9-8052-50e44a44c093.jpg',4,'湖大电竞天才少年',0,'湖南大学'),(7,'2026-03-27 20:25:11.909737','We are Fallcons.','CS2',4,'https://zds.captainplane.top/upload/5fa731ee-ccf3-461f-bce2-dd54ce73d25d.png',5,'Fall cons',0,'中南大学');
/*!40000 ALTER TABLE `team` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_application`
--

DROP TABLE IF EXISTS `team_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_application` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `team_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_application`
--

LOCK TABLES `team_application` WRITE;
/*!40000 ALTER TABLE `team_application` DISABLE KEYS */;
INSERT INTO `team_application` VALUES (1,'2026-03-26 14:32:07.525773',NULL,1,1,2),(2,'2026-03-26 20:53:38.800608',NULL,1,1,2),(3,'2026-03-26 21:13:55.139890',NULL,1,1,2),(4,'2026-03-27 17:23:55.261131',NULL,1,6,2),(5,'2026-03-27 20:51:03.871060',NULL,1,6,7),(6,'2026-03-27 20:51:20.703195',NULL,1,7,5),(7,'2026-03-27 20:51:51.035191',NULL,1,7,6),(8,'2026-03-27 20:53:13.310565',NULL,1,7,8),(9,'2026-03-27 20:53:40.189314',NULL,1,7,9),(10,'2026-03-28 20:02:51.783489',NULL,1,6,10);
/*!40000 ALTER TABLE `team_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_member`
--

DROP TABLE IF EXISTS `team_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `join_time` datetime(6) DEFAULT NULL,
  `role` int DEFAULT NULL,
  `team_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `game_project` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_member`
--

LOCK TABLES `team_member` WRITE;
/*!40000 ALTER TABLE `team_member` DISABLE KEYS */;
INSERT INTO `team_member` VALUES (9,'2026-03-26 22:08:24.000677',1,6,3,'CS2'),(10,'2026-03-27 17:24:05.611542',0,6,2,'CS2'),(11,'2026-03-27 20:25:11.911262',1,7,4,'CS2'),(12,'2026-03-27 21:08:12.507043',0,6,7,'CS2'),(13,'2026-03-27 21:13:36.575537',0,7,9,'CS2'),(14,'2026-03-27 21:13:36.880797',0,7,8,'CS2'),(15,'2026-03-27 21:13:37.067650',0,7,6,'CS2'),(16,'2026-03-27 21:13:37.195767',0,7,5,'CS2'),(17,'2026-03-28 20:02:59.697478',0,6,10,'CS2');
/*!40000 ALTER TABLE `team_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avatar` varchar(255) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `nickname` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `real_name` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `student_id` varchar(255) DEFAULT NULL,
  `university` varchar(255) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `qq` varchar(255) DEFAULT NULL,
  `signature` varchar(255) DEFAULT NULL,
  `wechat` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_sb8bbouer5wak8vyiiy4pf2bx` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (2,'https://zds.captainplane.top/upload/9574af1d-20ab-4158-ae2f-061b273ac59c.png','2026-03-25 20:53:29.002913',NULL,'大茉子帝王','be518d92e2efa58b447a7f46d5b5ea9d',NULL,NULL,'ROLE_TEAM_MEMBER',1,NULL,'湖南大学','2026-03-28 14:42:38.640999','971203251@qq.com',NULL,NULL,NULL),(3,'https://zds.captainplane.top/upload/a0b34eb6-4d28-46c3-8a8e-81e52cc54f51.jpg','2026-03-25 21:23:17.665781',NULL,'湖大电竞天才少年','be518d92e2efa58b447a7f46d5b5ea9d',NULL,NULL,'ROLE_LEADER',0,NULL,'湖南大学','2026-03-28 14:41:26.648624','9712032511@qq.com',NULL,'蘸豆，爽！',NULL),(4,'https://zds.captainplane.top/upload/e24a239c-aa12-4a82-8432-9246f2375454.webp','2026-03-27 20:19:59.560406',NULL,'尼蔻','be518d92e2efa58b447a7f46d5b5ea9d',NULL,NULL,'ROLE_LEADER',1,NULL,'中南大学','2026-03-28 20:06:15.765025','9712032512@qq.com',NULL,NULL,NULL),(5,'https://zds.captainplane.top/upload/2f41d5dc-06a2-4a17-be6d-5d68eeb773f9.webp','2026-03-27 20:20:40.158595',NULL,'hide','be518d92e2efa58b447a7f46d5b5ea9d',NULL,NULL,'ROLE_TEAM_MEMBER',1,NULL,'中南大学','2026-03-28 20:06:15.525539','9712032513@qq.com',NULL,NULL,NULL),(6,'https://zds.captainplane.top/upload/2deea40a-3a12-42ab-bff5-73eddfa963b4.webp','2026-03-27 20:21:05.450252',NULL,'馃悏','be518d92e2efa58b447a7f46d5b5ea9d',NULL,NULL,'ROLE_TEAM_MEMBER',1,NULL,'中南大学','2026-03-28 20:06:15.230557','9712032514@qq.com',NULL,NULL,NULL),(7,'https://zds.captainplane.top/upload/b370ed11-1ba4-4df5-8628-c46751f9853f.jpg','2026-03-27 20:50:52.929217',NULL,'湖南大學絶兇の猛虎，最強电击伝説純真の丁一郎','be518d92e2efa58b447a7f46d5b5ea9d',NULL,NULL,'ROLE_TEAM_MEMBER',1,NULL,'湖南大学','2026-03-28 20:06:14.921971','9712032515@qq.com',NULL,NULL,NULL),(8,'https://zds.captainplane.top/upload/50970e13-01ff-435b-a021-a1dd8d942ae8.webp','2026-03-27 20:52:58.104701',NULL,'荆芥','be518d92e2efa58b447a7f46d5b5ea9d',NULL,NULL,'ROLE_TEAM_MEMBER',1,NULL,'中南大学','2026-03-28 20:06:14.590543','9712032516@qq.com',NULL,NULL,NULL),(9,'https://zds.captainplane.top/upload/e93e16d7-c5b1-4887-b002-b4b4d525b2f8.webp','2026-03-27 20:53:31.871088',NULL,'外星人','be518d92e2efa58b447a7f46d5b5ea9d',NULL,NULL,'ROLE_TEAM_MEMBER',1,NULL,'中南大学','2026-03-28 20:06:14.286738','9712032517@qq.com',NULL,NULL,NULL),(10,'https://zds.captainplane.top/upload/bd3121d6-4ee2-4396-9009-7dd3af57f188.jpg','2026-03-28 20:01:15.934003',NULL,'米利坚大统领罗师傅','be518d92e2efa58b447a7f46d5b5ea9d',NULL,NULL,'ROLE_TEAM_MEMBER',1,NULL,'湖南大学','2026-03-28 20:06:14.001663','9712032518@qq.com',NULL,NULL,NULL),(11,'https://zds.captainplane.top/upload/97c7237f-e599-40fe-87bf-6d2d0689a483.jpg','2026-03-28 20:05:21.028826',NULL,'管理员admin','be518d92e2efa58b447a7f46d5b5ea9d',NULL,NULL,'ROLE_ADMIN',1,NULL,'湖南大学','2026-03-28 20:06:05.636767','97120325120@qq.com',NULL,NULL,NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-29 19:17:17
