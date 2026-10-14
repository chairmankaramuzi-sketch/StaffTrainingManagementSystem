-- =============================================================================
-- Staff Training Management System (STMS)
-- Database Schema Definition (SQL DDL)
-- Compatible with: SQLite 3 / MySQL 8.x / MariaDB / Apache Derby
-- =============================================================================

-- 1. Employees Table
CREATE TABLE IF NOT EXISTS employees (
    id VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(50),
    department VARCHAR(50) NOT NULL,
    job_title VARCHAR(100) NOT NULL,
    hire_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT 1
);

-- 2. Trainers Table
CREATE TABLE IF NOT EXISTS trainers (
    id VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(50),
    specialization VARCHAR(150) NOT NULL,
    years_of_experience INTEGER NOT NULL,
    hourly_rate DOUBLE NOT NULL,
    is_external BOOLEAN NOT NULL DEFAULT 1
);

-- 3. Training Courses Table (Polymorphic Course Hierarchy)
CREATE TABLE IF NOT EXISTS courses (
    id VARCHAR(50) PRIMARY KEY,
    course_type VARCHAR(50) NOT NULL,      -- 'TECHNICAL', 'SOFT_SKILLS', 'COMPLIANCE'
    title VARCHAR(150) NOT NULL,
    description TEXT,
    duration_hours INTEGER NOT NULL,
    course_level VARCHAR(50) NOT NULL,     -- 'BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXECUTIVE'
    max_capacity INTEGER NOT NULL,
    cost_per_seat DOUBLE NOT NULL,
    specific_attribute VARCHAR(200),       -- techStack / competency / regulatoryStandard
    flag_attribute BOOLEAN NOT NULL DEFAULT 0 -- labRequired / workshopRequired / companyMandatory
);

-- 4. Training Sessions Schedule Table
CREATE TABLE IF NOT EXISTS sessions (
    id VARCHAR(50) PRIMARY KEY,
    course_id VARCHAR(50) NOT NULL,
    course_title VARCHAR(150) NOT NULL,
    trainer_id VARCHAR(50) NOT NULL,
    trainer_name VARCHAR(150) NOT NULL,
    session_date DATE NOT NULL,
    time_slot VARCHAR(50) NOT NULL,
    location VARCHAR(150) NOT NULL,
    max_seats INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,           -- 'SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE SET NULL
);

-- 5. Enrollments & Grading Table
CREATE TABLE IF NOT EXISTS enrollments (
    id VARCHAR(50) PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL,
    employee_id VARCHAR(50) NOT NULL,
    employee_name VARCHAR(150) NOT NULL,
    course_id VARCHAR(50) NOT NULL,
    course_title VARCHAR(150) NOT NULL,
    enrollment_date DATE NOT NULL,
    attended BOOLEAN NOT NULL DEFAULT 0,
    score DOUBLE,
    status VARCHAR(50) NOT NULL,           -- 'REGISTERED', 'ATTENDED', 'PASSED', 'FAILED', 'CANCELLED'
    certificate_id VARCHAR(50),
    notes TEXT,
    FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- 6. Verified Certificates Table
CREATE TABLE IF NOT EXISTS certificates (
    id VARCHAR(50) PRIMARY KEY,
    enrollment_id VARCHAR(50) NOT NULL,
    employee_id VARCHAR(50) NOT NULL,
    employee_name VARCHAR(150) NOT NULL,
    course_id VARCHAR(50) NOT NULL,
    course_title VARCHAR(150) NOT NULL,
    course_category VARCHAR(100) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE,
    verification_code VARCHAR(100) NOT NULL,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Indexes for Query Performance Optimization
CREATE INDEX IF NOT EXISTS idx_emp_dept ON employees(department);
CREATE INDEX IF NOT EXISTS idx_sess_course ON sessions(course_id);
CREATE INDEX IF NOT EXISTS idx_enr_emp ON enrollments(employee_id);
CREATE INDEX IF NOT EXISTS idx_enr_sess ON enrollments(session_id);
CREATE INDEX IF NOT EXISTS idx_cert_emp ON certificates(employee_id);
