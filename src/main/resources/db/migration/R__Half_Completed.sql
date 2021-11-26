-- 正式版本发布前临时脚本
-- 判断 proposal 表是否存在 subdomain 字段，不存在则添加
DELIMITER $
DROP PROCEDURE IF EXISTS schema_change$
CREATE PROCEDURE schema_change()
BEGIN
IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_schema = DATABASE()  AND table_name = 'proposal' AND column_name = 'subdomain') THEN
ALTER TABLE proposal ADD COLUMN subdomain VARCHAR(128) DEFAULT NULL COMMENT '用户域名';
END IF;
END$
DELIMITER ;

CALL schema_change();

DROP PROCEDURE IF EXISTS schema_change;