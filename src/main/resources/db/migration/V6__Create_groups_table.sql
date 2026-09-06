-- V6__Create_groups_table.sql
-- Groups table

CREATE TABLE groups (
    group_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_name VARCHAR(255) NOT NULL,
    group_description TEXT,
    cover_image_url TEXT,
    member_count INTEGER DEFAULT 0,
    privacy VARCHAR(50),
    moderation_id UUID,
    tags VARCHAR(255),
    location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP,
    CONSTRAINT fk_groups_moderation FOREIGN KEY (moderation_id) REFERENCES content_moderations(moderation_id) ON DELETE SET NULL
);

CREATE INDEX idx_groups_privacy ON groups(privacy);
CREATE INDEX idx_groups_created_at ON groups(created_at DESC);
CREATE INDEX idx_groups_last_activity_at ON groups(last_activity_at DESC);
CREATE INDEX idx_groups_member_count ON groups(member_count DESC);
CREATE INDEX idx_groups_moderation_id ON groups(moderation_id);

COMMENT ON TABLE groups IS 'User groups for community discussions';
