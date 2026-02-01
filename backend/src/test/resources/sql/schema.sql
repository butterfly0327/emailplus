-- 테스트용 스키마

-- accounts 테이블
CREATE TABLE IF NOT EXISTS accounts (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- gestures 테이블
CREATE TABLE IF NOT EXISTS gestures (
    gesture_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- actions 테이블
CREATE TABLE IF NOT EXISTS actions (
    action_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action_name VARCHAR(100) NOT NULL,
    action_category VARCHAR(100),
    action_description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- mappings 테이블
CREATE TABLE IF NOT EXISTS mappings (
    mapping_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    is_representative BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

-- mapping_items 테이블
CREATE TABLE IF NOT EXISTS mapping_items (
    mapping_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mapping_id BIGINT NOT NULL,
    gesture_id BIGINT NOT NULL,
    action_id BIGINT NOT NULL,
    sort_order INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mapping_id) REFERENCES mappings(mapping_id) ON DELETE CASCADE,
    FOREIGN KEY (gesture_id) REFERENCES gestures(gesture_id) ON DELETE CASCADE,
    FOREIGN KEY (action_id) REFERENCES actions(action_id) ON DELETE CASCADE,
    UNIQUE KEY unique_mapping_gesture (mapping_id, gesture_id),
    UNIQUE KEY unique_mapping_action (mapping_id, action_id)
);
