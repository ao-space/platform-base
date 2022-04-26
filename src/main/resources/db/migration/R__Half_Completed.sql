-- 正式版本发布前临时脚本
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