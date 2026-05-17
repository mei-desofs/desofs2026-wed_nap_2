-- ArcadeHaven Database Schema
-- V1: Initial table creation

CREATE TABLE IF NOT EXISTS users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'BUYER',
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS games (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    price       NUMERIC(10, 2) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    rawg_api_id VARCHAR(100),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    publisher_id UUID NOT NULL REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS game_files (
    game_id     UUID         NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    filename    VARCHAR(255) NOT NULL,
    path        VARCHAR(500) NOT NULL,
    file_type   VARCHAR(20)  NOT NULL,
    uploaded_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS orders (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id    UUID         NOT NULL REFERENCES users(id),
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    total_price NUMERIC(10, 2) NOT NULL DEFAULT 0,
    invoice_path VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_items (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id       UUID           NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    game_id        UUID           NOT NULL REFERENCES games(id),
    price          NUMERIC(10, 2) NOT NULL,
    activation_key VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS libraries (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID      NOT NULL UNIQUE REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS library_entries (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    library_id     UUID         NOT NULL REFERENCES libraries(id) ON DELETE CASCADE,
    game_id        UUID         NOT NULL REFERENCES games(id),
    activation_key VARCHAR(100) NOT NULL,
    acquired_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
);

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_games_status ON games(status);
CREATE INDEX IF NOT EXISTS idx_games_publisher ON games(publisher_id);
CREATE INDEX IF NOT EXISTS idx_orders_buyer ON orders(buyer_id);
CREATE INDEX IF NOT EXISTS idx_library_entries_library ON library_entries(library_id);
