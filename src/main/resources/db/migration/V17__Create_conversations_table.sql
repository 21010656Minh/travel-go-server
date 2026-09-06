-- V17__Create_conversations_table.sql
-- Conversations for chat functionality

CREATE TABLE conversations (
    conversation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_name VARCHAR(255),
    conversation_avatar TEXT,
    type VARCHAR(50) NOT NULL,
    last_message TEXT,
    last_active_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_conversations_type ON conversations(type);
CREATE INDEX idx_conversations_last_active_at ON conversations(last_active_at DESC NULLS LAST);

COMMENT ON TABLE conversations IS 'Chat conversations (private and group)';
