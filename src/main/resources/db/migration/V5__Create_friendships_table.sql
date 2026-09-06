-- V5__Create_friendships_table.sql
-- Friendships between users

CREATE TABLE friendships (
    friendship_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id UUID NOT NULL,
    receiver_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_friendships_requester FOREIGN KEY (requester_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_receiver FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_friendships_requester_id ON friendships(requester_id);
CREATE INDEX idx_friendships_receiver_id ON friendships(receiver_id);
CREATE INDEX idx_friendships_status ON friendships(status);
CREATE INDEX idx_friendships_requester_receiver ON friendships(requester_id, receiver_id);
CREATE INDEX idx_friendships_receiver_requester ON friendships(receiver_id, requester_id);
CREATE INDEX idx_friendships_status_created ON friendships(status, created_at DESC);

COMMENT ON TABLE friendships IS 'Friend relationships between users';
