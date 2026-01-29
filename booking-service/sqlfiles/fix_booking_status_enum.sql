-- Fix for "Data truncated" error when setting status to EXPIRED
-- Run this script in your MySQL database to update the enum definition

ALTER TABLE bookings 
MODIFY COLUMN status ENUM('INITIATED','CONFIRMED','CANCELLED','EXPIRED') NOT NULL;
