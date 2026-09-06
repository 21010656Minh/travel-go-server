-- V4__Create_content_moderations_table.sql
-- Content moderation table - referenced by posts, groups, blogs, watches

CREATE TABLE content_moderations (
    moderation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_type VARCHAR(50) NOT NULL,
    content_id UUID NOT NULL,
    moderated_by_user_id UUID NOT NULL,
    moderation_reason TEXT NOT NULL,
    is_active BOOLEAN DEFAULT true,
    moderated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unlocked_at TIMESTAMP,
    CONSTRAINT fk_content_moderations_user FOREIGN KEY (moderated_by_user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_content_moderations_type_id ON content_moderations(content_type, content_id);
CREATE INDEX idx_content_moderations_is_active ON content_moderations(is_active) WHERE is_active = true;
CREATE INDEX idx_content_moderations_moderated_by ON content_moderations(moderated_by_user_id);
CREATE INDEX idx_content_moderations_moderated_at ON content_moderations(moderated_at DESC);

COMMENT ON TABLE content_moderations IS 'Content moderation records for posts, watches, groups, and blogs';
