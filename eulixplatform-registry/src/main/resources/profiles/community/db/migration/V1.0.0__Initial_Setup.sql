-- Copyright (c) 2022 Institute of Software Chinese Academy of Sciences (ISCAS)
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

CREATE TABLE IF NOT EXISTS box_registries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    box_uuid VARCHAR(128) NOT NULL,
    box_reg_key VARCHAR(128) NOT NULL COMMENT '盒子注册码',
    network_client_id VARCHAR(128) NOT NULL COMMENT 'network client id',
    network_secret_key VARCHAR(128) DEFAULT NULL COMMENT 'network client访问密钥',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_boxid (box_uuid),
    UNIQUE KEY uk_clientid (network_client_id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_registries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    box_uuid VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    user_reg_key VARCHAR(128) NOT NULL COMMENT '用户注册码',
    type VARCHAR(128) NOT NULL COMMENT '注册类型,admin/member',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_boxid_userid (box_uuid, user_id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS client_registries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    box_uuid VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    client_uuid VARCHAR(128) NOT NULL,
    client_reg_key VARCHAR(128) NOT NULL COMMENT '客户端注册码',
    type VARCHAR(128) NOT NULL COMMENT '注册类型,bind/auth',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_boxid_userid_clientid (box_uuid, user_id, client_uuid)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS subdomain (
    id BIGINT NOT NULL AUTO_INCREMENT,
    box_uuid VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) DEFAULT NULL,
    subdomain VARCHAR(128) NOT NULL COMMENT '子域名',
    user_domain VARCHAR(128) DEFAULT NULL COMMENT '用户域名',
    state INT DEFAULT 0 COMMENT '0-临时;1-已使用',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    expires_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    KEY uk_boxid_userid (box_uuid, user_id),
    UNIQUE KEY uk_subdomain (subdomain)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS network_client_server_route (
    id BIGINT NOT NULL AUTO_INCREMENT,
    network_client_id VARCHAR(128) NOT NULL COMMENT 'network client id',
    network_server_id BIGINT NOT NULL COMMENT 'network server id',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_net_clientid (network_client_id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS network_server_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    server_protocol VARCHAR(128) NOT NULL COMMENT 'network server协议',
    server_addr VARCHAR(128) NOT NULL COMMENT 'network server地址',
    server_port INT NOT NULL COMMENT 'network server端口',
    identifier VARCHAR(128) NOT NULL COMMENT 'network server标识符，如ip',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    state INT DEFAULT 0 COMMENT '0-未启用;1-已上线',
    extra VARCHAR(1024) DEFAULT NULL COMMENT '扩展字段,json格式',
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_identifier (identifier)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

insert ignore into network_server_info (server_protocol, server_addr, server_port, identifier, extra, created_at, updated_at) values ('tls','network server poblic domain', 0, 'network server inner address', '', now(), now());

CREATE TABLE IF NOT EXISTS pkey_auth (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pkey VARCHAR(128) NOT NULL,
    bkey VARCHAR(128) DEFAULT NULL,
    user_domain VARCHAR(128) DEFAULT NULL COMMENT '用户域名',
    box_pub_key VARCHAR(1024) DEFAULT NULL COMMENT '盒子公钥',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    expires_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pkey (pkey)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

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

CREATE TABLE IF NOT EXISTS box_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    box_uuid VARCHAR(128) NOT NULL,
    description VARCHAR(128) DEFAULT NULL,
    extra VARCHAR(1024) DEFAULT NULL COMMENT '预留json格式',
    auth_type VARCHAR(128) DEFAULT NULL COMMENT '鉴权类型,box_uuid/box_pub_key',
    box_pub_key VARCHAR(1024) DEFAULT NULL COMMENT '盒子公钥',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_info_boxid (box_uuid)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;