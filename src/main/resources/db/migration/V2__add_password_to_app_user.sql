-- Add password column to app_user table
ALTER TABLE app_user 
ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT 'default_password';

-- Update existing users with a default hashed password (you should update this in production)
-- The default password is 'password' hashed with BCrypt
UPDATE app_user 
SET password = '$2a$10$xLFtBIXGtYv/VHwkZ.lkv.R9QZ2mI5Z5F8tMpWdUWqsYpdlGJPxxO'
WHERE password = 'default_password';

-- Make the column not null after setting default values
ALTER TABLE app_user 
ALTER COLUMN password SET NOT NULL;
