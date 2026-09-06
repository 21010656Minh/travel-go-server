-- V18__Create_conversation_members_table.sql
-- Conversation membership

CREATE TABLE conversation_members (
    conversation_member_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversation_members_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_members_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_conversation_members_conversation_id ON conversation_members(conversation_id);
CREATE INDEX idx_conversation_members_user_id ON conversation_members(user_id);
CREATE INDEX idx_conversation_members_user_conversation ON conversation_members(user_id, conversation_id);
CREATE INDEX idx_conversation_members_role ON conversation_members(role);

COMMENT ON TABLE conversation_members IS 'Members of conversations with roles';
