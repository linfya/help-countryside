-- ============================================================
-- HelpCountryside 助农商城 数据库结构
-- MySQL 8.0 / utf8mb4
-- 说明：本脚本根据实体类（com.eng.domain.po）整理，用于快速搭建本地环境；
--      线上库的索引与字段长度以实际部署为准。
-- ============================================================

CREATE DATABASE IF NOT EXISTS `helpcountryside`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `helpcountryside`;

-- ------------------------------------------------------------
-- 用户
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `user_id`      bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '用户id',
    `phone`        varchar(20) NOT NULL COMMENT '手机号，登录账号',
    `user_name`    varchar(50) DEFAULT NULL COMMENT '用户名',
    `icon`         varchar(255) DEFAULT NULL COMMENT '头像地址',
    `balance`      int(11)     NOT NULL DEFAULT 0 COMMENT '余额，单位：分',
    `pay_password` varchar(64) DEFAULT NULL COMMENT '支付密码（加盐 MD5）',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- ------------------------------------------------------------
-- 用户扩展信息
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`
(
    `user_id`      bigint(20) NOT NULL COMMENT '用户id',
    `city`         varchar(50)   DEFAULT NULL COMMENT '城市',
    `introduce`    varchar(200)  DEFAULT NULL COMMENT '个人介绍',
    `birthday`     date          DEFAULT NULL COMMENT '生日',
    `gender`       int(11)       DEFAULT 1 COMMENT '性别',
    `items`        int(11)       DEFAULT 0 COMMENT '发布商品数',
    `works`        int(11)       DEFAULT 0 COMMENT '作品数',
    `fans`         int(11)       DEFAULT 0 COMMENT '粉丝数',
    `follow_human` int(11)       DEFAULT 0 COMMENT '关注数',
    PRIMARY KEY (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户扩展信息表';

-- ------------------------------------------------------------
-- 商品
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品id',
    `name`          varchar(100)   DEFAULT NULL COMMENT '商品名称',
    `price`         int(11)        DEFAULT NULL COMMENT '价格，单位：分',
    `stock`         int(11)        DEFAULT NULL COMMENT '库存',
    `type`          varchar(50)    DEFAULT NULL COMMENT '分类',
    `first_image`   varchar(255)   DEFAULT NULL COMMENT '首图',
    `image`         text COMMENT '图片，逗号分隔',
    `description`   text COMMENT '详情图片',
    `sold`          int(11)        DEFAULT 0 COMMENT '销量',
    `comment_count` int(11)        DEFAULT 0 COMMENT '评论数',
    `status`        int(11)        DEFAULT 0 COMMENT '状态：0 草稿 1 上架',
    `upload_type`   varchar(20)    DEFAULT NULL COMMENT '发布类型',
    `creater`       bigint(20)     DEFAULT NULL COMMENT '发布人',
    `create_time`   datetime       DEFAULT NULL COMMENT '创建时间',
    `update_time`   datetime       DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_creater` (`creater`),
    KEY `idx_status_update` (`status`, `update_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品表';

-- ------------------------------------------------------------
-- 商品评论
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `item_comments`;
CREATE TABLE `item_comments`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论id',
    `user_id`     bigint(20) NOT NULL COMMENT '评论人',
    `item_id`     bigint(20) NOT NULL COMMENT '商品id',
    `content`     varchar(500) DEFAULT NULL COMMENT '评论内容',
    `grade`       int(11)      DEFAULT 5 COMMENT '评分星级',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_item` (`item_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品评论表';

-- ------------------------------------------------------------
-- 购物车
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '购物车id',
    `user_id`     bigint(20) NOT NULL COMMENT '用户id',
    `item_id`     bigint(20) NOT NULL COMMENT '商品id',
    `num`         int(11)      DEFAULT 1 COMMENT '数量',
    `name`        varchar(100) DEFAULT NULL COMMENT '商品名称',
    `price`       int(11)      DEFAULT NULL COMMENT '价格，单位：分',
    `image`       varchar(255) DEFAULT NULL COMMENT '商品图片',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='购物车表';

-- ------------------------------------------------------------
-- 收货地址
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address`
(
    `address_id`   bigint(20) NOT NULL AUTO_INCREMENT COMMENT '地址id',
    `user_id`      bigint(20) NOT NULL COMMENT '用户id',
    `human_name`   varchar(50)  DEFAULT NULL COMMENT '收件人',
    `phone`        varchar(20)  DEFAULT NULL COMMENT '联系电话',
    `user_area`    varchar(100) DEFAULT NULL COMMENT '所在地区',
    `user_address` varchar(255) DEFAULT NULL COMMENT '详细地址',
    `status`       int(11)      DEFAULT 0 COMMENT '是否默认地址：1 是',
    PRIMARY KEY (`address_id`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='收货地址表';

-- ------------------------------------------------------------
-- 订单（order 是 MySQL 关键字，需反引号）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`
(
    `id`          bigint(20) NOT NULL COMMENT '订单id',
    `item_id`     bigint(20)     DEFAULT NULL COMMENT '商品id',
    `num`         int(11)        DEFAULT NULL COMMENT '购买数量',
    `name`        varchar(100)   DEFAULT NULL COMMENT '商品标题',
    `price`       int(11)        DEFAULT NULL COMMENT '价格，单位：分',
    `remark`      varchar(255)   DEFAULT NULL COMMENT '备注',
    `image`       varchar(255)   DEFAULT NULL COMMENT '商品图片',
    `create_time` datetime       DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime       DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_item` (`item_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单表';

-- ------------------------------------------------------------
-- 订单详情
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单详情id',
    `order_id`     bigint(20) NOT NULL COMMENT '订单id',
    `user_id`      bigint(20) NOT NULL COMMENT '买家id',
    `address_id`   bigint(20)   DEFAULT NULL COMMENT '收货地址id',
    `status`       int(11)      DEFAULT 1 COMMENT '1未付款 2已付款未发货 3已发货 4已确认收货 5已取消 6已评价',
    `create_time`  datetime     DEFAULT NULL COMMENT '创建时间',
    `pay_time`     datetime     DEFAULT NULL COMMENT '支付时间',
    `consign_time` datetime     DEFAULT NULL COMMENT '发货时间',
    `end_time`     datetime     DEFAULT NULL COMMENT '交易完成时间',
    `close_time`   datetime     DEFAULT NULL COMMENT '交易关闭时间',
    `comment_time` datetime     DEFAULT NULL COMMENT '评价时间',
    `update_time`  datetime     DEFAULT NULL COMMENT '更新时间',
    `is_delete`    int(11)      DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order` (`order_id`),
    KEY `idx_user_status` (`user_id`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单详情表';

-- ------------------------------------------------------------
-- 物流信息（与订单一对一）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `order_location`;
CREATE TABLE `order_location`
(
    `order_id`        bigint(20) NOT NULL COMMENT '订单id',
    `location_number` varchar(50)  DEFAULT NULL COMMENT '物流单号',
    `human_name`      varchar(50)  DEFAULT NULL COMMENT '收件人',
    `phone`           varchar(20)  DEFAULT NULL COMMENT '收件人手机号',
    `user_area`       varchar(100) DEFAULT NULL COMMENT '所在地区',
    `user_address`    varchar(255) DEFAULT NULL COMMENT '详细地址',
    `create_time`     datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`     datetime     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='物流信息表';

-- ------------------------------------------------------------
-- 支付单
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `pay_order`;
CREATE TABLE `pay_order`
(
    `id`               bigint(20) NOT NULL COMMENT '支付单id（雪花算法）',
    `order_no`         varchar(64)  DEFAULT NULL COMMENT '业务订单号',
    `pay_order_no`     bigint(20)   DEFAULT NULL COMMENT '支付单号',
    `user_id`          bigint(20)   DEFAULT NULL COMMENT '支付用户id',
    `amount`           int(11)      DEFAULT NULL COMMENT '支付金额，单位：分',
    `status`           int(11)      DEFAULT 0 COMMENT '0待提交 1待支付 2超时或取消 3支付成功',
    `expand_json`      varchar(500) DEFAULT NULL COMMENT '扩展字段',
    `pay_success_time` datetime     DEFAULT NULL COMMENT '支付成功时间',
    `pay_over_time`    datetime     DEFAULT NULL COMMENT '支付超时时间',
    `create_time`      datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`      datetime     DEFAULT NULL COMMENT '更新时间',
    `is_delete`        int(11)      DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_order_no` (`order_no`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='支付单表';

-- ------------------------------------------------------------
-- 社区：图文 / 视频动态
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '动态id',
    `user_id`     bigint(20) NOT NULL COMMENT '作者id',
    `title`       varchar(100) DEFAULT NULL COMMENT '标题',
    `images`      text COMMENT '图片/视频地址，逗号分隔',
    `first_image` varchar(255) DEFAULT NULL COMMENT '首图',
    `type`        varchar(50)  DEFAULT NULL COMMENT '分类',
    `upload_type` varchar(20)  DEFAULT NULL COMMENT '发布类型：image / video',
    `content`     text COMMENT '文字内容',
    `liked`       int(11)      DEFAULT 0 COMMENT '点赞数',
    `comments`    int(11)      DEFAULT 0 COMMENT '评论数',
    `status`      int(11)      DEFAULT 0 COMMENT '0 草稿 1 已发布',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_status_update` (`status`, `update_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='社区动态表';

-- ------------------------------------------------------------
-- 动态评论
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `blog_comments`;
CREATE TABLE `blog_comments`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论id',
    `user_id`     bigint(20) NOT NULL COMMENT '评论人',
    `blog_id`     bigint(20) NOT NULL COMMENT '动态id',
    `content`     varchar(500) DEFAULT NULL COMMENT '评论内容',
    `liked`       int(11)      DEFAULT 0 COMMENT '点赞数',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_blog` (`blog_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='动态评论表';
