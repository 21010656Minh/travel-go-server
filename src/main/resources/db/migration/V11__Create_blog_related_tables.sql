-- V11__Create_blog_related_tables.sql
-- Blog reviews and blog tags

-- Blog reviews
CREATE TABLE blog_reviews (
    review_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    blog_id UUID NOT NULL,
    user_id UUID NOT NULL,
    content TEXT NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    is_edited BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_blog_reviews_blog FOREIGN KEY (blog_id) REFERENCES blogs(blog_id) ON DELETE CASCADE,
    CONSTRAINT fk_blog_reviews_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Blog tags
CREATE TABLE blog_tags (
    blog_id UUID NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (blog_id, tag_id),
    CONSTRAINT fk_blog_tags_blog FOREIGN KEY (blog_id) REFERENCES blogs(blog_id) ON DELETE CASCADE,
    CONSTRAINT fk_blog_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE
);

CREATE INDEX idx_blog_reviews_blog_id ON blog_reviews(blog_id);
CREATE INDEX idx_blog_reviews_user_id ON blog_reviews(user_id);
CREATE INDEX idx_blog_reviews_rating ON blog_reviews(rating);
CREATE INDEX idx_blog_tags_tag_id ON blog_tags(tag_id);

COMMENT ON TABLE blog_reviews IS 'User reviews and ratings for blogs';
COMMENT ON TABLE blog_tags IS 'Many-to-many relationship between blogs and tags';
