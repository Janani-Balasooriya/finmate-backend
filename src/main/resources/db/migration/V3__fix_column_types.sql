-- Fix column types to match JPA entity expectations
ALTER TABLE invitations 
MODIFY COLUMN role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
MODIFY COLUMN status VARCHAR(50) NOT NULL DEFAULT 'PENDING';
