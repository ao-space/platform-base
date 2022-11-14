-- 正式版本发布前临时脚本
ALTER TABLE proposal ADD box_uuid VARCHAR(128) DEFAULT NULL;
ALTER TABLE proposal ADD user_id VARCHAR(128) DEFAULT NULL;

ALTER TABLE questionnaire_feedback ADD box_uuid VARCHAR(128) NOT NULL;
ALTER TABLE questionnaire_feedback ADD user_id VARCHAR(128) NOT NULL;
ALTER TABLE questionnaire_feedback MODIFY COLUMN user_domain VARCHAR(128) DEFAULT NULL COMMENT '用户域名';
ALTER TABLE questionnaire_feedback DROP INDEX uk_userdomain_payload_answer_id;

ALTER TABLE questionnaire_feedback ADD UNIQUE KEY uk_userid_payload_answer_id (box_uuid, user_id, payload_survey_id, payload_answer_id);