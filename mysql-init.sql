-- MySQL Initialization Script for External Access
-- This runs when MySQL container starts for the first time

-- Create database
CREATE DATABASE IF NOT EXISTS chat;

-- Create users for ALL connection types (localhost, 127.0.0.1, and %)
-- Using mysql_native_password for compatibility

-- Root users
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root';
CREATE USER IF NOT EXISTS 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root';
CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY 'root';

-- Application users
CREATE USER IF NOT EXISTS 'chatuser'@'%' IDENTIFIED WITH mysql_native_password BY 'chatpass';
CREATE USER IF NOT EXISTS 'chatuser'@'localhost' IDENTIFIED WITH mysql_native_password BY 'chatpass';
CREATE USER IF NOT EXISTS 'chatuser'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY 'chatpass';

-- Grant privileges
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION;

GRANT ALL PRIVILEGES ON chat.* TO 'chatuser'@'%';
GRANT ALL PRIVILEGES ON chat.* TO 'chatuser'@'localhost';
GRANT ALL PRIVILEGES ON chat.* TO 'chatuser'@'127.0.0.1';

FLUSH PRIVILEGES;
