/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50505
Source Host           : localhost:3306
Source Database       : ssh

Target Server Type    : MYSQL
Target Server Version : 50505
File Encoding         : 65001

Date: 2019-08-19 13:54:51
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for xc_config
-- ----------------------------
DROP TABLE IF EXISTS `xc_config`;
CREATE TABLE `xc_config` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `group_id` int(11) DEFAULT NULL COMMENT '分组id',
  `title` varchar(30) DEFAULT '' COMMENT '标题',
  `host` varchar(255) DEFAULT '' COMMENT '主机',
  `port` int(11) DEFAULT 3306 COMMENT '端口',
  `username` varchar(30) DEFAULT '' COMMENT '用户名',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `version` int(11) DEFAULT 0 COMMENT '乐观锁',
  `create_time` datetime DEFAULT current_timestamp() COMMENT '创建日期',
  PRIMARY KEY (`id`)
) ;

-- ----------------------------
-- Table structure for xc_group
-- ----------------------------
DROP TABLE IF EXISTS `xc_group`;
CREATE TABLE `xc_group` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) DEFAULT '' COMMENT '分组名称',
  `version` int(11) DEFAULT 0 COMMENT '乐观锁',
  `create_time` datetime DEFAULT current_timestamp() COMMENT '创建日期',
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for xc_user
-- ----------------------------
DROP TABLE IF EXISTS `xc_user`;
CREATE TABLE `xc_user` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(30) DEFAULT '' COMMENT '用户名',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `nick_name` varchar(30) DEFAULT '' COMMENT '昵称',
  `head_img` varchar(150) DEFAULT '' COMMENT '头像',
  `status` int(11) DEFAULT 1 COMMENT '状态 0.冻结 1.正常',
  `deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除（0未删除1已删除）',
  `version` int(11) DEFAULT 0 COMMENT '乐观锁',
  `create_time` datetime DEFAULT current_timestamp() COMMENT '创建日期',
  PRIMARY KEY (`id`)
);