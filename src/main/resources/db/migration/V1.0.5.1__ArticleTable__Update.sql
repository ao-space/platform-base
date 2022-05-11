-- 正式版本发布前临时脚本
ALTER TABLE article_info DROP INDEX uk_title ;
ALTER TABLE article_info ADD KEY uk_title (title) ;