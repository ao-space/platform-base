-- 正式版本发布前临时脚本
ALTER TABLE applet_info ADD  web_down_url VARCHAR(256) COMMENT 'web程序下载地址';
ALTER TABLE applet_info ADD  web_md5 VARCHAR(128) DEFAULT NULL;
ALTER TABLE applet_info ADD  member_permission INT DEFAULT 1 COMMENT '0-默认对成员关闭;1-默认对成员开放';