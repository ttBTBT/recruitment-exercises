-- MySQL 8.4 schema for questions 10 and 11. Boot uses ddl-auto=validate.
CREATE TABLE IF NOT EXISTS app_users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  registered_at DATETIME(6) NOT NULL,
  CONSTRAINT uk_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS events (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(120) NOT NULL,
  description VARCHAR(5000) NOT NULL,
  location VARCHAR(200) NOT NULL,
  event_time DATETIME(6) NOT NULL,
  capacity INT NOT NULL,
  registered_count INT NOT NULL DEFAULT 0,
  cancelled BIT NOT NULL DEFAULT 0,
  organizer_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_event_organizer FOREIGN KEY (organizer_id) REFERENCES app_users(id),
  CONSTRAINT chk_capacity CHECK (capacity > 0),
  CONSTRAINT chk_registered_count CHECK (registered_count >= 0 AND registered_count <= capacity),
  INDEX idx_event_time (event_time),
  INDEX idx_organizer (organizer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS registrations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  registered_at DATETIME(6) NOT NULL,
  CONSTRAINT uk_event_user UNIQUE (event_id, user_id),
  CONSTRAINT fk_registration_event FOREIGN KEY (event_id) REFERENCES events(id),
  CONSTRAINT fk_registration_user FOREIGN KEY (user_id) REFERENCES app_users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
