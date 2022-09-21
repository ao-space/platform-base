-- 正式版本发布前临时脚本
CREATE TABLE IF NOT EXISTS box_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    box_uuid VARCHAR(128) NOT NULL COMMENT '盒子id',
    service_id VARCHAR(128) NOT NULL COMMENT '应用id',
    service_name VARCHAR(128) DEFAULT NULL COMMENT '应用名称',
    box_reg_key VARCHAR(128) NOT NULL COMMENT '盒子注册码',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    expires_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_boxid_serviceid  (box_uuid, service_id , box_reg_key),
    KEY uk_boxregkey (box_reg_key)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE box_info ADD box_pub_key  VARCHAR(1024) DEFAULT NULL COMMENT '盒子公钥' ;
ALTER TABLE box_info ADD auth_type  VARCHAR(128) NOT NULL COMMENT '鉴权类型,box_uuid/box_pub_key';
create unique index uk_info_boxpubkey on box_info (box_pub_key);
