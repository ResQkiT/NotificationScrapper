-- Создание таблицы пользователей (Telegram ID)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Создание таблицы ссылок
CREATE TABLE IF NOT EXISTS links (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(2048) UNIQUE NOT NULL,
    last_updated_at TIMESTAMP WITH TIME ZONE, -- Время последнего обновления контента
    last_checked_at TIMESTAMP WITH TIME ZONE  -- Время последней проверки ссылки
);

-- Создание таблицы подписок (многие-ко-многим: пользователь <-> ссылка)
CREATE TABLE IF NOT EXISTS subscriptions (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    link_id BIGINT REFERENCES links(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, link_id)
);

-- Создание таблицы тегов
CREATE TABLE IF NOT EXISTS tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

-- Связь многие-ко-многим: ссылка <-> тег
CREATE TABLE IF NOT EXISTS link_tags (
    link_id BIGINT REFERENCES links(id) ON DELETE CASCADE,
    tag_id BIGINT REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (link_id, tag_id)
);

-- Создание таблицы фильтров
CREATE TABLE IF NOT EXISTS filters (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

-- Связь многие-ко-многим: ссылка <-> фильтр
CREATE TABLE IF NOT EXISTS link_filters (
    link_id BIGINT REFERENCES links(id) ON DELETE CASCADE,
    filter_id BIGINT REFERENCES filters(id) ON DELETE CASCADE,
    PRIMARY KEY (link_id, filter_id)
);
