-- V7__Create_group_members_table.sql
-- Group membership table

CREATE TABLE group_members (
    group_member_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(50),
    role VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES groups(group_id) ON DELETE CASCADE,
    CONSTRAINT fk_group_members_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_group_members_group_id ON group_members(group_id);
CREATE INDEX idx_group_members_user_id ON group_members(user_id);
CREATE INDEX idx_group_members_status ON group_members(status);
CREATE INDEX idx_group_members_role ON group_members(role);

COMMENT ON TABLE group_members IS 'Members of groups with roles and status';
