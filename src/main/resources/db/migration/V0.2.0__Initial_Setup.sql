
CREATE TABLE IF NOT EXISTS registries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    box_reg_key VARCHAR(128) NOT NULL,
    client_reg_key VARCHAR(128) NOT NULL,
    box_uuid VARCHAR(128) NOT NULL,
    client_uuid VARCHAR(128) NOT NULL,
    subdomain VARCHAR(128) NOT NULL,
    type VARCHAR(128) NOT NULL COMMENT '注册类型box/client',
    created_at DATETIME,
    updated_at DATETIME,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_boxid_clientid (box_uuid, client_uuid),
    UNIQUE KEY uk_subdomain (subdomain)
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
