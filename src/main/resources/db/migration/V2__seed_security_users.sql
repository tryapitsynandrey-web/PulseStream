-- Create Users Table for Authentication
CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- Seed Credentials (Password for all accounts is 'password')
INSERT INTO users (username, password, role, enabled) VALUES
('admin', '$2a$10$8.Je3j3aXphWlnDThvjM0O.24eMpH3B3sR8tU2lZ5B1oD/p2K.V0G', 'ROLE_ADMIN', true),
('analyst', '$2a$10$8.Je3j3aXphWlnDThvjM0O.24eMpH3B3sR8tU2lZ5B1oD/p2K.V0G', 'ROLE_ANALYST', true),
('user', '$2a$10$8.Je3j3aXphWlnDThvjM0O.24eMpH3B3sR8tU2lZ5B1oD/p2K.V0G', 'ROLE_USER', true);
