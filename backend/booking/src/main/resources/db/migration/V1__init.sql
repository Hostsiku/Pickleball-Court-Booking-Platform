-- CREATE TABLE users (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT,
--     name VARCHAR(100),
--     email VARCHAR(100) UNIQUE,
--     password VARCHAR(255),
--     role VARCHAR(20)
-- );

-- CREATE TABLE venues (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT,
--     name VARCHAR(100),
--     no_of_courts INT,
--     open_time TIME,
--     close_time TIME,
--     weekend_rate DOUBLE,
--     weekday_rate DOUBLE,
--     phone_no VARCHAR(20),
--     email VARCHAR(100),
--     description TEXT,
--     address VARCHAR(255),
--     owner_id BIGINT
-- );



-- =========================
-- USERS TABLE
-- =========================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(20)
);

-- =========================
-- VENUES TABLE
-- =========================
CREATE TABLE IF NOT EXISTS venues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT,
    name VARCHAR(100),
    no_of_courts INT,
    open_time VARCHAR(10),
    close_time VARCHAR(10),
    weekend_rate INT,
    weekday_rate INT,
    phone_no VARCHAR(20),
    email VARCHAR(100),
    description TEXT,
    address VARCHAR(255)
);

-- =========================
-- VENUE PHOTOS TABLE (IMPORTANT 🔥)
-- =========================
CREATE TABLE IF NOT EXISTS venue_photos (
    venue_id BIGINT,
    photo_url VARCHAR(500),

    FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE
);

-- =========================
-- BOOKINGS TABLE
-- =========================
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    venue_id BIGINT,
    court_id INT,

    booking_date VARCHAR(20),
    start_time VARCHAR(10),
    end_time VARCHAR(10),

    user_id BIGINT,

    status VARCHAR(20),
    created_at DATETIME,

    -- 🔥 UNIQUE CONSTRAINT (VERY IMPORTANT)
    UNIQUE (venue_id, court_id, booking_date, start_time),

    -- 🔥 FOREIGN KEYS (GOOD PRACTICE)
    FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =========================
-- INDEXES (PERFORMANCE 🔥)
-- =========================
CREATE INDEX idx_booking_lookup 
ON bookings (venue_id, booking_date, court_id);