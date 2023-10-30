CREATE TABLE IF NOT EXISTS `distributed_reentrant_locks` (
    `lock_key` VARCHAR(128) NOT NULL,
    `lock_value` VARCHAR(128) NOT NULL,
    `expires_at` timestamp(0) NOT NULL,
    `reentrant_count` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME DEFAULT NULL,
    `updated_at` DATETIME DEFAULT NULL,
    `version` INT DEFAULT 0,
    PRIMARY KEY (`lock_key`)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `distributed_reentrant_read_write_locks` (
    `lock_key` VARCHAR(128) NOT NULL,
    `expires_at` TIMESTAMP(0) NOT NULL,
    `mode` VARCHAR(20),
    `lock_holds_json` VARCHAR(2048),
    `created_at` DATETIME DEFAULT NULL,
    `updated_at` DATETIME DEFAULT NULL,
    `version` INT DEFAULT 0,
    PRIMARY KEY (`lock_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;