CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    INDEX idx_users_status (status),
    INDEX idx_users_deleted_created (deleted, created_at),
    INDEX idx_users_name (last_name, first_name)
);

CREATE TABLE api_keys (
    id BIGINT NOT NULL AUTO_INCREMENT,
    key_hash CHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    last_used_at TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_api_keys_hash UNIQUE (key_hash),
    INDEX idx_api_keys_active (active)
);
