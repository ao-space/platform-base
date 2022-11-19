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
ALTER TABLE applet_info ADD  web_down_url VARCHAR(256) COMMENT 'web程序下载地址';
ALTER TABLE applet_info ADD  web_md5 VARCHAR(128) DEFAULT NULL;
ALTER TABLE applet_info ADD  member_permission INT DEFAULT 1 COMMENT '0-默认对成员关闭;1-默认对成员开放';