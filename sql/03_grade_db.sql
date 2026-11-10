-- psql -U postgres -d grade_db -f sql/03_grade_db.sql
-- student_id and course_id are LOGICAL references to other databases.
-- PostgreSQL cannot enforce foreign keys across databases; GradeService checks them instead.
CREATE TABLE IF NOT EXISTS grades (
    id           BIGSERIAL        PRIMARY KEY,
    student_id   BIGINT           NOT NULL,
    course_id    BIGINT           NOT NULL,
    score        DOUBLE PRECISION NOT NULL CHECK (score BETWEEN 0 AND 100),
    letter_grade VARCHAR(2)       NOT NULL,
    graded_at    TIMESTAMP        NOT NULL DEFAULT now(),
    CONSTRAINT uk_grades_student_course UNIQUE (student_id, course_id)
);

CREATE INDEX IF NOT EXISTS idx_grades_student_id ON grades (student_id);
CREATE INDEX IF NOT EXISTS idx_grades_course_id  ON grades (course_id);

-- Sample grades; ids match the sample rows in 01_student_db.sql and 02_course_db.sql
INSERT INTO grades (student_id, course_id, score, letter_grade) VALUES
    (1, 1, 94, 'A'),
    (1, 2, 81, 'B'),
    (1, 3, 77, 'C'),
    (2, 1, 68, 'D')
ON CONFLICT (student_id, course_id) DO NOTHING;
