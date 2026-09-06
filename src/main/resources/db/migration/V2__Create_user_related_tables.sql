-- V2__Create_user_related_tables.sql
-- Tables that depend on users table

-- User profiles
CREATE TABLE user_profiles (
    user_profile_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    full_name VARCHAR(255),
    location VARCHAR(255),
    gender VARCHAR(10),
    dob DATE,
    about TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- User credentials
CREATE TABLE user_credentials (
    credential_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255),
    password VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_credentials_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uk_provider_user UNIQUE (provider, provider_user_id)
);

-- Password reset tokens
CREATE TABLE password_reset_tokens (
    reset_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    reset_token VARCHAR(255) NOT NULL UNIQUE,
    expired_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_user_profiles_location ON user_profiles(location);
CREATE INDEX idx_user_profiles_gender ON user_profiles(gender);

CREATE INDEX idx_user_credentials_user_id ON user_credentials(user_id);
CREATE INDEX idx_user_credentials_provider ON user_credentials(provider);
CREATE INDEX idx_user_credentials_provider_user_id ON user_credentials(provider_user_id);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_expired_at ON password_reset_tokens(expired_at);

COMMENT ON TABLE user_profiles IS 'Extended user profile information';
COMMENT ON TABLE user_credentials IS 'User authentication credentials for multiple providers';
COMMENT ON TABLE password_reset_tokens IS 'Tokens for password reset functionality';
