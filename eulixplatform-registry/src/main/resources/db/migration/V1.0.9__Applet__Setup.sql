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


CREATE TABLE IF NOT EXISTS applet_info (
    id BIGINT NOT NULL AUTO_INCREMENT,
    applet_name VARCHAR(32)  COMMENT '小程序名字',
    applet_en_name VARCHAR(32)  COMMENT '小程序英文名字',
    state INT DEFAULT 0 COMMENT '0-支持安装;1-敬请期待',
    applet_id VARCHAR(32) NOT NULL COMMENT '小程序标识',
    applet_version VARCHAR(128) NOT NULL,
    applet_size BIGINT DEFAULT NULL COMMENT '单位字节',
    update_desc TEXT DEFAULT NULL,
    force_update INT DEFAULT 0 COMMENT '1-强制更新;0-可选更新',
    icon_url VARCHAR(256) NOT NULL COMMENT '小图标下载地址',
    down_url VARCHAR(256) NOT NULL COMMENT '程序下载地址',
    md5 VARCHAR(128) DEFAULT NULL,
    min_compatible_box_version VARCHAR(128) DEFAULT NULL COMMENT '最小box兼容版本',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    KEY uk_appletid (applet_id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE applet_info  ADD COLUMN applet_secret VARCHAR(128);
ALTER TABLE applet_info  ADD COLUMN categories VARCHAR(256);
