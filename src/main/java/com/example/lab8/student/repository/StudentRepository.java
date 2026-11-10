package com.example.lab8.student.repository;

import com.example.lab8.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

/** Bound to student_db via StudentDatabaseConfig. */
public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
