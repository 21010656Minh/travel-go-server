-- V24__Add_conversation_id_to_content_media.sql
-- Add conversation_id field to support chat media storage

-- Drop old constraint that required post_id OR blog_id
ALTER TABLE content_media DROP CONSTRAINT IF EXISTS chk_content_media_content;

-- Add conversation_id column
ALTER TABLE content_media ADD COLUMN conversation_id UUID;

-- Add foreign key to conversations table
ALTER TABLE content_media ADD CONSTRAINT fk_content_media_conversation 
    FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE;

-- Add new constraint: must have post_id, blog_id, OR conversation_id
ALTER TABLE content_media ADD CONSTRAINT chk_content_media_content 
    CHECK (post_id IS NOT NULL OR blog_id IS NOT NULL OR conversation_id IS NOT NULL);

-- Add index for conversation_id lookups
CREATE INDEX idx_content_media_conversation_id ON content_media(conversation_id);
CREATE INDEX idx_content_media_conversation_uploaded ON content_media(conversation_id, uploaded_at DESC);

COMMENT ON COLUMN content_media.conversation_id IS 'Links media to chat conversations';
