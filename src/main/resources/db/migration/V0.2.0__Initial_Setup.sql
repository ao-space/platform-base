
CREATE TABLE IF NOT EXISTS registries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    box_reg_key VARCHAR(128) NOT NULL,
    client_reg_key VARCHAR(128) NOT NULL,
    box_uuid VARCHAR(128) NOT NULL,
    client_uuid VARCHAR(128) NOT NULL,
    state VARCHAR(64) NOT NULL,
    subdomain VARCHAR(128) NOT NULL,
    tunnel_server VARCHAR(512) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_guards (box_uuid, client_uuid, subdomain)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notify_device (
    id BIGINT NOT NULL AUTO_INCREMENT,
    client_uuid VARCHAR(128) NOT NULL,
    client_reg_key VARCHAR(128) NOT NULL,
    device_id VARCHAR(128) NOT NULL,
    platform VARCHAR(128) NOT NULL,
    device_token VARCHAR(128),
    env VARCHAR(128),
    state INT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_device (device_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS notify_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    body TEXT NOT NULL,
    ext_parameters TEXT NOT NULL,
    client_uuid VARCHAR(128) NOT NULL,
    message_id VARCHAR(128) NOT NULL,
    state INT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_message (message_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS box_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pkey VARCHAR(128) NOT NULL,
    bkey VARCHAR(128) DEFAULT NULL,
    box_domain VARCHAR(128) DEFAULT NULL,
    box_pub_key VARCHAR(1024) DEFAULT NULL,
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    expires_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pkey (pkey)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS app_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    app_name VARCHAR(128) NOT NULL,
    app_type VARCHAR(128) NOT NULL,
    app_version VARCHAR(128) NOT NULL,
    app_size BIGINT DEFAULT NULL COMMENT '单位字节',
    update_desc TEXT DEFAULT NULL,
    force_update INT DEFAULT 0 COMMENT '1-强制更新;0-可选更新',
    download_url VARCHAR(256) NOT NULL,
    md5 VARCHAR(128) NOT NULL,
    extra VARCHAR(1024) DEFAULT NULL COMMENT '预留json格式',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name_type_version (app_name,app_type,app_version)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;