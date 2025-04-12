
-- Добавляем тип ссылки в основную таблицу
ALTER TABLE links ADD COLUMN type VARCHAR(31);

-- Создаем таблицу для StackOverflow-ссылок
CREATE TABLE IF NOT EXISTS stackoverflow_links (
    link_id BIGINT PRIMARY KEY REFERENCES links(id) ON DELETE CASCADE,

    answer_last_id BIGINT,
    answer_last_username VARCHAR,
    answer_created_at TIMESTAMP WITH TIME ZONE,
    answer_preview_description VARCHAR,

    comment_id BIGINT,
    comment_last_username VARCHAR,
    comment_created_at TIMESTAMP WITH TIME ZONE,
    comment_preview_description VARCHAR
);

-- Создаем таблицу для GitHub-ссылок
CREATE TABLE IF NOT EXISTS github_links (
    link_id BIGINT PRIMARY KEY REFERENCES links(id) ON DELETE CASCADE,

    issue_last_id BIGINT,
    issue_title VARCHAR,
    issue_creator_username VARCHAR,
    issue_created_at TIMESTAMP WITH TIME ZONE,
    issue_preview_description VARCHAR,

    pull_last_id BIGINT,
    pull_title VARCHAR,
    pull_creator_username VARCHAR,
    pull_created_at TIMESTAMP WITH TIME ZONE,
    pull_preview_description VARCHAR
);
