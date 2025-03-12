/*
Navicat MySQL Data Transfer

Source Server         : mysqld
Source Server Version : 50720
Source Host           : localhost:3306
Source Database       : course_selection

Target Server Type    : MYSQL
Target Server Version : 50720
File Encoding         : 65001

Date: 2025-03-12 11:31:16
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `courses`
-- ----------------------------
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses` (
  `course_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `course_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `teacher_id` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `credit` int(11) NOT NULL,
  `max_students` int(11) NOT NULL,
  `current_students` int(11) DEFAULT '0',
  PRIMARY KEY (`course_id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `courses_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `teachers` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of courses
-- ----------------------------
INSERT INTO `courses` VALUES ('C001', 'C语言程序设计', 'T001', '4', '50', '3');
INSERT INTO `courses` VALUES ('C002', '数据库课程设计', 'T002', '3', '40', '3');
INSERT INTO `courses` VALUES ('C003', 'Web服务器端开发', 'T003', '4', '45', '1');
INSERT INTO `courses` VALUES ('C004', 'C++程序设计', 'T004', '4', '50', '2');
INSERT INTO `courses` VALUES ('C005', 'Java程序设计', 'T005', '4', '45', '2');
INSERT INTO `courses` VALUES ('C006', 'Vue前端开发', 'T006', '3', '40', '2');
INSERT INTO `courses` VALUES ('C007', 'UniApp移动开发', 'T001', '3', '35', '2');
INSERT INTO `courses` VALUES ('C008', '计算机导论', 'T002', '2', '60', '2');
INSERT INTO `courses` VALUES ('C009', '算法分析与设计', 'T003', '4', '40', '2');
INSERT INTO `courses` VALUES ('C010', '数据结构', 'T004', '4', '50', '1');
INSERT INTO `courses` VALUES ('C011', '操作系统', 'T005', '4', '45', '1');
INSERT INTO `courses` VALUES ('C012', '计算机网络', 'T006', '4', '45', '2');

-- ----------------------------
-- Table structure for `course_selection`
-- ----------------------------
DROP TABLE IF EXISTS `course_selection`;
CREATE TABLE `course_selection` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `student_id` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `course_id` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `score` decimal(5,2) DEFAULT NULL,
  `select_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_selection` (`student_id`,`course_id`),
  KEY `course_id` (`course_id`),
  CONSTRAINT `course_selection_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`),
  CONSTRAINT `course_selection_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of course_selection
-- ----------------------------
INSERT INTO `course_selection` VALUES ('1', 'S001', 'C001', '85.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('2', 'S001', 'C002', '78.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('4', 'S002', 'C001', '76.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('5', 'S002', 'C004', '88.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('6', 'S002', 'C005', '91.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('7', 'S003', 'C002', '82.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('8', 'S003', 'C006', '95.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('9', 'S003', 'C007', '89.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('10', 'S004', 'C008', '87.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('11', 'S004', 'C009', '76.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('12', 'S005', 'C010', '92.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('13', 'S005', 'C011', '85.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('14', 'S006', 'C012', '88.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('15', 'S006', 'C001', '79.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('16', 'S007', 'C002', '94.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('17', 'S007', 'C003', '82.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('18', 'S008', 'C004', '87.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('19', 'S008', 'C005', '91.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('20', 'S009', 'C006', '85.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('21', 'S009', 'C007', '78.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('22', 'S010', 'C008', '93.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('23', 'S010', 'C009', '86.00', '2025-03-12 11:16:43');
INSERT INTO `course_selection` VALUES ('43', 'S001', 'C012', null, '2025-03-12 11:27:05');

-- ----------------------------
-- Table structure for `students`
-- ----------------------------
DROP TABLE IF EXISTS `students`;
CREATE TABLE `students` (
  `student_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `class_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `major` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`student_id`),
  CONSTRAINT `students_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of students
-- ----------------------------
INSERT INTO `students` VALUES ('S001', '张三', '计算机2101', '计算机科学与技术');
INSERT INTO `students` VALUES ('S002', '李四', '计算机2101', '计算机科学与技术');
INSERT INTO `students` VALUES ('S003', '王五', '计算机2102', '计算机科学与技术');
INSERT INTO `students` VALUES ('S004', '赵六', '计算机2102', '计算机科学与技术');
INSERT INTO `students` VALUES ('S005', '孙七', '软件2101', '软件工程');
INSERT INTO `students` VALUES ('S006', '周八', '软件2101', '软件工程');
INSERT INTO `students` VALUES ('S007', '吴九', '软件2102', '软件工程');
INSERT INTO `students` VALUES ('S008', '郑十', '软件2102', '软件工程');
INSERT INTO `students` VALUES ('S009', '刘一', '网络2101', '网络工程');
INSERT INTO `students` VALUES ('S010', '陈二', '网络2101', '网络工程');

-- ----------------------------
-- Table structure for `system_logs`
-- ----------------------------
DROP TABLE IF EXISTS `system_logs`;
CREATE TABLE `system_logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `log_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `operation` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_id` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `details` text COLLATE utf8mb4_unicode_ci,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of system_logs
-- ----------------------------

-- ----------------------------
-- Table structure for `teachers`
-- ----------------------------
DROP TABLE IF EXISTS `teachers`;
CREATE TABLE `teachers` (
  `teacher_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `department` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`teacher_id`),
  CONSTRAINT `teachers_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of teachers
-- ----------------------------
INSERT INTO `teachers` VALUES ('T001', '韩瑶', '计算机系');
INSERT INTO `teachers` VALUES ('T002', '颜烨', '计算机系');
INSERT INTO `teachers` VALUES ('T003', '季松华', '计算机系');
INSERT INTO `teachers` VALUES ('T004', '钟茂盛', '计算机系');
INSERT INTO `teachers` VALUES ('T005', '冉苗', '计算机系');
INSERT INTO `teachers` VALUES ('T006', '李璐加', '计算机系');

-- ----------------------------
-- Table structure for `users`
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('admin','teacher','student') COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES ('admin', 'admin', 'admin123', 'admin', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('S001', 'S001', 'S001', 'student', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('S002', 'S002', 'S002', 'student', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('S003', 'S003', 'S003', 'student', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('S004', 'S004', 'S004', 'student', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('S005', 'S005', 'S005', 'student', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('S006', 'S006', 'S006', 'student', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('S007', 'S007', 'S007', 'student', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('S008', 'S008', 'S008', 'student', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('S009', 'S009', 'S009', 'student', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('S010', 'S010', 'S010', 'student', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('T001', 'T001', 'T001', 'teacher', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('T002', 'T002', 'T002', 'teacher', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('T003', 'T003', 'T003', 'teacher', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('T004', 'T004', 'T004', 'teacher', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('T005', 'T005', 'T005', 'teacher', '2025-03-12 11:16:43');
INSERT INTO `users` VALUES ('T006', 'T006', 'T006', 'teacher', '2025-03-12 11:16:43');

-- ----------------------------
-- Procedure structure for `sp_drop_course`
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_drop_course`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_drop_course`(
    IN p_student_id VARCHAR(20),
    IN p_course_id VARCHAR(20),
    OUT p_result INT
)
BEGIN
    DECLARE v_current_count INT;
    DECLARE v_exists INT;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = -2;  -- 数据库错误
    END;
    
    START TRANSACTION;
    
    -- 检查是否存在这条选课记录
    SELECT COUNT(*) INTO v_exists
    FROM course_selection 
    WHERE student_id = p_student_id 
    AND course_id = p_course_id;
    
    IF v_exists = 0 THEN
        SET p_result = -1;  -- 没有找到选课记录
        ROLLBACK;
    ELSE
        -- 获取当前课程人数
        SELECT current_students INTO v_current_count
        FROM courses 
        WHERE course_id = p_course_id
        FOR UPDATE;  -- 锁定行防止并发更新
        
        IF v_current_count > 0 THEN
            -- 删除选课记录
            DELETE FROM course_selection 
            WHERE student_id = p_student_id 
            AND course_id = p_course_id;
            
            -- 更新课程人数
            UPDATE courses 
            SET current_students = current_students - 1
            WHERE course_id = p_course_id
            AND current_students > 0;  -- 确保人数不会变成负数
            
            SET p_result = 1;  -- 退课成功
            COMMIT;
        ELSE
            SET p_result = -3;  -- 课程人数已经是0
            ROLLBACK;
        END IF;
    END IF;
END
;;
DELIMITER ;

-- ----------------------------
-- Procedure structure for `sp_select_course`
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_select_course`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_select_course`(
    IN p_student_id VARCHAR(20),
    IN p_course_id VARCHAR(20),
    OUT p_result INT
)
BEGIN
    DECLARE v_current_count INT;
    DECLARE v_max_students INT;
    DECLARE v_existing_selection INT;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = -3;
    END;
    
    START TRANSACTION;
    
    SELECT COUNT(*) INTO v_existing_selection
    FROM course_selection
    WHERE student_id = p_student_id AND course_id = p_course_id;
    
    IF v_existing_selection > 0 THEN
        SET p_result = -2;
    ELSE
        SELECT current_students, max_students 
        INTO v_current_count, v_max_students
        FROM courses 
        WHERE course_id = p_course_id
        FOR UPDATE;
        
        IF v_current_count >= v_max_students THEN
            SET p_result = -1;
        ELSE
            INSERT INTO course_selection(student_id, course_id)
            VALUES(p_student_id, p_course_id);
            
            UPDATE courses 
            SET current_students = current_students + 1
            WHERE course_id = p_course_id;
            
            SET p_result = 1;
        END IF;
    END IF;
    
    COMMIT;
END
;;
DELIMITER ;
DROP TRIGGER IF EXISTS `tr_score_check`;
DELIMITER ;;
CREATE TRIGGER `tr_score_check` BEFORE UPDATE ON `course_selection` FOR EACH ROW BEGIN
    IF NEW.score < 0 OR NEW.score > 100 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '成绩必须在0-100之间';
    END IF;
END
;;
DELIMITER ;
