
-- Добавляем тип ссылки в основную таблицу
ALTER TABLE links ADD COLUMN type VARCHAR(31);

-- Создаем таблицу для StackOverflow-ссылок
CREATE TABLE IF NOT EXISTS stackoverflow_links (
    link_id BIGINT PRIMARY KEY REFERENCES links(id) ON DELETE CASCADE,
    question_title VARCHAR NOT NULL,
    username VARCHAR NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    preview_content VARCHAR,
    last_answer_date TIMESTAMP WITH TIME ZONE
);

-- Создаем таблицу для GitHub-ссылок
CREATE TABLE IF NOT EXISTS github_links (
    link_id BIGINT PRIMARY KEY REFERENCES links(id) ON DELETE CASCADE,
    issue_last_id BIGINT NOT NULL,
    issue_title VARCHAR NOT NULL,
    issue_creator_username VARCHAR NOT NULL,
    issue_created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    issue_preview_description VARCHAR(200),

    pull_last_id BIGINT NOT NULL,
    pull_title VARCHAR NOT NULL,
    pull_creator_username VARCHAR NOT NULL,
    pull_created_at TIMESTAMP WITH TIME ZONE,
    pull_preview_description VARCHAR(200)
);
