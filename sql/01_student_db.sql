-- psql -U postgres -d student_db -f sql/01_student_db.sql
CREATE TABLE IF NOT EXISTS students (
    id              BIGSERIAL    PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    major           VARCHAR(100),
    enrollment_year INTEGER      NOT NULL CHECK (enrollment_year BETWEEN 1900 AND 2100)
);

INSERT INTO students (first_name, last_name, email, major, enrollment_year) VALUES
    ('Aruzhan', 'Seitkali', 'aruzhan.seitkali@university.kz', 'Computer Science', 2023),
    ('Daniyar', 'Bekov',    'daniyar.bekov@university.kz',    'Information Systems', 2022),
    ('Madina',  'Nurlanova','madina.nurlanova@university.kz', 'Software Engineering', 2024)
ON CONFLICT (email) DO NOTHING;
