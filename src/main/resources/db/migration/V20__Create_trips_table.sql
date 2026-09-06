-- V20__Create_trips_table.sql
-- Trips associated with conversations

CREATE TABLE trips (
    trip_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL,
    trip_name VARCHAR(255) NOT NULL,
    trip_description TEXT,
    cover_image_url TEXT,
    destination VARCHAR(255),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    budget DECIMAL(15,2),
    status VARCHAR(50) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trips_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    CONSTRAINT fk_trips_created_by FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_trips_dates CHECK (start_date <= end_date)
);

CREATE INDEX idx_trips_conversation_id ON trips(conversation_id);
CREATE INDEX idx_trips_created_by ON trips(created_by);
CREATE INDEX idx_trips_status ON trips(status);
CREATE INDEX idx_trips_start_date ON trips(start_date);
CREATE INDEX idx_trips_created_at ON trips(created_at DESC);

COMMENT ON TABLE trips IS 'Group travel plans associated with conversations';
