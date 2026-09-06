-- V19__Create_conversation_messages_table.sql
-- Messages in conversations

CREATE TABLE conversation_messages (
    conversation_message_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    content TEXT NOT NULL,
    media_url VARCHAR(255),
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversation_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_messages_sender FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_conversation_messages_conversation_id ON conversation_messages(conversation_id);
CREATE INDEX idx_conversation_messages_sender_id ON conversation_messages(sender_id);
CREATE INDEX idx_conversation_messages_created_at ON conversation_messages(conversation_id, created_at DESC);
CREATE INDEX idx_conversation_messages_status ON conversation_messages(status);

COMMENT ON TABLE conversation_messages IS 'Messages sent in conversations';
