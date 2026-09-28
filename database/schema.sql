-- =============================================================================
-- Smart Campus Issue Reporter — Database Schema
-- Database: smart_campus
-- =============================================================================

CREATE DATABASE IF NOT EXISTS smart_campus
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_campus;

-- Issues table
DROP TABLE IF EXISTS issues;
CREATE TABLE issues (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    issue_code       VARCHAR(20)  NOT NULL,
    reported_by      VARCHAR(100) NOT NULL,
    category         ENUM('ELECTRICAL','PLUMBING','CLASSROOM','WIFI','CLEANLINESS','SAFETY','OTHER') NOT NULL,
    location         VARCHAR(200) NOT NULL,
    description      TEXT         NOT NULL,
    affected_users   INT          NOT NULL DEFAULT 1,
    safety_impact    TINYINT(1)   NOT NULL DEFAULT 0,
    priority         ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL,
    priority_score   INT,
    status           ENUM('OPEN','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
    assigned_to      VARCHAR(100),
    resolution_notes TEXT,
    reported_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at      DATETIME,

    CONSTRAINT uq_issue_code UNIQUE (issue_code),
    CONSTRAINT chk_affected_users CHECK (affected_users >= 1 AND affected_users <= 10000)
);

-- Performance indexes
CREATE INDEX idx_status       ON issues (status);
CREATE INDEX idx_priority     ON issues (priority);
CREATE INDEX idx_category     ON issues (category);
CREATE INDEX idx_reported_by  ON issues (reported_by);
CREATE INDEX idx_reported_at  ON issues (reported_at);

-- =============================================================================
-- Sample Data — realistic campus issues for dashboard demonstration
-- =============================================================================

INSERT INTO issues (issue_code, reported_by, category, location, description,
                    affected_users, safety_impact, priority, priority_score, status,
                    assigned_to, resolution_notes, reported_at, updated_at, resolved_at)
VALUES
-- Critical safety issue
('SCI-20260920-1001', 'Arun Kumar',    'SAFETY',      'Block A, Staircase 2',
 'Loose railing on 3rd floor staircase — risk of falling. Multiple students have reported wobbling.',
 120, 1, 'CRITICAL', 85, 'IN_PROGRESS', 'Maintenance Team Alpha', NULL,
 NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 1 DAY, NULL),

-- High electrical issue
('SCI-20260921-2034', 'Priya Sharma',  'ELECTRICAL',  'Computer Lab 3, Block B',
 'Power sockets near workstations 12–15 are sparking. Lab is partially unusable.',
 45, 1, 'CRITICAL', 80, 'ASSIGNED', 'Electrician Ravi', NULL,
 NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 2 DAY, NULL),

-- Medium wifi issue
('SCI-20260922-3210', 'Karthik Rajan', 'WIFI',        'Library, Reading Hall',
 'Wi-Fi access points are completely offline in the main reading section. Students cannot access e-resources.',
 80, 0, 'HIGH', 55, 'IN_PROGRESS', 'Network Admin Team', NULL,
 NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 1 DAY, NULL),

-- Resolved plumbing issue
('SCI-20260923-4452', 'Meena Reddy',   'PLUMBING',    'Girls Hostel Block C, Floor 2',
 'Water pipe burst near bathroom — flooding corridor. Emergency repair required.',
 35, 1, 'CRITICAL', 78, 'RESOLVED', 'Plumbing Crew B', 'Burst pipe replaced and corridor dried. Water supply restored.',
 NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),

-- Open classroom issue
('SCI-20260924-5678', 'Rahul Verma',   'CLASSROOM',   'Seminar Hall 1, Admin Block',
 'Projector lamp burned out. Seminar presentations are affected for all classes scheduled this week.',
 55, 0, 'HIGH', 60, 'OPEN', NULL, NULL,
 NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, NULL),

-- Low priority cleanliness
('SCI-20260925-6890', 'Divya Nair',    'CLEANLINESS', 'Canteen, Ground Floor',
 'Waste bins near entrance are overflowing and not cleared since Monday. Unpleasant odour spreading.',
 30, 0, 'MEDIUM', 32, 'ASSIGNED', 'Sanitation Team', NULL,
 NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 1 DAY, NULL),

-- Open wifi issue
('SCI-20260926-7123', 'Suresh Pillai', 'WIFI',        'Hostel Block D, All Floors',
 'Internet connectivity is extremely slow (below 1 Mbps) making video lectures impossible to stream.',
 150, 0, 'CRITICAL', 77, 'OPEN', NULL, NULL,
 NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, NULL),

-- Closed old issue
('SCI-20260915-8001', 'Ananya Singh',  'ELECTRICAL',  'Classroom 205, Block C',
 'Ceiling fan stopped working. Room temperature is uncomfortable during afternoon sessions.',
 30, 0, 'MEDIUM', 35, 'CLOSED', 'Electrician Ravi', 'Fan capacitor replaced. Fan is operational.',
 NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 9 DAY),

-- Recent low priority
('SCI-20260927-9345', 'Vikram Iyer',   'OTHER',       'Parking Lot B',
 'Speed bumps in parking lot are faded and barely visible, especially at night.',
 10, 0, 'LOW', 15, 'OPEN', NULL, NULL,
 NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NULL),

-- Recent medium priority
('SCI-20260928-0567', 'Lakshmi Priya', 'CLASSROOM',   'Lab 7, Electronics Block',
 'Air conditioning unit making loud grinding noise and emitting warm air instead of cold.',
 25, 0, 'MEDIUM', 38, 'OPEN', NULL, NULL,
 NOW(), NOW(), NULL);
