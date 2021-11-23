-- 正式版本发布前临时脚本
CREATE TABLE IF NOT EXISTS questionnaire (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(1024) NOT NULL COMMENT '标题',
    content VARCHAR(1024) NOT NULL COMMENT '内容(地址)',
    start_at DATETIME DEFAULT NULL COMMENT '开始日期',
    end_at DATETIME DEFAULT NULL COMMENT '结束日期',
    payload_survey_id BIGINT NOT NULL COMMENT '第三方问卷id',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payload_survey_id (payload_survey_id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS questionnaire_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    subdomain VARCHAR(128) NOT NULL COMMENT '用户域名',
    payload_survey_id BIGINT NOT NULL COMMENT '第三方问卷id',
    payload_answer_id BIGINT NOT NULL COMMENT '第三方问卷答案id',
    payload_answer_at DATETIME DEFAULT NULL COMMENT '用户提交答案时间',
    payload_answer_detail TEXT DEFAULT NULL COMMENT '用户答案详情,json格式',
    extra VARCHAR(1024) DEFAULT NULL COMMENT '预留json格式',
    created_at DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT NULL,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_subdomain_payload_answer_id (subdomain, payload_survey_id, payload_answer_id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;