-- V14__Create_content_comments_table.sql
-- Comments on posts and watches

CREATE TABLE content_comments (
    comment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID,
    watch_id UUID,
    user_id UUID NOT NULL,
    content TEXT NOT NULL,
    parent_comment_id UUID,
    reply_count INTEGER DEFAULT 0,
    like_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_content_comments_post FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_content_comments_watch FOREIGN KEY (watch_id) REFERENCES watches(watch_id) ON DELETE CASCADE,
    CONSTRAINT fk_content_comments_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_content_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES content_comments(comment_id) ON DELETE CASCADE,
    CONSTRAINT chk_content_comments_content CHECK (post_id IS NOT NULL OR watch_id IS NOT NULL)
);

CREATE INDEX idx_content_comments_post_id ON content_comments(post_id);
CREATE INDEX idx_content_comments_watch_id ON content_comments(watch_id);
CREATE INDEX idx_content_comments_user_id ON content_comments(user_id);
CREATE INDEX idx_content_comments_parent_id ON content_comments(parent_comment_id);
CREATE INDEX idx_content_comments_created_at ON content_comments(created_at DESC);

COMMENT ON TABLE content_comments IS 'Comments on posts and watches with support for nested replies';
