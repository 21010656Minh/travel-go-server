-- V3__Create_tags_table.sql
-- Tags table - no dependencies, needed by many tables

CREATE TABLE tags (
    tag_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tag_slug ON tags(slug);
CREATE INDEX idx_tag_title ON tags(title);

COMMENT ON TABLE tags IS 'Tags for categorizing content (posts, blogs, watches)';
