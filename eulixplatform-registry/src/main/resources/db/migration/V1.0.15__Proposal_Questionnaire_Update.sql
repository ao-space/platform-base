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

-- 正式版本发布前临时脚本
ALTER TABLE proposal ADD box_uuid VARCHAR(128) DEFAULT NULL;
ALTER TABLE proposal ADD user_id VARCHAR(128) DEFAULT NULL;

ALTER TABLE questionnaire_feedback ADD box_uuid VARCHAR(128) NOT NULL;
ALTER TABLE questionnaire_feedback ADD user_id VARCHAR(128) NOT NULL;
ALTER TABLE questionnaire_feedback MODIFY COLUMN user_domain VARCHAR(128) DEFAULT NULL COMMENT '用户域名';
ALTER TABLE questionnaire_feedback DROP INDEX uk_userdomain_payload_answer_id;

ALTER TABLE questionnaire_feedback ADD UNIQUE KEY uk_userid_payload_answer_id (box_uuid, user_id, payload_survey_id, payload_answer_id);