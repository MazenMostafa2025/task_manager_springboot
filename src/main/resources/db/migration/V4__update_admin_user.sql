-- Update the admin user with email if it exists
UPDATE app_user 
SET email = 'admin@example.com'
WHERE username = 'admin' AND email IS NULL;
