-- V21__Create_trip_schedules_table.sql
-- Detailed schedules for trips

CREATE TABLE trip_schedules (
    trip_schedule_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(500),
    schedule_date TIMESTAMP NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    activity_type VARCHAR(50),
    estimated_cost DECIMAL(15,2),
    notes TEXT,
    order_index INTEGER DEFAULT 0,
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trip_schedules_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_schedules_created_by FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_trip_schedules_time CHECK (start_time IS NULL OR end_time IS NULL OR start_time <= end_time)
);

CREATE INDEX idx_trip_schedules_trip_id ON trip_schedules(trip_id);
CREATE INDEX idx_trip_schedules_schedule_date ON trip_schedules(schedule_date);
CREATE INDEX idx_trip_schedules_order_index ON trip_schedules(trip_id, order_index);

COMMENT ON TABLE trip_schedules IS 'Detailed schedule items for trips';
