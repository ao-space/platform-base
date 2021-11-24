-- 正式版本发布前临时脚本
-- ALTER TABLE proposal DROP COLUMN subdomain;
ALTER TABLE proposal ADD COLUMN subdomain VARCHAR(128) DEFAULT NULL COMMENT '用户域名';