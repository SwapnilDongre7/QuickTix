-- QuickTix Database Initialization Script
-- This script creates all required databases

CREATE DATABASE IF NOT EXISTS identity_service;
CREATE DATABASE IF NOT EXISTS catalogue_service;
CREATE DATABASE IF NOT EXISTS theatre_service;
CREATE DATABASE IF NOT EXISTS booking_service;
CREATE DATABASE IF NOT EXISTS payment_service;

-- Grant privileges
GRANT ALL PRIVILEGES ON identity_service.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON catalogue_service.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON theatre_service.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON booking_service.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON payment_service.* TO 'root'@'%';


FLUSH PRIVILEGES;
