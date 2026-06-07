-- 科普星球数据库初始化脚本
-- 执行前请先创建数据库: CREATE DATABASE kepuxingqiu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE kepuxingqiu;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录名',
    `password`    VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
    `nickname`    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '昵称',
    `avatar_url`  VARCHAR(255) DEFAULT '' COMMENT '头像URL',
    `age_group`   TINYINT      NOT NULL DEFAULT 2 COMMENT '年龄段 1=幼儿园 2=小学 3=初中',
    `level`       INT          NOT NULL DEFAULT 1 COMMENT '等级',
    `experience`  INT          NOT NULL DEFAULT 0 COMMENT '累计经验值',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 游戏分类表
CREATE TABLE IF NOT EXISTS `game_category` (
    `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    `description` VARCHAR(200) DEFAULT '' COMMENT '分类描述',
    `icon`        VARCHAR(255) DEFAULT '' COMMENT '图标URL',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序权重',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏分类表';

-- 游戏内容/关卡表
CREATE TABLE IF NOT EXISTS `game_content` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '关卡ID',
    `category_id`       INT          NOT NULL COMMENT '所属分类ID',
    `title`             VARCHAR(100) NOT NULL COMMENT '关卡标题',
    `difficulty`        TINYINT      NOT NULL DEFAULT 1 COMMENT '难度 1=简单 2=中等 3=困难',
    `content_data`      JSON         COMMENT '游戏配置数据(星座点位/拼图信息等)',
    `knowledge_point`   TEXT         COMMENT '关联知识点讲解',
    `experience_reward` INT          NOT NULL DEFAULT 50 COMMENT '通关经验奖励',
    `sort_order`        INT          NOT NULL DEFAULT 0 COMMENT '排序权重',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏内容关卡表';

-- 用户游戏进度表
CREATE TABLE IF NOT EXISTS `user_game_progress` (
    `id`              BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT   NOT NULL COMMENT '用户ID',
    `game_content_id` BIGINT   NOT NULL COMMENT '关卡ID',
    `status`          TINYINT  NOT NULL DEFAULT 0 COMMENT '0=未开始 1=进行中 2=已完成',
    `score`           INT      NOT NULL DEFAULT 0 COMMENT '得分',
    `completed_at`    DATETIME DEFAULT NULL COMMENT '完成时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_game` (`user_id`, `game_content_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户游戏进度表';

-- 科普图鉴表
CREATE TABLE IF NOT EXISTS `encyclopedia` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '图鉴ID',
    `category`         VARCHAR(20)  NOT NULL COMMENT '分类: astronomy/history/insect',
    `name`             VARCHAR(100) NOT NULL COMMENT '名称',
    `description`      TEXT         COMMENT '详细描述',
    `image_url`        VARCHAR(255) DEFAULT '' COMMENT '图片URL',
    `unlock_condition` VARCHAR(200) DEFAULT '' COMMENT '解锁条件描述',
    `related_game_id`  BIGINT       DEFAULT NULL COMMENT '关联的关卡ID',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科普图鉴表';

-- 用户解锁图鉴表
CREATE TABLE IF NOT EXISTS `user_encyclopedia` (
    `id`               BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT   NOT NULL COMMENT '用户ID',
    `encyclopedia_id`  BIGINT   NOT NULL COMMENT '图鉴ID',
    `unlocked_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_enc` (`user_id`, `encyclopedia_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户解锁图鉴表';

-- 成就定义表
CREATE TABLE IF NOT EXISTS `achievement` (
    `id`                INT          NOT NULL AUTO_INCREMENT COMMENT '成就ID',
    `name`              VARCHAR(50)  NOT NULL COMMENT '成就名称',
    `description`       VARCHAR(200) NOT NULL DEFAULT '' COMMENT '成就描述',
    `icon`              VARCHAR(255) DEFAULT '' COMMENT '徽章图标URL',
    `condition_type`    VARCHAR(30)  NOT NULL COMMENT '条件类型: complete_game/unlock_enc/reach_level',
    `condition_value`   INT          NOT NULL DEFAULT 1 COMMENT '条件数值阈值',
    `experience_reward` INT          NOT NULL DEFAULT 100 COMMENT '达成后经验奖励',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就定义表';

-- 用户成就进度表
CREATE TABLE IF NOT EXISTS `user_achievement` (
    `id`               BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT   NOT NULL COMMENT '用户ID',
    `achievement_id`   INT      NOT NULL COMMENT '成就ID',
    `current_progress` INT      NOT NULL DEFAULT 0 COMMENT '当前进度值',
    `is_completed`     TINYINT  NOT NULL DEFAULT 0 COMMENT '是否完成 0=未完成 1=已完成',
    `completed_at`     DATETIME DEFAULT NULL COMMENT '完成时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_achievement` (`user_id`, `achievement_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就进度表';

-- ===== 初始化数据 =====

-- 游戏分类
INSERT INTO `game_category` (`id`, `name`, `description`, `icon`, `sort_order`) VALUES
(1, '天文世界', '探索浩瀚星空，认识星座与天文知识', '/icons/astronomy.png', 1),
(2, '历史文明', '还原历史文物，了解古代文明故事', '/icons/history.png', 2),
(3, '昆虫自然', '认识昆虫世界，探索生命奥秘', '/icons/insect.png', 3);

-- 游戏关卡（天文：星座连线）
INSERT INTO `game_content` (`category_id`, `title`, `difficulty`, `content_data`, `knowledge_point`, `experience_reward`, `sort_order`) VALUES
(1, '猎户座连线', 1,
 '{"stars":[{"id":1,"x":120,"y":80,"name":"参宿四"},{"id":2,"x":200,"y":100,"name":"参宿五"},{"id":3,"x":160,"y":150,"name":"参宿三"},{"id":4,"x":100,"y":200,"name":"参宿六"},{"id":5,"x":220,"y":200,"name":"参宿七"}],"connections":[[1,3],[2,3],[3,4],[3,5]]}',
 '猎户座是冬季夜空中最显眼的星座之一，其中参宿四是一颗红超巨星，参宿七则是蓝超巨星。', 80, 1),
(1, '大熊座连线', 2,
 '{"stars":[{"id":1,"x":100,"y":100,"name":"天枢"},{"id":2,"x":150,"y":95,"name":"天璇"},{"id":3,"x":190,"y":110,"name":"天玑"},{"id":4,"x":220,"y":130,"name":"天权"},{"id":5,"x":230,"y":160,"name":"玉衡"},{"id":6,"x":200,"y":190,"name":"开阳"},{"id":7,"x":170,"y":210,"name":"摇光"}],"connections":[[1,2],[2,3],[3,4],[4,5],[5,6],[6,7]]}',
 '大熊座中的北斗七星是中国古代重要的天文坐标，常用于辨别方向，斗柄指向可判断季节。', 120, 2);

-- 游戏关卡（历史：文物拼图）
INSERT INTO `game_content` (`category_id`, `title`, `difficulty`, `content_data`, `knowledge_point`, `experience_reward`, `sort_order`) VALUES
(2, '四羊方尊拼图', 1,
 '{"rows":3,"cols":3,"image":"/puzzle/siyang.png","pieces":[{"id":1,"correctRow":0,"correctCol":0},{"id":2,"correctRow":0,"correctCol":1},{"id":3,"correctRow":0,"correctCol":2},{"id":4,"correctRow":1,"correctCol":0},{"id":5,"correctRow":1,"correctCol":1},{"id":6,"correctRow":1,"correctCol":2},{"id":7,"correctRow":2,"correctCol":0},{"id":8,"correctRow":2,"correctCol":1},{"id":9,"correctRow":2,"correctCol":2}]}',
 '四羊方尊是商朝晚期青铜礼器，铸造于公元前14世纪，造型雄奇，是中国青铜器的瑰宝，现藏于中国国家博物馆。', 100, 1),
(2, '青铜器纹饰拼图', 2,
 '{"rows":4,"cols":4,"image":"/puzzle/bronze.png","pieces":[]}',
 '饕餮纹是商周青铜器上常见的纹饰，以抽象兽面为主题，象征神秘与权力。', 150, 2);

-- 游戏关卡（昆虫：生命周期）
INSERT INTO `game_content` (`category_id`, `title`, `difficulty`, `content_data`, `knowledge_point`, `experience_reward`, `sort_order`) VALUES
(3, '蝴蝶生命周期排序', 1,
 '{"stages":[{"id":1,"name":"卵","image":"/insects/butterfly_egg.png","order":1},{"id":2,"name":"幼虫","image":"/insects/butterfly_larva.png","order":2},{"id":3,"name":"蛹","image":"/insects/butterfly_pupa.png","order":3},{"id":4,"name":"成虫","image":"/insects/butterfly_adult.png","order":4}]}',
 '蝴蝶是完全变态昆虫，一生经历卵→幼虫→蛹→成虫四个阶段，每个阶段形态差异巨大。', 80, 1),
(3, '蜜蜂分类游戏', 2,
 '{"insects":[{"id":1,"name":"蜜蜂","type":"beneficial"},{"id":2,"name":"蚜虫","type":"harmful"},{"id":3,"name":"瓢虫","type":"beneficial"},{"id":4,"name":"蝗虫","type":"harmful"}]}',
 '蜜蜂是重要的授粉昆虫，对农业生产和生态系统至关重要；瓢虫则是蚜虫的天敌，是益虫。', 120, 2);

-- 科普图鉴
INSERT INTO `encyclopedia` (`category`, `name`, `description`, `image_url`, `unlock_condition`, `related_game_id`) VALUES
('astronomy', '猎户座', '冬季星空中最壮观的星座，由7颗主要亮星组成，代表神话中的猎人俄里翁。', '/enc/orion.png', '完成猎户座连线游戏', 1),
('astronomy', '北斗七星', '大熊座中最亮的7颗星，自古以来是中国人辨方向、定季节的重要星象。', '/enc/beidou.png', '完成大熊座连线游戏', 2),
('history', '四羊方尊', '商代晚期青铜礼器，器身四角铸有四只卷角羊，是中国目前已知最大的商代青铜方尊。', '/enc/siyang.png', '完成四羊方尊拼图游戏', 3),
('history', '饕餮纹', '中国商周青铜器上最典型的装饰纹样，以兽面为主要特征，充满神秘威严之感。', '/enc/taotie.png', '完成青铜器纹饰拼图游戏', 4),
('insect', '蝴蝶', '鳞翅目昆虫，完全变态发育，翅膀上的鳞粉形成千变万化的美丽图案。', '/enc/butterfly.png', '完成蝴蝶生命周期排序游戏', 5),
('insect', '蜜蜂', '膜翅目蜜蜂科昆虫，过社会性生活，是最重要的农业授粉昆虫，一个蜂群可有数万只工蜂。', '/enc/bee.png', '完成蜜蜂分类游戏', 6);

-- 成就定义
INSERT INTO `achievement` (`id`, `name`, `description`, `icon`, `condition_type`, `condition_value`, `experience_reward`) VALUES
(1, '天文新手', '完成第一个天文游戏', '/achievements/star_novice.png', 'complete_game_category', 1, 100),
(2, '星空探索者', '完成所有天文游戏', '/achievements/star_explorer.png', 'complete_game_category', 2, 300),
(3, '文物守护者', '完成第一个历史文物拼图', '/achievements/history_guardian.png', 'complete_game_category', 3, 100),
(4, '自然观察家', '解锁3个昆虫图鉴', '/achievements/nature_observer.png', 'unlock_enc_category', 3, 200),
(5, '知识达人', '解锁6个科普图鉴', '/achievements/knowledge_master.png', 'unlock_enc', 6, 500),
(6, '初级学者', '达到5级', '/achievements/scholar_1.png', 'reach_level', 5, 300),
(7, '游戏达人', '完成5个游戏', '/achievements/gamer.png', 'complete_game', 5, 200);
