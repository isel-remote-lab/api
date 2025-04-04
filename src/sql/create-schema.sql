START TRANSACTION;

CREATE SCHEMA IF NOT EXISTS rl;

DROP TABLE IF EXISTS rl.user CASCADE;

DROP TABLE IF EXISTS rl.token CASCADE;

DROP TABLE IF EXISTS rl.group CASCADE;

DROP TABLE IF EXISTS rl.laboratory CASCADE;

DROP TABLE IF EXISTS rl.lab_waiting_queue CASCADE;

DROP TABLE IF EXISTS rl.lab_session CASCADE;

DROP TABLE IF EXISTS rl.group_laboratory CASCADE;

CREATE TABLE
    rl.user (
        id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        o_auth_id VARCHAR(255) NOT NULL UNIQUE,
        role CHAR(1) NOT NULL,
        username VARCHAR(255) NOT NULL,
        email VARCHAR(255) NOT NULL UNIQUE,
        created_at TIMESTAMPTZ NOT NULL
    );

CREATE TABLE
    rl.token (
        token_validation VARCHAR(255) NOT NULL,
        user_id INT NOT NULL REFERENCES rl.user (id),
        created_at TIMESTAMPTZ NOT NULL,
        last_used_at TIMESTAMPTZ NOT NULL,
        PRIMARY KEY (token_validation, user_id)
    );

-- Groups can be general groups, classes or student groups
CREATE TABLE
    rl.group (
        id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        group_name VARCHAR(255) NOT NULL,
        group_description TEXT,
        created_at TIMESTAMPTZ NOT NULL,
        owner_id INT NOT NULL REFERENCES rl.user (id)
    );

CREATE TABLE
    rl.user_group (
        user_id INT NOT NULL REFERENCES rl.user (id),
        group_id INT NOT NULL REFERENCES rl.group (id),
        PRIMARY KEY (user_id, group_id)
    );

CREATE TABLE
    rl.laboratory (
        id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        lab_name VARCHAR(255) NOT NULL,
        lab_duration INT NOT NULL,
        lab_queue_limit INT NOT NULL,
        created_at TIMESTAMPTZ NOT NULL,
        owner_id INT NOT NULL REFERENCES rl.user (id)
    );

-- Waiting queue for a lab
CREATE TABLE
    rl.lab_waiting_queue (
        id INT GENERATED ALWAYS AS IDENTITY, -- For the order of the queue
        user_id INT NOT NULL REFERENCES rl.user (id),
        lab_id INT NOT NULL REFERENCES rl.laboratory (id),
        PRIMARY KEY (id, user_id, lab_id)
    );

CREATE TABLE
    rl.lab_session (
        id INT GENERATED ALWAYS AS IDENTITY,
        lab_id INT NOT NULL REFERENCES rl.laboratory (id),
        owner_id INT NOT NULL REFERENCES rl.user (id),
        start_time TIMESTAMPTZ,
        end_time TIMESTAMPTZ,
        PRIMARY KEY (id, lab_id, owner_id)
    );

CREATE TABLE
    rl.group_laboratory (
        group_id INT NOT NULL REFERENCES rl.group (id),
        lab_id INT NOT NULL REFERENCES rl.laboratory (id),
        PRIMARY KEY (group_id, lab_id)
    );

CREATE TABLE
    rl.app_invite (
        id INT GENERATED ALWAYS AS IDENTITY,
        invite_code VARCHAR(255) NOT NULL,
        owner_id INT NOT NULL REFERENCES rl.user (id),
        created_at TIMESTAMPTZ NOT NULL,
        last_used_at TIMESTAMPTZ NOT NULL,
        group_id INT NOT NULL REFERENCES rl.group (id),
        PRIMARY KEY (id, owner_id)
    );

CREATE TABLE
    rl.hardware (
        id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        hw_name VARCHAR(255) NOT NULL,
        hw_serial_num VARCHAR(255) NOT NULL,
        status CHAR(1) NOT NULL,
        mac_address VARCHAR(255),
        ip_address VARCHAR(255),
        created_at TIMESTAMPTZ NOT NULL
    );

CREATE TABLE
    rl.hardware_laboratory (
        hw_id INT NOT NULL REFERENCES rl.hardware (id),
        lab_id INT NOT NULL REFERENCES rl.laboratory (id),
        PRIMARY KEY (hw_id, lab_id)
    );

COMMIT;