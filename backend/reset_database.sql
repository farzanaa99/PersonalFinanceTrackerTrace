DROP TABLE IF EXISTS spending_alerts;
DROP TABLE IF EXISTS savings_goals;
DROP TABLE IF EXISTS recurring_transactions;
DROP TABLE IF EXISTS financial_insights;
DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS income;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    firebase_uid VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    created_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL,
    budget DOUBLE DEFAULT 0.0,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_category_per_user (category_name, user_id)
);

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DOUBLE NOT NULL,
    description TEXT,
    date DATE NOT NULL,
    category_id BIGINT,
    user_id BIGINT NOT NULL,
    transaction_type VARCHAR(31) NOT NULL, -- discriminator for inheritance
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE expenses (
    id BIGINT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES transactions(id) ON DELETE CASCADE
);

CREATE TABLE income (
    id BIGINT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES transactions(id) ON DELETE CASCADE
);

CREATE TABLE spending_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_type VARCHAR(255),
    message TEXT,
    threshold DOUBLE,
    current_value DOUBLE,
    created_at TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    category_id BIGINT,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE savings_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    target_amount DOUBLE,
    current_amount DOUBLE,
    target_date DATE,
    created_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    category_id BIGINT,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE recurring_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    amount DOUBLE,
    next_due_date DATE,
    recurrence_pattern VARCHAR(50), -- DAILY, WEEKLY, MONTHLY, YEARLY
    category_id BIGINT,
    user_id BIGINT NOT NULL,
    transaction_type VARCHAR(50), -- INCOME, EXPENSE
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE financial_insights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insight_type VARCHAR(255),
    title VARCHAR(255),
    description TEXT,
    value DOUBLE,
    percentage DOUBLE,
    period VARCHAR(255),
    calculated_at TIMESTAMP,
    category_id BIGINT,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE INDEX idx_transactions_user_date ON transactions(user_id, date);
CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE INDEX idx_categories_user ON categories(user_id);
CREATE INDEX idx_spending_alerts_user ON spending_alerts(user_id);
CREATE INDEX idx_savings_goals_user ON savings_goals(user_id);
CREATE INDEX idx_recurring_transactions_user ON recurring_transactions(user_id);
CREATE INDEX idx_financial_insights_user ON financial_insights(user_id);

SHOW TABLES;

DESCRIBE users;
DESCRIBE categories;
DESCRIBE transactions;
DESCRIBE expenses;
DESCRIBE income;
DESCRIBE spending_alerts;
DESCRIBE savings_goals;
DESCRIBE recurring_transactions;
DESCRIBE financial_insights; 