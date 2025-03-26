CREATE INDEX idx_users_created_at ON users (created_at);

CREATE INDEX idx_links_last_updated_at ON links (last_updated_at);
CREATE INDEX idx_links_last_checked_at ON links (last_checked_at);

-- Индекс для поиска подписок по ссылке
CREATE INDEX idx_subscriptions_link_id ON subscriptions (link_id);

-- Индекс для поиска связей тегов
CREATE INDEX idx_link_tags_tag_id ON link_tags (tag_id);

-- Индекс для поиска связей фильтров
CREATE INDEX idx_link_filters_filter_id ON link_filters (filter_id);
