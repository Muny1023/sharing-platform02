CREATE TABLE IF NOT EXISTS seata_demo_account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    demo_key VARCHAR(64) NOT NULL,
    note VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_seata_demo_account_key (demo_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seata_demo_content (
    id BIGINT NOT NULL AUTO_INCREMENT,
    demo_key VARCHAR(64) NOT NULL,
    note VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_seata_demo_content_key (demo_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS undo_log (
    branch_id BIGINT NOT NULL,
    xid VARCHAR(128) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info LONGBLOB NOT NULL,
    log_status INT NOT NULL,
    log_created DATETIME NOT NULL,
    log_modified DATETIME NOT NULL,
    PRIMARY KEY (branch_id, xid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
