-- 正式版本发布前临时脚本
DROP TABLE registries;

CREATE TABLE IF NOT EXISTS registries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    box_reg_key VARCHAR(128) NOT NULL COMMENT '盒子注册码',
    user_reg_key VARCHAR(128) DEFAULT NULL COMMENT '用户注册码',
    client_reg_key VARCHAR(128) DEFAULT NULL COMMENT '客户端注册码',
    box_uuid VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) DEFAULT NULL,
    client_uuid VARCHAR(128) DEFAULT NULL,
    user_domain VARCHAR(128) DEFAULT NULL COMMENT '用户域名',
    type VARCHAR(128) NOT NULL COMMENT '注册类型,box/user_admin/user_member/client_bind/client_auth',
    network_client_id VARCHAR(128) DEFAULT NULL COMMENT 'network client id',
    network_secret_key VARCHAR(128) DEFAULT NULL COMMENT 'network client访问密钥',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_boxid_userid_clientid (box_uuid, user_id, client_uuid),
    UNIQUE KEY uk_userdomain (user_domain)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS network_client_server_route (
    id BIGINT NOT NULL AUTO_INCREMENT,
    network_client_id VARCHAR(128) DEFAULT NULL COMMENT 'network client id',
    network_server_id VARCHAR(128) DEFAULT NULL COMMENT 'network server id',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS network_server_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    server_protocol VARCHAR(128) NOT NULL COMMENT 'network server协议',
    server_addr VARCHAR(128) NOT NULL COMMENT 'network server地址',
    server_port INT NOT NULL COMMENT 'network server端口',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;
