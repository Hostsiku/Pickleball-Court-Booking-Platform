CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(20)
);

CREATE TABLE venues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    no_of_courts INT,
    open_time TIME,
    close_time TIME,
    weekend_rate DOUBLE,
    weekday_rate DOUBLE,
    phone_no VARCHAR(20),
    email VARCHAR(100),
    description TEXT,
    address VARCHAR(255),
    owner_id BIGINT
);