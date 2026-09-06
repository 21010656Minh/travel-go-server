-- V22__Create_notifications_table.sql
-- User notifications

CREATE TABLE notifications (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    receiver_id UUID NOT NULL,
    sender_id UUID,
    type VARCHAR(50) NOT NULL,
    content TEXT,
    related_id UUID,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_receiver FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_sender FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_receiver_id ON notifications(receiver_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notifications_receiver_is_read ON notifications(receiver_id, is_read);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_sender_id ON notifications(sender_id);

COMMENT ON TABLE notifications IS 'User notifications for various activities';
