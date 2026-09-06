-- V12__Create_watches_table.sql
-- Watches (video content) table

CREATE TABLE watches (
    watch_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    video_url TEXT NOT NULL,
    thumbnail_url TEXT,
    duration INTEGER,
    location VARCHAR(255),
    privacy VARCHAR(50) NOT NULL,
    moderation_id UUID,
    like_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    share_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_watches_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_watches_moderation FOREIGN KEY (moderation_id) REFERENCES content_moderations(moderation_id) ON DELETE SET NULL
);

CREATE INDEX idx_watches_user_id ON watches(user_id);
CREATE INDEX idx_watches_privacy ON watches(privacy);
CREATE INDEX idx_watches_created_at ON watches(created_at DESC);
CREATE INDEX idx_watches_view_count ON watches(view_count DESC);
CREATE INDEX idx_watches_moderation_id ON watches(moderation_id);
CREATE INDEX idx_watches_user_created ON watches(user_id, created_at DESC);

COMMENT ON TABLE watches IS 'Video content shared by users (similar to YouTube Shorts/TikTok)';
