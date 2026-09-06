-- V13__Create_watch_related_tables.sql
-- Watch tags, saved watches, and watch histories

-- Watch tags
CREATE TABLE watch_tags (
    watch_id UUID NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (watch_id, tag_id),
    CONSTRAINT fk_watch_tags_watch FOREIGN KEY (watch_id) REFERENCES watches(watch_id) ON DELETE CASCADE,
    CONSTRAINT fk_watch_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE
);

-- Saved watches
CREATE TABLE saved_watches (
    saved_watch_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    watch_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_saved_watches_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_watches_watch FOREIGN KEY (watch_id) REFERENCES watches(watch_id) ON DELETE CASCADE,
    CONSTRAINT uk_user_watch UNIQUE (user_id, watch_id)
);

-- Watch histories
CREATE TABLE watch_histories (
    watch_history_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    watch_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_watch_histories_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_watch_histories_watch FOREIGN KEY (watch_id) REFERENCES watches(watch_id) ON DELETE CASCADE,
    CONSTRAINT uk_user_watch_history UNIQUE (user_id, watch_id)
);

CREATE INDEX idx_watch_tags_tag_id ON watch_tags(tag_id);
CREATE INDEX idx_saved_watches_user_id ON saved_watches(user_id);
CREATE INDEX idx_saved_watches_watch_id ON saved_watches(watch_id);
CREATE INDEX idx_watch_histories_user_id ON watch_histories(user_id);
CREATE INDEX idx_watch_histories_watch_id ON watch_histories(watch_id);
CREATE INDEX idx_watch_histories_updated_at ON watch_histories(updated_at DESC);

COMMENT ON TABLE watch_tags IS 'Many-to-many relationship between watches and tags';
COMMENT ON TABLE saved_watches IS 'User bookmarked watches';
COMMENT ON TABLE watch_histories IS 'Watch viewing history';
