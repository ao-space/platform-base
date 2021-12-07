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
    box_domain VARCHAR(128) DEFAULT NULL COMMENT '用户域名',
    box_pub_key VARCHAR(1024) DEFAULT NULL COMMENT '盒子公钥',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    expires_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pkey (pkey)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

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
    subdomain VARCHAR(128) DEFAULT NULL COMMENT '用户域名',
    content VARCHAR(1024) NOT NULL COMMENT '反馈内容',
    email VARCHAR(128) DEFAULT NULL,
    phone_number VARCHAR(128) DEFAULT NULL,
    image_urls VARCHAR(1024) DEFAULT NULL COMMENT '图片地址',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS questionnaire (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(1024) NOT NULL COMMENT '标题',
    content VARCHAR(1024) NOT NULL COMMENT '内容(地址)',
    start_at DATETIME DEFAULT NULL COMMENT '开始日期',
    end_at DATETIME DEFAULT NULL COMMENT '结束日期',
    payload_survey_id BIGINT NOT NULL COMMENT '第三方问卷id',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payload_survey_id (payload_survey_id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS questionnaire_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    subdomain VARCHAR(128) NOT NULL COMMENT '用户域名',
    payload_survey_id BIGINT NOT NULL COMMENT '第三方问卷id',
    payload_answer_id BIGINT NOT NULL COMMENT '第三方问卷答案id',
    payload_answer_at DATETIME DEFAULT NULL COMMENT '用户提交答案时间',
    payload_answer_detail TEXT DEFAULT NULL COMMENT '用户答案详情,json格式',
    extra VARCHAR(1024) DEFAULT NULL COMMENT '预留json格式',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_subdomain_payload_answer_id (subdomain, payload_survey_id, payload_answer_id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;