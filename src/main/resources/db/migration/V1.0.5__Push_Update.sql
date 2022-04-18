CREATE TABLE IF NOT EXISTS push_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    client_uuid VARCHAR(128) NOT NULL,
    device_token VARCHAR(128) NOT NULL COMMENT '设备push token',
    device_type VARCHAR(128) NOT NULL COMMENT '设备类型,android/ios/harmony',
    extra VARCHAR(1024) DEFAULT NULL COMMENT '预留json格式',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_clientid (client_uuid)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;