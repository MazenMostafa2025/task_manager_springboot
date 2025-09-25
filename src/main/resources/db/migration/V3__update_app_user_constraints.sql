-- Make username and email unique and required
ALTER TABLE app_user 
    ALTER COLUMN username SET NOT NULL,
    ALTER COLUMN full_name SET NOT NULL,
    ALTER COLUMN role SET NOT NULL,
    ADD CONSTRAINT uk_username UNIQUE (username);

-- Add email column if it doesn't exist
ALTER TABLE app_user 
    ADD COLUMN IF NOT EXISTS email VARCHAR(255);

-- Set a default email for existing users
UPDATE app_user 
SET email = username || '@example.com' 
WHERE email IS NULL;

-- Make email required and unique
ALTER TABLE app_user 
    ALTER COLUMN email SET NOT NULL,
    ADD CONSTRAINT uk_email UNIQUE (email);

-- Add index on role for faster lookups
CREATE INDEX IF NOT EXISTS idx_app_user_role ON app_user(role);
