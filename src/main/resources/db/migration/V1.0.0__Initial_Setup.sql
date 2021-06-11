
CREATE TABLE registries
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    box_reg_key VARCHAR(128) NOT NULL,
    client_reg_key VARCHAR(128) NOT NULL,
    box_uuid VARCHAR(128) NOT NULL,
    client_uuid VARCHAR(128) NOT NULL,
    state VARCHAR(64) NOT NULL,
    subdomain VARCHAR(128) NOT NULL,
    tunnel_server VARCHAR(512) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_guards (box_uuid, client_uuid, subdomain)
) ENGINE = InnoDB
  DEFAULT CHARSET=utf8mb4;
