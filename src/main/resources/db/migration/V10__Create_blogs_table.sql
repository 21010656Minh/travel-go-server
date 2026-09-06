-- V10__Create_blogs_table.sql
-- Blogs table

CREATE TABLE blogs (
    blog_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    thumbnail_url TEXT,
    description VARCHAR(1000),
    location VARCHAR(255),
    view_count BIGINT DEFAULT 0,
    average_rating DOUBLE PRECISION DEFAULT 0.0,
    total_ratings INTEGER DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    is_featured BOOLEAN DEFAULT false,
    moderation_id UUID,
    reading_time INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    CONSTRAINT fk_blogs_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_blogs_moderation FOREIGN KEY (moderation_id) REFERENCES content_moderations(moderation_id) ON DELETE SET NULL
);

CREATE INDEX idx_blogs_user_id ON blogs(user_id);
CREATE INDEX idx_blogs_status ON blogs(status);
CREATE INDEX idx_blogs_created_at ON blogs(created_at DESC);
CREATE INDEX idx_blogs_is_featured ON blogs(is_featured) WHERE is_featured = true;
CREATE INDEX idx_blogs_moderation_id ON blogs(moderation_id);
CREATE INDEX idx_blogs_user_status ON blogs(user_id, status);

COMMENT ON TABLE blogs IS 'Blog posts created by users';
