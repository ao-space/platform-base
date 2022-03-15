-- 正式版本发布前临时脚本

CREATE TABLE IF NOT EXISTS catalogue_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cata_name VARCHAR(32) NOT NULL COMMENT '节点名称',
    parent_id BIGINT DEFAULT NULL COMMENT '父节点id',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    KEY uk_parentid (parent_id),
    UNIQUE KEY uk_cataname (cata_name)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

insert ignore into catalogue_info (id, cata_name, created_at, updated_at) values ('1', '帮助中心', now(), now());

CREATE TABLE IF NOT EXISTS article_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(32) NOT NULL COMMENT '文章标题',
    cata_id BIGINT DEFAULT NULL COMMENT '目录id',
    content TEXT DEFAULT NULL COMMENT '文章内容',
    state INT DEFAULT 0 COMMENT '0-草稿;1-已发布',
    last_publishd_at DATETIME DEFAULT NULL,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    KEY uk_cataid (cata_id),
    UNIQUE KEY uk_title (title)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reserved_domain (
   id BIGINT NOT NULL AUTO_INCREMENT,
   regex VARCHAR(128) NOT NULL COMMENT '正则表达式',
   description VARCHAR(256) NOT NULL COMMENT '描述',
   created_at DATETIME DEFAULT NULL,
   updated_at DATETIME DEFAULT NULL,
   version INT DEFAULT 0,
   PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

