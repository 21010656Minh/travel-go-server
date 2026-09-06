-- V23__Create_fulltext_indexes.sql
-- PostgreSQL fulltext search indexes for multiple entities

-- ============================================
-- CONVERSATIONS - Fulltext search indexes
-- ============================================
-- Create GIN index for conversation_name fulltext search
CREATE INDEX IF NOT EXISTS idx_conversations_name_fulltext 
ON conversations USING GIN (to_tsvector('simple', COALESCE(conversation_name, '')));

-- Create GIN index for last_message fulltext search
CREATE INDEX IF NOT EXISTS idx_conversations_last_message_fulltext 
ON conversations USING GIN (to_tsvector('simple', COALESCE(last_message, '')));

-- Create composite index for better query performance
CREATE INDEX IF NOT EXISTS idx_conversations_user_type 
ON conversation_members (user_id, conversation_id);

COMMENT ON INDEX idx_conversations_name_fulltext IS 'GIN index for fulltext search on conversation names';
COMMENT ON INDEX idx_conversations_last_message_fulltext IS 'GIN index for fulltext search on last messages';
COMMENT ON INDEX idx_conversations_user_type IS 'Index for filtering conversations by user and type';

-- ============================================
-- USER PROFILES - Fulltext search indexes
-- ============================================
-- Create GIN index for full_name fulltext search
CREATE INDEX IF NOT EXISTS idx_user_profiles_fullname_fulltext 
ON user_profiles USING GIN (to_tsvector('simple', COALESCE(full_name, '')));

-- Create GIN index for location fulltext search
CREATE INDEX IF NOT EXISTS idx_user_profiles_location_fulltext 
ON user_profiles USING GIN (to_tsvector('simple', COALESCE(location, '')));

-- Create GIN index for about fulltext search
CREATE INDEX IF NOT EXISTS idx_user_profiles_about_fulltext 
ON user_profiles USING GIN (to_tsvector('simple', COALESCE(about, '')));

COMMENT ON INDEX idx_user_profiles_fullname_fulltext IS 'GIN index for fulltext search on user full names';
COMMENT ON INDEX idx_user_profiles_location_fulltext IS 'GIN index for fulltext search on user locations';
COMMENT ON INDEX idx_user_profiles_about_fulltext IS 'GIN index for fulltext search on user about section';

-- ============================================
-- USERS - Fulltext search indexes
-- ============================================
-- Create GIN index for user_name fulltext search
CREATE INDEX IF NOT EXISTS idx_users_username_fulltext 
ON users USING GIN (to_tsvector('simple', COALESCE(user_name, '')));

-- Create GIN index for email fulltext search
CREATE INDEX IF NOT EXISTS idx_users_email_fulltext 
ON users USING GIN (to_tsvector('simple', COALESCE(email, '')));

COMMENT ON INDEX idx_users_username_fulltext IS 'GIN index for fulltext search on usernames';
COMMENT ON INDEX idx_users_email_fulltext IS 'GIN index for fulltext search on emails';

-- ============================================
-- BLOGS - Fulltext search indexes
-- ============================================
-- Create GIN index for blog title fulltext search
CREATE INDEX IF NOT EXISTS idx_blogs_title_fulltext 
ON blogs USING GIN (to_tsvector('simple', COALESCE(title, '')));

-- Create GIN index for blog content fulltext search
CREATE INDEX IF NOT EXISTS idx_blogs_content_fulltext 
ON blogs USING GIN (to_tsvector('simple', COALESCE(content, '')));

-- Create GIN index for blog description fulltext search
CREATE INDEX IF NOT EXISTS idx_blogs_description_fulltext 
ON blogs USING GIN (to_tsvector('simple', COALESCE(description, '')));

-- Create GIN index for blog location fulltext search
CREATE INDEX IF NOT EXISTS idx_blogs_location_fulltext 
ON blogs USING GIN (to_tsvector('simple', COALESCE(location, '')));

COMMENT ON INDEX idx_blogs_title_fulltext IS 'GIN index for fulltext search on blog titles';
COMMENT ON INDEX idx_blogs_content_fulltext IS 'GIN index for fulltext search on blog content';
COMMENT ON INDEX idx_blogs_description_fulltext IS 'GIN index for fulltext search on blog descriptions';
COMMENT ON INDEX idx_blogs_location_fulltext IS 'GIN index for fulltext search on blog locations';

-- ============================================
-- GROUPS - Fulltext search indexes
-- ============================================
-- Create GIN index for group_name fulltext search
CREATE INDEX IF NOT EXISTS idx_groups_name_fulltext 
ON groups USING GIN (to_tsvector('simple', COALESCE(group_name, '')));

-- Create GIN index for group_description fulltext search
CREATE INDEX IF NOT EXISTS idx_groups_description_fulltext 
ON groups USING GIN (to_tsvector('simple', COALESCE(group_description, '')));

-- Create GIN index for group location fulltext search
CREATE INDEX IF NOT EXISTS idx_groups_location_fulltext 
ON groups USING GIN (to_tsvector('simple', COALESCE(location, '')));

-- Create GIN index for group tags fulltext search
CREATE INDEX IF NOT EXISTS idx_groups_tags_fulltext 
ON groups USING GIN (to_tsvector('simple', COALESCE(tags, '')));

COMMENT ON INDEX idx_groups_name_fulltext IS 'GIN index for fulltext search on group names';
COMMENT ON INDEX idx_groups_description_fulltext IS 'GIN index for fulltext search on group descriptions';
COMMENT ON INDEX idx_groups_location_fulltext IS 'GIN index for fulltext search on group locations';
COMMENT ON INDEX idx_groups_tags_fulltext IS 'GIN index for fulltext search on group tags';
