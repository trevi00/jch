-- Initialize admin user for development and testing
MERGE INTO users (
    email,
    password,
    name,
    user_type,
    email_verified,
    employment_status,
    admin_converted_at,
    created_at,
    updated_at
) VALUES (
    'admin@jbd.com',
    '$2a$10$K2o8fzPJGT1nN9BdVzJGrOGqQ7YvN8YJPa9XoFIpT2M9l3qFz6yFe', -- password: admin123
    'System Administrator',
    'ADMIN',
    true,
    'JOB_SEEKING',
    NOW(),
    NOW(),
    NOW()
);