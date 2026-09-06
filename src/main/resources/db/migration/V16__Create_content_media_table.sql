-- V16__Create_content_media_table.sql
-- Media files for posts and blogs

CREATE TABLE content_media (
    media_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID,
    blog_id UUID,
    url VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_content_media_post FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_content_media_blog FOREIGN KEY (blog_id) REFERENCES blogs(blog_id) ON DELETE CASCADE,
    CONSTRAINT chk_content_media_content CHECK (post_id IS NOT NULL OR blog_id IS NOT NULL)
);

CREATE INDEX idx_content_media_post_id ON content_media(post_id);
CREATE INDEX idx_content_media_blog_id ON content_media(blog_id);
CREATE INDEX idx_content_media_type ON content_media(type);
CREATE INDEX idx_content_media_uploaded_at ON content_media(uploaded_at DESC);

COMMENT ON TABLE content_media IS 'Media files (images/videos) attached to posts and blogs';
