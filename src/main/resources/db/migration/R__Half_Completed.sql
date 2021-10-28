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
    min_compatible_android_version VARCHAR(128) DEFAULT NULL COMMENT '最小android兼容版本',
    min_compatible_ios_version VARCHAR(128) DEFAULT NULL COMMENT '最小ios兼容版本',
    min_compatible_box_version VARCHAR(128) DEFAULT NULL COMMENT '最小box兼容版本',
    extra VARCHAR(1024) DEFAULT NULL COMMENT '预留json格式',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pkg_name_type_version (pkg_name,pkg_type,pkg_version)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS proposal (
    id BIGINT NOT NULL AUTO_INCREMENT,
    content VARCHAR(1024) NOT NULL COMMENT '反馈内容',
    email VARCHAR(128) DEFAULT NULL,
    phone_numer VARCHAR(128) DEFAULT NULL,
    image_urls VARCHAR(1024) DEFAULT NULL COMMENT '图片地址',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;