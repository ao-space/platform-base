-- 正式版本发布前临时脚本
CREATE TABLE IF NOT EXISTS pkg_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pkg_name VARCHAR(128) NOT NULL,
    pkg_type VARCHAR(128) NOT NULL,
    pkg_version VARCHAR(128) NOT NULL,
    pkg_size BIGINT DEFAULT NULL COMMENT '单位字节',
    update_desc TEXT DEFAULT NULL,
    force_update INT DEFAULT 0 COMMENT '1-强制更新;0-可选更新',
    download_url VARCHAR(256) NOT NULL,
    md5 VARCHAR(128) DEFAULT NULL,
    extra VARCHAR(1024) DEFAULT NULL COMMENT '预留json格式',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name_type_version (pkg_name,pkg_type,pkg_version)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;