-- V15__Create_likes_tables.sql
-- Content likes and comment likes

-- Content likes (for posts and watches)
CREATE TABLE content_likes (
    like_id BIGSERIAL PRIMARY KEY,
    post_id UUID,
    watch_id UUID,
    user_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_content_likes_post FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_content_likes_watch FOREIGN KEY (watch_id) REFERENCES watches(watch_id) ON DELETE CASCADE,
    CONSTRAINT fk_content_likes_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_content_likes_content CHECK (post_id IS NOT NULL OR watch_id IS NOT NULL)
);

-- Comment likes
CREATE TABLE content_comment_likes (
    like_id BIGSERIAL PRIMARY KEY,
    comment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_content_comment_likes_comment FOREIGN KEY (comment_id) REFERENCES content_comments(comment_id) ON DELETE CASCADE,
    CONSTRAINT fk_content_comment_likes_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uk_comment_user UNIQUE (comment_id, user_id)
);

CREATE INDEX idx_content_likes_post_id ON content_likes(post_id);
CREATE INDEX idx_content_likes_watch_id ON content_likes(watch_id);
CREATE INDEX idx_content_likes_user_id ON content_likes(user_id);
CREATE INDEX idx_content_likes_created_at ON content_likes(created_at DESC);

CREATE INDEX idx_content_comment_likes_comment_id ON content_comment_likes(comment_id);
CREATE INDEX idx_content_comment_likes_user_id ON content_comment_likes(user_id);

COMMENT ON TABLE content_likes IS 'Likes on posts and watches';
COMMENT ON TABLE content_comment_likes IS 'Likes on comments';
