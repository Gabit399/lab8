package com.example.lab8.grade.repository;

import com.example.lab8.grade.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Bound to grade_db via GradeDatabaseConfig. */
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByStudentIdOrderByGradedAtAsc(Long studentId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentId(Long studentId);

    boolean existsByCourseId(Long courseId);
}
