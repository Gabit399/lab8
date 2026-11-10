-- psql -U postgres -d course_db -f sql/02_course_db.sql
CREATE TABLE IF NOT EXISTS courses (
    id          BIGSERIAL    PRIMARY KEY,
    course_name VARCHAR(150) NOT NULL,
    course_code VARCHAR(20)  NOT NULL UNIQUE,
    credits     INTEGER      NOT NULL CHECK (credits BETWEEN 1 AND 30),
    instructor  VARCHAR(150)
);

INSERT INTO courses (course_name, course_code, credits, instructor) VALUES
    ('Web Applications in Java Spring', 'CSE301', 5, 'Dr. Akhmetov'),
    ('Database Systems',                'CSE220', 5, 'Dr. Iskakova'),
    ('Algorithms and Data Structures',  'CSE210', 6, 'Dr. Omarov')
ON CONFLICT (course_code) DO NOTHING;
