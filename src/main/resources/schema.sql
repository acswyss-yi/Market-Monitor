-- MySQL-compatible DDL (H2 runs this via spring.sql.init.mode=always)
CREATE TABLE IF NOT EXISTS alert_rules (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100)    NOT NULL,
    asset_type     VARCHAR(20)     NOT NULL COMMENT 'CRYPTO | STOCK',
    symbol         VARCHAR(50)     NOT NULL COMMENT 'e.g. bitcoin, AAPL',
    condition_type VARCHAR(30)     NOT NULL COMMENT 'ABOVE | BELOW',
    threshold_price DECIMAL(20,8)  NOT NULL,
    notify_channel VARCHAR(20)     NOT NULL COMMENT 'EMAIL | DINGTALK',
    notify_target  VARCHAR(500)    NOT NULL COMMENT 'email address or webhook URL',
    enabled        BOOLEAN         NOT NULL DEFAULT TRUE,
    cooldown_minutes INT           NOT NULL DEFAULT 60,
    last_triggered_at TIMESTAMP    NULL     DEFAULT NULL,
    created_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);