
CREATE TABLE registries
(
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
) ENGINE = InnoDB
  DEFAULT CHARSET=utf8mb4;

CREATE TABLE notify_device
(
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
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE notify_message
(
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
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE box_info
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    pkey VARCHAR(128) NOT NULL,
    bkey VARCHAR(128) NULL,
    box_domain VARCHAR(128) NULL,
    box_pub_key VARCHAR(1024) NULL,
    created_at DATETIME,
    updated_at DATETIME,
    expires_at DATETIME,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pkey (pkey)
) ENGINE = InnoDB
  DEFAULT CHARSET=utf8mb4;