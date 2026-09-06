-- V8__Create_posts_table.sql
-- Posts table

CREATE TABLE posts (
    post_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    content TEXT NOT NULL,
    shared_post_id UUID,
    group_id UUID,
    location VARCHAR(255),
    like_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    share_count INTEGER DEFAULT 0,
    is_share BOOLEAN DEFAULT false,
    post_type VARCHAR(50) NOT NULL,
    privacy VARCHAR(50) NOT NULL,
    moderation_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_posts_shared_post FOREIGN KEY (shared_post_id) REFERENCES posts(post_id) ON DELETE SET NULL,
    CONSTRAINT fk_posts_group FOREIGN KEY (group_id) REFERENCES groups(group_id) ON DELETE CASCADE,
    CONSTRAINT fk_posts_moderation FOREIGN KEY (moderation_id) REFERENCES content_moderations(moderation_id) ON DELETE SET NULL
);

CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_group_id ON posts(group_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX idx_posts_privacy ON posts(privacy);
CREATE INDEX idx_posts_post_type ON posts(post_type);
CREATE INDEX idx_posts_shared_post_id ON posts(shared_post_id);
CREATE INDEX idx_posts_like_count ON posts(like_count DESC);
CREATE INDEX idx_posts_moderation_id ON posts(moderation_id);
CREATE INDEX idx_posts_user_created ON posts(user_id, created_at DESC);
CREATE INDEX idx_posts_group_created ON posts(group_id, created_at DESC) WHERE group_id IS NOT NULL;

COMMENT ON TABLE posts IS 'User posts with support for sharing and groups';
