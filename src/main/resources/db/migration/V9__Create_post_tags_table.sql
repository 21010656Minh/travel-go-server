-- V9__Create_post_tags_table.sql
-- Many-to-many relationship between posts and tags

CREATE TABLE post_tags (
    post_id UUID NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE
);

CREATE INDEX idx_post_tags_tag_id ON post_tags(tag_id);

COMMENT ON TABLE post_tags IS 'Many-to-many relationship between posts and tags';
